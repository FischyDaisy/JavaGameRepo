package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

import com.bulletphysics.linearmath.MatrixUtil;

import main.engine.physics.collision.broadphase.BroadphaseNativeType;
import main.engine.utility.physUtils.Transform;

/**
 * MinkowskiSumShape is only for advanced users. This shape represents implicit
 * based minkowski sum of two convex implicit shapes.
 * 
 * @author jezek2
 */
public class MinkowskiSumShape extends ConvexInternalShape {

	private final Transform transA = new Transform();
	private final Transform transB = new Transform();
	private ConvexShape shapeA;
	private ConvexShape shapeB;

	public MinkowskiSumShape(ConvexShape shapeA, ConvexShape shapeB) {
		this.shapeA = shapeA;
		this.shapeB = shapeB;
		this.transA.setIdentity();
		this.transB.setIdentity();
	}
	
	@Override
	public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec, Vector3f out) {
		Vector3f tmp = Stack.alloc(Vector3f.class);
		Vector3f supVertexA = Stack.alloc(Vector3f.class);
		Vector3f supVertexB = Stack.alloc(Vector3f.class);

		// btVector3 supVertexA = m_transA(m_shapeA->localGetSupportingVertexWithoutMargin(-vec*m_transA.getBasis()));
		tmp.negate(vec);
		MatrixUtil.transposeTransform(tmp, tmp, transA.basis);
		shapeA.localGetSupportingVertexWithoutMargin(tmp, supVertexA);
		transA.transform(supVertexA);

		// btVector3 supVertexB = m_transB(m_shapeB->localGetSupportingVertexWithoutMargin(vec*m_transB.getBasis()));
		MatrixUtil.transposeTransform(tmp, vec, transB.basis);
		shapeB.localGetSupportingVertexWithoutMargin(tmp, supVertexB);
		transB.transform(supVertexB);

		//return supVertexA - supVertexB;
		out.sub(supVertexA, supVertexB);
		return out;
	}

	@Override
	public void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector3f[] vectors, Vector3f[] supportVerticesOut, int numVectors) {
		//todo: could make recursive use of batching. probably this shape is not used frequently.
		for (int i = 0; i < numVectors; i++) {
			localGetSupportingVertexWithoutMargin(vectors[i], supportVerticesOut[i]);
		}
	}

	@Override
	public void getAabb(Transform t, Vector3f aabbMin, Vector3f aabbMax) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public BroadphaseNativeType getShapeType() {
		return BroadphaseNativeType.MINKOWSKI_SUM_SHAPE_PROXYTYPE;
	}

	@Override
	public void calculateLocalInertia(float mass, Vector3f inertia) {
		assert (false);
		inertia.set(0, 0, 0);
	}

	@Override
	public String getName() {
		return "MinkowskiSum";
	}
	
	@Override
	public float getMargin() {
		return shapeA.getMargin() + shapeB.getMargin();
	}

	public void setTransformA(Transform transA) {
		this.transA.set(transA);
	}

	public void setTransformB(Transform transB) {
		this.transB.set(transB);
	}

	public void getTransformA(Transform dest) {
		dest.set(transA);
	}

	public void getTransformB(Transform dest) {
		dest.set(transB);
	}

}