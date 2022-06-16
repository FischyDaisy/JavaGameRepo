package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonConvexHull extends NewtonCollision {
	
	protected NewtonConvexHull(MemoryAddress address) {
		super(address);
	}
	
	/**
	 * 
	 * @param world
	 * @param count
	 * @param vertexCloud
	 * @param strideInBytes
	 * @param tolerance
	 * @param shapeID
	 * @param offsetMatrix
	 * @param allocator
	 * @return
	 */
	public static NewtonConvexHull create(NewtonWorld world, int count,  float[] vertexCloud,  int strideInBytes,  float tolerance,  int shapeID,  float[] offsetMatrix, SegmentAllocator allocator) {
		MemorySegment vertCloud = allocator.allocateArray(Newton_h.C_FLOAT, vertexCloud);
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, offsetMatrix);
		return new NewtonConvexHull(Newton_h.NewtonCreateConvexHull(world.address, count, vertCloud, strideInBytes, tolerance, shapeID, matrix));
	}
	
	public int getFaceIndices(int face, int[] faceIndices) {
		return 0;
	}
}
