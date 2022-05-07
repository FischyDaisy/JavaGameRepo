package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCompoundCollision extends NewtonCollision {
	
	protected NewtonCompoundCollision(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision create(NewtonWorld world, int shapeID) {
		return new NewtonCompoundCollision(Newton_h.NewtonCreateCompoundCollision(world.address, shapeID));
	}
}
