package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

/**
 * Callback for internal processing of triangles.
 * 
 * @see StridingMeshInterface#internalProcessAllTriangles
 * @author jezek2
 */
public abstract class InternalTriangleIndexCallback {

	public abstract void internalProcessTriangleIndex(Vector3f[] triangle, int partId, int triangleIndex);
	
}