package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonSphere extends NewtonCollision {
	
	private NewtonSphere(MemoryAddress address) {
		super(address, ResourceScope.newConfinedScope());
	}
	
	private NewtonSphere(MemoryAddress address, ResourceScope scope) {
		super(address, ResourceScope.newConfinedScope());
	}
	
	public static NewtonCollision create(NewtonWorld world, float radius, int shapeID, Addressable offsetMatrix) {
		return new NewtonSphere(Newton_h.NewtonCreateSphere(world.address, radius, shapeID, offsetMatrix));
	}
	
	public static NewtonCollision create(NewtonWorld world, float radius, int shapeID, Addressable offsetMatrix, ResourceScope scope) {
		return new NewtonSphere(Newton_h.NewtonCreateSphere(world.address, radius, shapeID, offsetMatrix), scope);
	}
}
