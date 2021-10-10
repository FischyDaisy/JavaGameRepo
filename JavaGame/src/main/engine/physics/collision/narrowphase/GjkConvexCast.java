package main.engine.physics.collision.narrowphase;

import org.joml.Vector3f;

import com.bulletphysics.collision.narrowphase.GjkPairDetector;
import com.bulletphysics.collision.narrowphase.PointCollector;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.VectorUtil;

import main.engine.physics.collision.narrowphase.DiscreteCollisionDetectorInterface.ClosestPointInput;
import main.engine.utility.physUtils.ObjectPool;
import main.engine.utility.physUtils.Transform;

/**
 * GjkConvexCast performs a raycast on a convex object using support mapping.
 * 
 * @author jezek2
 */
public class GjkConvexCast extends ConvexCast {

	//protected final BulletStack stack = BulletStack.get();
	protected final ObjectPool<ClosestPointInput> pointInputsPool = ObjectPool.get(ClosestPointInput.class);

//#ifdef BT_USE_DOUBLE_PRECISION
//	private static final int MAX_ITERATIONS = 64;
//#else
	private static final int MAX_ITERATIONS = 32;
//#endif
	
	private SimplexSolverInterface simplexSolver;
	private ConvexShape convexA;
	private ConvexShape convexB;
	
	private GjkPairDetector gjk = new GjkPairDetector();

	public GjkConvexCast(ConvexShape convexA, ConvexShape convexB, SimplexSolverInterface simplexSolver) {
		this.simplexSolver = simplexSolver;
		this.convexA = convexA;
		this.convexB = convexB;
	}
	
	public boolean calcTimeOfImpact(Transform fromA, Transform toA, Transform fromB, Transform toB, CastResult result) {
		simplexSolver.reset();

		// compute linear velocity for this interval, to interpolate
		// assume no rotation/angular velocity, assert here?
		Vector3f linVelA = new Vector3f();
		Vector3f linVelB = new Vector3f();

		linVelA.sub(toA.origin, fromA.origin);
		linVelB.sub(toB.origin, fromB.origin);

		float radius = 0.001f;
		float lambda = 0f;
		Vector3f v = new Vector3f();
		v.set(1f, 0f, 0f);

		int maxIter = MAX_ITERATIONS;

		Vector3f n = new Vector3f();
		n.set(0f, 0f, 0f);
		boolean hasResult = false;
		Vector3f c = new Vector3f();
		Vector3f r = new Vector3f();
		r.sub(linVelA, linVelB);

		float lastLambda = lambda;
		//btScalar epsilon = btScalar(0.001);

		int numIter = 0;
		// first solution, using GJK

		Transform identityTrans = new Transform();
		identityTrans.setIdentity();

		//result.drawCoordSystem(sphereTr);

		PointCollector pointCollector = new PointCollector();

		gjk.init(convexA, convexB, simplexSolver, null); // penetrationDepthSolver);		
		ClosestPointInput input = pointInputsPool.get();
		input.init();
		try {
			// we don't use margins during CCD
			//	gjk.setIgnoreMargin(true);

			input.transformA.set(fromA);
			input.transformB.set(fromB);
			gjk.getClosestPoints(input, pointCollector, null);

			hasResult = pointCollector.hasResult;
			c.set(pointCollector.pointInWorld);

			if (hasResult) {
				float dist;
				dist = pointCollector.distance;
				n.set(pointCollector.normalOnBInWorld);

				// not close enough
				while (dist > radius) {
					numIter++;
					if (numIter > maxIter) {
						return false; // todo: report a failure
					}
					float dLambda = 0f;

					float projectedLinearVelocity = r.dot(n);

					dLambda = dist / (projectedLinearVelocity);

					lambda = lambda - dLambda;

					if (lambda > 1f) {
						return false;
					}
					if (lambda < 0f) {
						return false;					// todo: next check with relative epsilon
					}
					
					if (lambda <= lastLambda) {
						return false;
					//n.setValue(0,0,0);
					//break;
					}
					lastLambda = lambda;

					// interpolate to next lambda
					result.debugDraw(lambda);
					VectorUtil.setInterpolate3(input.transformA.origin, fromA.origin, toA.origin, lambda);
					VectorUtil.setInterpolate3(input.transformB.origin, fromB.origin, toB.origin, lambda);

					gjk.getClosestPoints(input, pointCollector, null);
					if (pointCollector.hasResult) {
						if (pointCollector.distance < 0f) {
							result.fraction = lastLambda;
							n.set(pointCollector.normalOnBInWorld);
							result.normal.set(n);
							result.hitPoint.set(pointCollector.pointInWorld);
							return true;
						}
						c.set(pointCollector.pointInWorld);
						n.set(pointCollector.normalOnBInWorld);
						dist = pointCollector.distance;
					}
					else {
						// ??
						return false;
					}

				}

				// is n normalized?
				// don't report time of impact for motion away from the contact normal (or causes minor penetration)
				if (n.dot(r) >= -result.allowedPenetration) {
					return false;
				}
				result.fraction = lambda;
				result.normal.set(n);
				result.hitPoint.set(c);
				return true;
			}

			return false;
		}
		finally {
			pointInputsPool.release(input);
		}
	}
	
}