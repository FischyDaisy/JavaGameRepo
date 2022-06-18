package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCylinder extends NewtonCollision {
	
	protected NewtonCylinder(MemoryAddress address) {
		super(address);
	}
	
	/**
	 * 
	 * @param world
	 * @param radio0
	 * @param radio1
	 * @param height
	 * @param shapeID
	 * @param offsetMatrix
	 * @param allocator
	 * @return
	 */
	public static NewtonCylinder create(NewtonWorld world, float radio0,  float radio1,  float height,  int shapeID,  float[] offsetMatrix, SegmentAllocator allocator) {
		if (offsetMatrix == null) {
			return new NewtonCylinder(Newton_h.NewtonCreateCylinder(world.address, radio0, radio1, height, shapeID, MemoryAddress.NULL));
		}
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, offsetMatrix);
		return new NewtonCylinder(Newton_h.NewtonCreateCylinder(world.address, radio0, radio1, height, shapeID, matrix));
	}
}
