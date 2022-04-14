package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public abstract class NewtonCollision {
	
	protected final MemoryAddress address;
	protected ResourceScope scope;
	
	protected NewtonCollision(MemoryAddress address) {
		this.address = address;
		scope = ResourceScope.newConfinedScope();
	}
	
	public void destroy() {
		Newton_h.NewtonDestroyCollision(address);
		scope.close();
	}
}
