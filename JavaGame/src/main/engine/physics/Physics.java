package main.engine.physics;

import com.newton.NewtonBody;
import com.newton.generated.NewtonApplyForceAndTorque;
import com.newton.generated.Newton_h;

import jdk.incubator.foreign.MemoryAddress;

public class Physics {
	public static class ApplyGravity implements NewtonApplyForceAndTorque {
		@Override
		public void apply(MemoryAddress x0, float x1, int x2) {
			NewtonBody body = NewtonBody.wrap(x0);
			//Newton_h.NewtonBodyGetMass(body, body, body, x0, body);
		}
		
	}
}
