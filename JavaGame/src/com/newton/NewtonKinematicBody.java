package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonKinematicBody extends NewtonBody {
	private NewtonKinematicBody(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonBody create(NewtonWorld world, NewtonCollision collision, Addressable matrix) {
		return new NewtonKinematicBody(Newton_h.NewtonCreateKinematicBody(world.address, collision.address, matrix));
	}
	
	protected static NewtonBody wrapImpl(MemoryAddress address) {
		return new NewtonKinematicBody(address);
	}
}
