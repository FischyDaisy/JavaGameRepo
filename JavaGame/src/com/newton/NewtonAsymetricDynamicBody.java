package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonAsymetricDynamicBody extends NewtonBody {

	protected NewtonAsymetricDynamicBody(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonBody create(NewtonWorld world, NewtonCollision collision, float[] matrix, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment matrixSeg = allocator.allocateArray(Newton_h.C_FLOAT, matrix);
		return new NewtonAsymetricDynamicBody(Newton_h.NewtonCreateAsymetricDynamicBody(world.address, collision.address, matrixSeg));
	}
}
