package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonScene extends NewtonCollision {
	
	private NewtonScene(MemoryAddress address) {
		super(address, ResourceScope.newConfinedScope());
	}
	
	private NewtonScene(MemoryAddress address, ResourceScope scope) {
		super(address, scope);
	}
	
	public static NewtonCollision create(NewtonWorld world, int shapeID) {
		return new NewtonScene(Newton_h.NewtonCreateSceneCollision(world.address, shapeID));
	}
	
	public static NewtonCollision create(NewtonWorld world, int shapeID, ResourceScope scope) {
		return new NewtonScene(Newton_h.NewtonCreateSceneCollision(world.address, shapeID), scope);
	}
}
