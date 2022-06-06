package com.newton;

import org.joml.Matrix4f;

import com.newton.generated.Constants$root;
import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonConvexHull extends NewtonCollision {
	
	protected NewtonConvexHull(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonConvexHull create(NewtonWorld world, int count,  Addressable vertexCloud,  int strideInBytes,  float tolerance,  int shapeID,  Addressable offsetMatrix) {
		return new NewtonConvexHull(Newton_h.NewtonCreateConvexHull(world.address, count, vertexCloud, strideInBytes, tolerance, shapeID, offsetMatrix));
	}
	
	public static NewtonConvexHull create(NewtonWorld world, int count,  float[] vertexCloud,  int strideInBytes,  float tolerance,  int shapeID,  Matrix4f offsetMatrix, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment vertCloud = allocator.allocateArray(Newton_h.C_FLOAT, vertexCloud);
		float[] matArr = new float[16];
		offsetMatrix.get(matArr);
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, matArr);
		return create(world, count, vertCloud, strideInBytes, tolerance, shapeID, matrix);
	}
	
	public int getFaceIndices(int face, int[] faceIndices) {
		return 0;
	}
}
