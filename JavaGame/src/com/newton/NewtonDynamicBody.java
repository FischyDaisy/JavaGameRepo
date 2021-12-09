package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonDynamicBody extends NewtonBody {
	
	private NewtonDynamicBody(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonBody create(NewtonWorld world, NewtonCollision collision, Addressable matrix) {
		return new NewtonDynamicBody(Newton_h.NewtonCreateDynamicBody(world, collision, matrix));
	}
	
	protected static NewtonBody wrapImpl(MemoryAddress address) {
		return new NewtonDynamicBody(address);
	}
}
