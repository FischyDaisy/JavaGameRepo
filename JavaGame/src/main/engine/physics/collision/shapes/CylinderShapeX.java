package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

/**
 * Cylinder shape around the X axis.
 * 
 * @author jezek2
 */
public class CylinderShapeX extends CylinderShape {

	public CylinderShapeX(Vector3f halfExtents) {
		super(halfExtents, false);
		upAxis = 0;
		recalcLocalAabb();
	}

	@Override
	public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec, Vector3f out) {
		return cylinderLocalSupportX(getHalfExtentsWithoutMargin(Stack.alloc(Vector3f.class)), vec, out);
	}

	@Override
	public void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector3f[] vectors, Vector3f[] supportVerticesOut, int numVectors) {
		for (int i = 0; i < numVectors; i++) {
			cylinderLocalSupportX(getHalfExtentsWithoutMargin(Stack.alloc(Vector3f.class)), vectors[i], supportVerticesOut[i]);
		}
	}

	@Override
	public float getRadius() {
		return getHalfExtentsWithMargin(Stack.alloc(Vector3f.class)).y;
	}

	@Override
	public String getName() {
		return "CylinderX";
	}
	
}