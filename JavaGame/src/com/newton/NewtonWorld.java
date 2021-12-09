package com.newton;

import jdk.incubator.foreign.*;
import com.newton.*;
import com.newton.generated.Newton_h;

public class NewtonWorld implements Addressable {
	
	private final MemoryAddress address;
	
	private NewtonWorld(MemoryAddress address) {
		this.address = address;
	}
	
	public static NewtonWorld create() {
		return new NewtonWorld(Newton_h.NewtonCreate(new Object[] {}));
	}
	
	public void destroy() {
		Newton_h.NewtonDestroy(this);
	}
	
	public void update(float timestep) {
		Newton_h.NewtonUpdate(this, timestep);
	}
	
	public void destroyAllBodies() {
		Newton_h.NewtonDestroyAllBodies(this);
	}
	
	public void setSolverIterations(int i) {
		Newton_h.NewtonSetSolverIterations(this, i);
	}
	
	/**
	 * This method wraps a memory address into a NewtonWorld object.
	 * This method is only meant to be used internally. Improper use of this method could
	 * result in errors or an exception.
	 * @param address - MemoryAddress of NewtonWorld
	 * @return NewtonWorld object
	 */
	public static NewtonWorld wrap(MemoryAddress address) {
		return new NewtonWorld(address);
	}
	
	@Override
	public MemoryAddress address() {
		return address;
	}
}
