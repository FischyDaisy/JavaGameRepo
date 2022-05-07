package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public abstract class NewtonCollision {
	
	protected final MemoryAddress address;
	
	protected NewtonCollision(MemoryAddress address) {
		this.address = address;
	}
	
	public void destroy() {
		Newton_h.NewtonDestroyCollision(address);
	}
	
	protected static NewtonCollision wrap(MemoryAddress address) {
		int collisionType = Newton_h.NewtonCollisionGetType(address);
		return switch (collisionType) {
			case 0 -> new NewtonSphere(address);
			case 1 -> new NewtonCapsule(address);
			case 2 -> new NewtonCylinder(address);
			case 3 -> new NewtonChamferCylinder(address);
			case 4 -> new NewtonBox(address);
			case 5 -> new NewtonCone(address);
			case 6 -> new NewtonConvexHull(address);
			case 7 -> new NewtonNull(address);
			case 8 -> new NewtonCompoundCollision(address);
			case 9 -> new NewtonTreeCollision(address);
			case 10 -> new NewtonHeightField(address);
			default -> throw new RuntimeException("Error wrapping MemoryAddress");
		};
	}
}