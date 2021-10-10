package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

import com.bulletphysics.linearmath.VectorUtil;

/**
 * StridingMeshInterface is the abstract class for high performance access to
 * triangle meshes. It allows for sharing graphics and collision meshes. Also
 * it provides locking/unlocking of graphics meshes that are in GPU memory.
 * 
 * @author jezek2
 */
public abstract class StridingMeshInterface {

	protected final Vector3f scaling = new Vector3f(1f, 1f, 1f);
	
	public void internalProcessAllTriangles(InternalTriangleIndexCallback callback, Vector3f aabbMin, Vector3f aabbMax) {
		int graphicssubparts = getNumSubParts();
		Vector3f[] triangle/*[3]*/ = new Vector3f[]{ Stack.alloc(Vector3f.class), Stack.alloc(Vector3f.class), Stack.alloc(Vector3f.class) };

		Vector3f meshScaling = getScaling(Stack.alloc(Vector3f.class));

		for (int part=0; part<graphicssubparts; part++) {
			VertexData data = getLockedReadOnlyVertexIndexBase(part);

			for (int i=0, cnt=data.getIndexCount()/3; i<cnt; i++) {
				data.getTriangle(i*3, meshScaling, triangle);
				callback.internalProcessTriangleIndex(triangle, part, i);
			}

			unLockReadOnlyVertexBase(part);
		}
	}

	private static class AabbCalculationCallback extends InternalTriangleIndexCallback {
		public final Vector3f aabbMin = new Vector3f(1e30f, 1e30f, 1e30f);
		public final Vector3f aabbMax = new Vector3f(-1e30f, -1e30f, -1e30f);

		public void internalProcessTriangleIndex(Vector3f[] triangle, int partId, int triangleIndex) {
			VectorUtil.setMin(aabbMin, triangle[0]);
			VectorUtil.setMax(aabbMax, triangle[0]);
			VectorUtil.setMin(aabbMin, triangle[1]);
			VectorUtil.setMax(aabbMax, triangle[1]);
			VectorUtil.setMin(aabbMin, triangle[2]);
			VectorUtil.setMax(aabbMax, triangle[2]);
		}
	}
	
	public void calculateAabbBruteForce(Vector3f aabbMin, Vector3f aabbMax) {
		// first calculate the total aabb for all triangles
		AabbCalculationCallback aabbCallback = new AabbCalculationCallback();
		aabbMin.set(-1e30f, -1e30f, -1e30f);
		aabbMax.set(1e30f, 1e30f, 1e30f);
		internalProcessAllTriangles(aabbCallback, aabbMin, aabbMax);

		aabbMin.set(aabbCallback.aabbMin);
		aabbMax.set(aabbCallback.aabbMax);
	}
	
	/**
	 * Get read and write access to a subpart of a triangle mesh.
	 * This subpart has a continuous array of vertices and indices.
	 * In this way the mesh can be handled as chunks of memory with striding
	 * very similar to OpenGL vertexarray support.
	 * Make a call to unLockVertexBase when the read and write access is finished.
	 */
	public abstract VertexData getLockedVertexIndexBase(int subpart/*=0*/);

	public abstract VertexData getLockedReadOnlyVertexIndexBase(int subpart/*=0*/);

	/**
	 * unLockVertexBase finishes the access to a subpart of the triangle mesh.
	 * Make a call to unLockVertexBase when the read and write access (using getLockedVertexIndexBase) is finished.
	 */
	public abstract void unLockVertexBase(int subpart);

	public abstract void unLockReadOnlyVertexBase(int subpart);

	/**
	 * getNumSubParts returns the number of seperate subparts.
	 * Each subpart has a continuous array of vertices and indices.
	 */
	public abstract int getNumSubParts();

	public abstract void preallocateVertices(int numverts);
	public abstract void preallocateIndices(int numindices);

	public Vector3f getScaling(Vector3f out) {
		out.set(scaling);
		return out;
	}
	
	public void setScaling(Vector3f scaling) {
		this.scaling.set(scaling);
	}
	
}