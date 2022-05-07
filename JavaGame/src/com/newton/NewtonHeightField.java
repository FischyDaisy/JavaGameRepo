package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonHeightField extends NewtonCollision {
	
	protected NewtonHeightField(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision create(NewtonWorld world, int width,  int height,  int gridsDiagonals,  int elevationdatType,  Addressable elevationMap,  Addressable attributeMap,  float verticalScale,  float horizontalScale_x,  float horizontalScale_z,  int shapeID) {
		return new NewtonHeightField(Newton_h.NewtonCreateHeightFieldCollision(world.address, width, height, gridsDiagonals, elevationdatType, elevationMap, attributeMap, verticalScale, horizontalScale_x, horizontalScale_z, shapeID));
	}
}
