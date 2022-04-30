package com.newton;

import jdk.incubator.foreign.*;

public class NewtonJoint {
	
	protected final MemoryAddress address;
	
	protected NewtonJoint(MemoryAddress address) {
		this.address = address;
	}
}
