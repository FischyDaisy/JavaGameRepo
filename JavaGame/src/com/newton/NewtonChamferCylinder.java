package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonChamferCylinder extends NewtonCollision {
	
	private NewtonChamferCylinder(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision createChamferCylinderCollision(NewtonWorld world, float radius,  float height,  int shapeID,  Addressable offsetMatrix) {
		return new NewtonChamferCylinder(Newton_h.NewtonCreateChamferCylinder(world, radius, height, shapeID, offsetMatrix));
	}
}
