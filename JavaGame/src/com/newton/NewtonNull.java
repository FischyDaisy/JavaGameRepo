package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonNull extends NewtonCollision {
	
	protected NewtonNull(MemoryAddress address) {
		super(address);
	}
	
	/**
	 * 
	 * @param world
	 * @return
	 */
	public static NewtonNull create(NewtonWorld world) {
		return new NewtonNull(Newton_h.NewtonCreateNull(world.address));
	}
}
