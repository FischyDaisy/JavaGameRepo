package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonSphere extends NewtonCollision {
	
	private NewtonSphere(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision createSphereCollision(NewtonWorld world, float radius, int shapeID, Addressable offsetMatrix) {
		return new NewtonSphere(Newton_h.NewtonCreateSphere(world, radius, shapeID, offsetMatrix));
	}
}
