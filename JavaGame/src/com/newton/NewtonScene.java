package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonScene extends NewtonCollision {
	
	private NewtonScene(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision createSceneCollision(NewtonWorld world, int shapeID) {
		return new NewtonScene(Newton_h.NewtonCreateSceneCollision(world, shapeID));
	}
}
