package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

/**
 * Allows accessing vertex data.
 * 
 * @author jezek2
 */
public abstract class VertexData {

	public abstract int getVertexCount();

	public abstract int getIndexCount();

	public abstract <T extends Vector3f> T getVertex(int idx, T out);

	public abstract void setVertex(int idx, float x, float y, float z);

	public void setVertex(int idx, Vector3f v) {
		setVertex(idx, v.x, v.y, v.z);
	}

	public abstract int getIndex(int idx);

	public void getTriangle(int firstIndex, Vector3f scale, Vector3f[] triangle) {
		for (int i=0; i<3; i++) {
			getVertex(getIndex(firstIndex+i), triangle[i]);
			triangle[i].mul(scale);
		}
	}
	
}