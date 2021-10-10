package main.engine.physics.collision.narrowphase;

import org.joml.Vector3f;

/**
 * SimplexSolverInterface can incrementally calculate distance between origin and
 * up to 4 vertices. Used by GJK or Linear Casting. Can be implemented by the
 * Johnson-algorithm or alternative approaches based on voronoi regions or barycentric
 * coordinates.
 * 
 * @author jezek2
 */
public abstract class SimplexSolverInterface {

	public abstract void reset();

	public abstract void addVertex(Vector3f w, Vector3f p, Vector3f q);
	
	public abstract boolean closest(Vector3f v);

	public abstract float maxVertex();

	public abstract boolean fullSimplex();

	public abstract int getSimplex(Vector3f[] pBuf, Vector3f[] qBuf, Vector3f[] yBuf);

	public abstract boolean inSimplex(Vector3f w);
	
	public abstract void backup_closest(Vector3f v);

	public abstract boolean emptySimplex();

	public abstract void compute_points(Vector3f p1, Vector3f p2);

	public abstract int numVertices();
	
}