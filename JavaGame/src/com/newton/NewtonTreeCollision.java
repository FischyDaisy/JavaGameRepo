package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonTreeCollision extends NewtonCollision {
	
	private NewtonTreeCollision(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision createTreeCollision(NewtonWorld world, int shapeID) {
		return new NewtonTreeCollision(Newton_h.NewtonCreateTreeCollision(world, shapeID));
	}
}
