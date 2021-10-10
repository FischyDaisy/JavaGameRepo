package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

import main.engine.physics.collision.broadphase.BroadphaseNativeType;
import main.engine.utility.physUtils.Transform;

/**
 * SphereShape implements an implicit sphere, centered around a local origin with radius.
 * 
 * @author jezek2
 */
public class SphereShape extends ConvexInternalShape {
	
	public SphereShape(float radius) {
		implicitShapeDimensions.x = radius;
		collisionMargin = radius;
	}

	@Override
	public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec, Vector3f out) {
		out.set(0f, 0f, 0f);
		return out;
	}

	@Override
	public void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector3f[] vectors, Vector3f[] supportVerticesOut, int numVectors) {
		for (int i = 0; i < numVectors; i++) {
			supportVerticesOut[i].set(0f, 0f, 0f);
		}
	}

	@Override
	public void getAabb(Transform t, Vector3f aabbMin, Vector3f aabbMax) {
		Vector3f center = t.origin;
		Vector3f extent = Stack.alloc(Vector3f.class);
		extent.set(getMargin(), getMargin(), getMargin());
		aabbMin.sub(center, extent);
		aabbMax.add(center, extent);
	}

	@Override
	public BroadphaseNativeType getShapeType() {
		return BroadphaseNativeType.SPHERE_SHAPE_PROXYTYPE;
	}

	@Override
	public void calculateLocalInertia(float mass, Vector3f inertia) {
		float elem = 0.4f * mass * getMargin() * getMargin();
		inertia.set(elem, elem, elem);
	}

	@Override
	public String getName() {
		return "SPHERE";
	}
	
	public float getRadius() {
		return implicitShapeDimensions.x * localScaling.x;
	}

	@Override
	public void setMargin(float margin) {
		super.setMargin(margin);
	}

	@Override
	public float getMargin() {
		// to improve gjk behaviour, use radius+margin as the full margin, so never get into the penetration case
		// this means, non-uniform scaling is not supported anymore
		return getRadius();
	}
	
}