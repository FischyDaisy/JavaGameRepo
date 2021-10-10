package main.engine.physics.collision.narrowphase;

import org.joml.Vector3f;

import com.bulletphysics.collision.narrowphase.GjkEpaSolver;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.IDebugDraw;

import main.engine.utility.physUtils.Transform;

/**
 * GjkEpaPenetrationDepthSolver uses the Expanding Polytope Algorithm to calculate
 * the penetration depth between two convex shapes.
 * 
 * @author jezek2
 */
public class GjkEpaPenetrationDepthSolver extends ConvexPenetrationDepthSolver {

	private GjkEpaSolver gjkEpaSolver = new GjkEpaSolver();

	public boolean calcPenDepth(SimplexSolverInterface simplexSolver,
												  ConvexShape pConvexA, ConvexShape pConvexB,
												  Transform transformA, Transform transformB,
												  Vector3f v, Vector3f wWitnessOnA, Vector3f wWitnessOnB,
												  IDebugDraw debugDraw/*, btStackAlloc* stackAlloc*/)
	{
		float radialmargin = 0f;

		// JAVA NOTE: 2.70b1: update when GjkEpaSolver2 is ported
		
		GjkEpaSolver.Results results = new GjkEpaSolver.Results();
		if (gjkEpaSolver.collide(pConvexA, transformA,
				pConvexB, transformB,
				radialmargin/*,stackAlloc*/, results)) {
			//debugDraw->drawLine(results.witnesses[1],results.witnesses[1]+results.normal,btVector3(255,0,0));
			//resultOut->addContactPoint(results.normal,results.witnesses[1],-results.depth);
			wWitnessOnA.set(results.witnesses[0]);
			wWitnessOnB.set(results.witnesses[1]);
			return true;
		}

		return false;
	}

}