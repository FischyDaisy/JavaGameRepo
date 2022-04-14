package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCone extends NewtonCollision {
	
	private NewtonCone(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision createConeCollision(NewtonWorld world, float radius,  float height,  int shapeID,  Addressable offsetMatrix) {
		return new NewtonCone(Newton_h.NewtonCreateCone(world.address, radius, height, shapeID, offsetMatrix));
	}
}
