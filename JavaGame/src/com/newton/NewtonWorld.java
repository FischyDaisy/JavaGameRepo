package com.newton;

import jdk.incubator.foreign.*;
import com.newton.*;
import com.newton.generated.Newton_h;

public class NewtonWorld {
	
	private final MemoryAddress address;
	
	public NewtonWorld(MemoryAddress address) {
		this.address = address;
	}
	
	public MemoryAddress getAddress() {
		return address;
	}	
}
