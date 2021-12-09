package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCapsule extends NewtonCollision {
	
	private NewtonCapsule(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision createCapsuleCollision(NewtonWorld world, float radius0,  float radius1,  float height,  int shapeID,  Addressable offsetMatrix) {
		return new NewtonCapsule(Newton_h.NewtonCreateCapsule(world, radius0, radius1, height, shapeID, offsetMatrix));
	}
}
