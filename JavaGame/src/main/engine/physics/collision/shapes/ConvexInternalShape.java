package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.VectorUtil;

import main.engine.physics.BulletGlobals;
import main.engine.utility.physUtils.Transform;

/**
 * ConvexInternalShape is an internal base class, shared by most convex shape implementations.
 * 
 * @author jezek2
 */
public abstract class ConvexInternalShape extends ConvexShape {

	// local scaling. collisionMargin is not scaled !
	protected final Vector3f localScaling = new Vector3f(1f, 1f, 1f);
	protected final Vector3f implicitShapeDimensions = new Vector3f();
	protected float collisionMargin = BulletGlobals.CONVEX_DISTANCE_MARGIN;

	/**
	 * getAabb's default implementation is brute force, expected derived classes to implement a fast dedicated version.
	 */
	@Override
	public void getAabb(Transform t, Vector3f aabbMin, Vector3f aabbMax) {
		getAabbSlow(t, aabbMin, aabbMax);
	}
	
	@Override
	public void getAabbSlow(Transform trans, Vector3f minAabb, Vector3f maxAabb) {
		float margin = getMargin();
		Vector3f vec = new Vector3f();
		Vector3f tmp1 = new Vector3f();
		Vector3f tmp2 = new Vector3f();
		
		for (int i=0;i<3;i++)
		{
			vec.set(0f, 0f, 0f);
			VectorUtil.setCoord(vec, i, 1f);

			MatrixUtil.transposeTransform(tmp1, vec, trans.basis);
			localGetSupportingVertex(tmp1, tmp2);
			
			trans.transform(tmp2);

			VectorUtil.setCoord(maxAabb, i, VectorUtil.getCoord(tmp2, i) + margin);

			VectorUtil.setCoord(vec, i, -1f);

			MatrixUtil.transposeTransform(tmp1, vec, trans.basis);
			localGetSupportingVertex(tmp1, tmp2);
			trans.transform(tmp2);

			VectorUtil.setCoord(minAabb, i, VectorUtil.getCoord(tmp2, i) - margin);
		}
	}

	@Override
	public Vector3f localGetSupportingVertex(Vector3f vec, Vector3f out) {
		Vector3f supVertex = localGetSupportingVertexWithoutMargin(vec, out);

		if (getMargin() != 0f) {
			Vector3f vecnorm = new Vector3f(vec);
			if (vecnorm.lengthSquared() < (BulletGlobals.FLT_EPSILON * BulletGlobals.FLT_EPSILON)) {
				vecnorm.set(-1f, -1f, -1f);
			}
			vecnorm.normalize();
			supVertex.scaleAdd(getMargin(), vecnorm, supVertex);
		}
		return out;
	}
	
	public void setLocalScaling(Vector3f scaling) {
		localScaling.absolute(scaling);
	}
	
	public Vector3f getLocalScaling(Vector3f out) {
		out.set(localScaling);
		return out;
	}

	public float getMargin() {
		return collisionMargin;
	}

	public void setMargin(float margin) {
		this.collisionMargin = margin;
	}

	@Override
	public int getNumPreferredPenetrationDirections() {
		return 0;
	}

	@Override
	public void getPreferredPenetrationDirection(int index, Vector3f penetrationVector) {
		throw new InternalError();
	}
	
}