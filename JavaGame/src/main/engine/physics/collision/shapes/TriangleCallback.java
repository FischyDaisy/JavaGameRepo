package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

/**
 * TriangleCallback provides a callback for each overlapping triangle when calling
 * processAllTriangles.<p>
 * 
 * This callback is called by processAllTriangles for all {@link ConcaveShape} derived
 * classes, such as {@link BvhTriangleMeshShape}, {@link StaticPlaneShape} and
 * {@link HeightfieldTerrainShape}.
 * 
 * @author jezek2
 */
public abstract class TriangleCallback {

	public abstract void processTriangle(Vector3f[] triangle, int partId, int triangleIndex);
	
}