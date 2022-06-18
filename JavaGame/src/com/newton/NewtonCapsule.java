package com.newton;

import com.newton.generated.*;

import jdk.incubator.foreign.*;

public class NewtonCapsule extends NewtonCollision {
	
	protected NewtonCapsule(MemoryAddress address) {
		super(address);
	}
	
	/**
	 * 
	 * @param world
	 * @param radius0
	 * @param radius1
	 * @param height
	 * @param shapeID
	 * @param offsetMatrix
	 * @param allocator
	 * @return
	 */
	public static NewtonCapsule create(NewtonWorld world, float radius0, float radius1, float height, int shapeID, float[] offsetMatrix, SegmentAllocator allocator) {
		if (offsetMatrix == null) {
			return new NewtonCapsule(Newton_h.NewtonCreateCapsule(world.address, radius0, radius1, height, shapeID, MemoryAddress.NULL));
		}
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, offsetMatrix);
		return new NewtonCapsule(Newton_h.NewtonCreateCapsule(world.address, radius0, radius1, height, shapeID, matrix));
	}
}
