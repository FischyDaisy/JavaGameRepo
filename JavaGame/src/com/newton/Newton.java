package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.Addressable;

public class Newton {
	public static NewtonWorld NewtonCreate() {
		return new NewtonWorld(Newton_h.NewtonCreate(new Object[] {}));
	}
	
	public static void NewtonDestroy(NewtonWorld world) {
		Newton_h.NewtonDestroy(world.getAddress());
	}
	
	public static void NewtonDestroyAllBodies(NewtonWorld world) {
		Newton_h.NewtonDestroyAllBodies(world.getAddress());
	}
	
	public static void NewtonSetSolverIterations(NewtonWorld world, int i) {
		Newton_h.NewtonSetSolverIterations(world.getAddress(), i);
	}
	
	public static NewtonCollision NewtonCreateBox(NewtonWorld world, 
			float dx, float dy, float dz, int shapeId, Addressable offsetMatrix) {
		return NewtonCreateBox(world.getAddress(), dx, dy, dz, shapeId, offsetMatrix);
	}
	
	public static NewtonCollision NewtonCreateBox(Addressable world, 
			float dx, float dy, float dz, int shapeId, Addressable offsetMatrix) {
		return new NewtonCollision(Newton_h.NewtonCreateBox(world, dx, dy, dz, shapeId, offsetMatrix));
	}
}
