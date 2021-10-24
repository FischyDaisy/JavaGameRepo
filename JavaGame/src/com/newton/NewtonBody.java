package com.newton;

import jdk.incubator.foreign.*;

public class NewtonBody {
	
	private final MemoryAddress address;
	
	public NewtonBody(MemoryAddress address) {
		this.address = address;
	}
	
	public MemoryAddress getAddress() {
		return address;
	}
}
