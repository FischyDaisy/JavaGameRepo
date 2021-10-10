package main.engine.physics.collision.shapes;

import java.nio.ByteBuffer;

import org.joml.Vector3f;


/**
*
* @author jezek2
*/
public class ByteBufferVertexData extends VertexData {

	public ByteBuffer vertexData;
	public int vertexCount;
	public int vertexStride;
	public ScalarType vertexType;

	public ByteBuffer indexData;
	public int indexCount;
	public int indexStride;
	public ScalarType indexType;

	@Override
	public int getVertexCount() {
		return vertexCount;
	}

	@Override
	public int getIndexCount() {
		return indexCount;
	}

	@Override
	public <T extends Vector3f> T getVertex(int idx, T out) {
		int off = idx*vertexStride;
		out.x = vertexData.getFloat(off+4*0);
		out.y = vertexData.getFloat(off+4*1);
		out.z = vertexData.getFloat(off+4*2);
		return out;
	}

	@Override
	public void setVertex(int idx, float x, float y, float z) {
		int off = idx*vertexStride;
		vertexData.putFloat(off+4*0, x);
		vertexData.putFloat(off+4*1, y);
		vertexData.putFloat(off+4*2, z);
	}

	@Override
	public int getIndex(int idx) {
		if (indexType == ScalarType.SHORT) {
			return indexData.getShort(idx*indexStride) & 0xFFFF;
		}
		else if (indexType == ScalarType.INTEGER) {
			return indexData.getInt(idx*indexStride);
		}
		else {
			throw new IllegalStateException("indicies type must be short or integer");
		}
	}

}