package main.engine.physics;

import com.newton.*;
import jdk.incubator.foreign.*;

public class NewtonTest {
	public static void testFunc() {
		NewtonWorld world = NewtonWorld.create();
		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			world.setDestructorCallback((addr) -> {System.out.println(addr.toString());}, scope);
		}
	}
}
