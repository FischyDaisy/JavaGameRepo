package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

/**
 * Cylinder shape around the Z axis.
 * 
 * @author jezek2
 */
public class CylinderShapeZ extends CylinderShape {

	public CylinderShapeZ(Vector3f halfExtents) {
		super(halfExtents, false);
		upAxis = 2;
		recalcLocalAabb();
	}

	@Override
	public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec, Vector3f out) {
		return cylinderLocalSupportZ(getHalfExtentsWithoutMargin(Stack.alloc(Vector3f.class)), vec, out);
	}

	@Override
	public void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector3f[] vectors, Vector3f[] supportVerticesOut, int numVectors) {
		for (int i = 0; i < numVectors; i++) {
			cylinderLocalSupportZ(getHalfExtentsWithoutMargin(Stack.alloc(Vector3f.class)), vectors[i], supportVerticesOut[i]);
		}
	}

	@Override
	public float getRadius() {
		return getHalfExtentsWithMargin(Stack.alloc(Vector3f.class)).x;
	}

	@Override
	public String getName() {
		return "CylinderZ";
	}
	
}