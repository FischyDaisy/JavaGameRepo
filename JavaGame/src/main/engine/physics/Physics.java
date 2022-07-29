package main.engine.physics;

import crab.newton.NewtonBody;
import jdk.incubator.foreign.*;

public class Physics {
	
	public static final float GRAVITY_FORCE = -9.8f;
	
	public static void applyGravity(MemoryAddress bodyPtr, float timestep, int threadIndex) {
		NewtonBody body = NewtonBody.wrap(bodyPtr);
		float[] mass = body.getMass();
		float[] newMass = new float[] {0f, mass[0] * GRAVITY_FORCE, 0f};
		body.setForce(newMass);
	}
}
