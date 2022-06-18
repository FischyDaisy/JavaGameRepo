package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCone extends NewtonCollision {
	
	protected NewtonCone(MemoryAddress address) {
		super(address);
	}
	
	/**
	 * 
	 * @param world
	 * @param radius
	 * @param height
	 * @param shapeID
	 * @param offsetMatrix
	 * @param allocator
	 * @return
	 */
	public static NewtonCone create(NewtonWorld world, float radius,  float height,  int shapeID,  float[] offsetMatrix, SegmentAllocator allocator) {
		if (offsetMatrix == null) {
			return new NewtonCone(Newton_h.NewtonCreateCone(world.address, radius, height, shapeID, MemoryAddress.NULL));
		}
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, offsetMatrix);
		return new NewtonCone(Newton_h.NewtonCreateCone(world.address, radius, height, shapeID, matrix));
	}
}
