package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonKinematicBody extends NewtonBody {
	
	protected NewtonKinematicBody(MemoryAddress address) {
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
	public static NewtonKinematicBody create(NewtonWorld world, NewtonCollision collision, float[] matrix, SegmentAllocator allocator) {
		MemorySegment matrixSegment = allocator.allocateArray(Newton_h.C_FLOAT, matrix);
		return new NewtonKinematicBody(Newton_h.NewtonCreateKinematicBody(world.address, collision.address, matrixSegment));
	}
}
