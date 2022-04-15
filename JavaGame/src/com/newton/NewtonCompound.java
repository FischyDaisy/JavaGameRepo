package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCompound extends NewtonCollision {
	
	private NewtonCompound(MemoryAddress address) {
		super(address, ResourceScope.newConfinedScope());
	}
	
	private NewtonCompound(MemoryAddress address, ResourceScope scope) {
		super(address, scope);
	}
	
	public static NewtonCollision create(NewtonWorld world, int shapeID) {
		return new NewtonCompound(Newton_h.NewtonCreateCompoundCollision(world.address, shapeID));
	}
	
	public static NewtonCollision create(NewtonWorld world, int shapeID, ResourceScope scope) {
		return new NewtonCompound(Newton_h.NewtonCreateCompoundCollision(world.address, shapeID), scope);
	}
}
