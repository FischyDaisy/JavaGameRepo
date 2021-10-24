package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCollision {
	
	private final MemoryAddress address;
	
	public NewtonCollision(MemoryAddress address) {
		this.address = address;
	}
	
	public MemoryAddress getAddress() {
		return address;
	}
}
