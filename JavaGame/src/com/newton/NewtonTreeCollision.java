package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonTreeCollision extends NewtonCollision {
	
	protected NewtonTreeCollision(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision create(NewtonWorld world, int shapeID) {
		return new NewtonTreeCollision(Newton_h.NewtonCreateTreeCollision(world.address, shapeID));
	}
}
