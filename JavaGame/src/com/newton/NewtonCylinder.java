package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCylinder extends NewtonCollision {
	
	private NewtonCylinder(MemoryAddress address) {
		super(address);
		scope = ResourceScope.newConfinedScope();
	}
	
	public static NewtonCollision createCylinderCollision(NewtonWorld world, float radio0,  float radio1,  float height,  int shapeID,  Addressable offsetMatrix) {
		return new NewtonCylinder(Newton_h.NewtonCreateCylinder(world, radio0, radio1, height, shapeID, offsetMatrix));
	}
}
