package com.newton;

import jdk.incubator.foreign.*;

public class NewtonMesh {
	
	protected final MemoryAddress address;
	
	protected NewtonMesh(MemoryAddress address) {
		this.address = address;
	}
}
