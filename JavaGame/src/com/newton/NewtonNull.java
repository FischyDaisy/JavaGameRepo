package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonNull extends NewtonCollision {
	
	private NewtonNull(MemoryAddress address) {
		super(address, ResourceScope.newConfinedScope());
	}
	
	private NewtonNull(MemoryAddress address, ResourceScope scope) {
		super(address, scope);
	}
	
	public static NewtonCollision create(NewtonWorld world) {
		return new NewtonNull(Newton_h.NewtonCreateNull(world.address));
	}
	
	public static NewtonCollision create(NewtonWorld world, ResourceScope scope) {
		return new NewtonNull(Newton_h.NewtonCreateNull(world.address), scope);
	}
}
