package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonSphere extends NewtonCollision {
	
	protected NewtonSphere(MemoryAddress address) {
		super(address);
	}
	
	/**
	 * 
	 * @param world
	 * @param radius
	 * @param shapeID
	 * @param offsetMatrix
	 * @param allocator
	 * @return
	 */
	public static NewtonSphere create(NewtonWorld world, float radius, int shapeID, float[] offsetMatrix, SegmentAllocator allocator) {
		if (offsetMatrix == null) {
			return new NewtonSphere(Newton_h.NewtonCreateSphere(world.address, radius, shapeID, MemoryAddress.NULL));
		}
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, offsetMatrix);
		return new NewtonSphere(Newton_h.NewtonCreateSphere(world.address, radius, shapeID, matrix));
	}
}
