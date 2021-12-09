package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCompound extends NewtonCollision {
	
	private NewtonCompound(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision createCompoundCollision(NewtonWorld world, int shapeID) {
		return new NewtonCompound(Newton_h.NewtonCreateCompoundCollision(world, shapeID));
	}
}
