package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonDynamicBody extends NewtonBody {
	
	protected NewtonDynamicBody(MemoryAddress address) {
		super(address);
	}
	
	/**
	 * 
	 * @param world
	 * @param collision
	 * @param matrix
	 * @param allocator
	 * @return
	 */
	public static NewtonDynamicBody create(NewtonWorld world, NewtonCollision collision, float[] matrix, SegmentAllocator allocator) {
		MemorySegment matrixSegment = allocator.allocateArray(Newton_h.C_FLOAT, matrix);
		return new NewtonDynamicBody(Newton_h.NewtonCreateDynamicBody(world.address, collision.address, matrixSegment));
	}
}
