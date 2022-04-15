package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public abstract class NewtonCollision {
	
	protected final MemoryAddress address;
	protected final ResourceScope scope;
	
	protected NewtonCollision(MemoryAddress address, ResourceScope scope) {
		this.address = address;
		this.scope = scope;
	}
	
	public void destroy() {
		Newton_h.NewtonDestroyCollision(address);
		if (scope.isAlive()) {
			scope.close();
		}
	}
}