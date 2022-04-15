package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonTreeCollision extends NewtonCollision {
	
	private NewtonTreeCollision(MemoryAddress address) {
		super(address, ResourceScope.newConfinedScope());
	}
	
	private NewtonTreeCollision(MemoryAddress address, ResourceScope scope) {
		super(address, scope);
	}
	
	public static NewtonCollision create(NewtonWorld world, int shapeID) {
		return new NewtonTreeCollision(Newton_h.NewtonCreateTreeCollision(world.address, shapeID));
	}
	
	public static NewtonCollision create(NewtonWorld world, int shapeID, ResourceScope scope) {
		return new NewtonTreeCollision(Newton_h.NewtonCreateTreeCollision(world.address, shapeID), scope);
	}
}
