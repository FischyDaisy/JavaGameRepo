package com.newton;

import jdk.incubator.foreign.*;

public class NewtonMaterial {
	
	protected final MemoryAddress address;
	
	protected NewtonMaterial(MemoryAddress address) {
		this.address = address;
	}
}
