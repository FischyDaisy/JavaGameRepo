package com.newton;

import org.joml.Vector3f;

import com.newton.generated.NewtonApplyForceAndTorque;
import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public abstract class NewtonBody {
	
	protected final MemoryAddress address;
	protected ResourceScope scope;
	
	protected NewtonBody(MemoryAddress address) {
		this.address = address;
		scope = ResourceScope.newConfinedScope();
	}
	
	public int getType() {
		return getType(this);
	}
	
	public static int getType(NewtonBody body) {
		return Newton_h.NewtonBodyGetType(body.address);
	}
	
	public int getCollidable() {
		return getCollidable(this);
	}
	
	public static int getCollidable(NewtonBody body) {
		return Newton_h.NewtonBodyGetCollidable(body.address);
	}
	
	public void setCollidable(int collidableState) {
		setCollidable(this, collidableState);
	}
	
	public static void setCollidable(NewtonBody body, int collidableState) {
		Newton_h.NewtonBodySetCollidable(body.address, collidableState);
	}
	
	public void addForce(float[] force) {
	}
	
	public void addForce(Vector3f force) {
	}
	
	public void addTorque(float[] torque) {
	}
	
	public void addTorque(Vector3f torque) {
	}
	
	public NewtonApplyForceAndTorque getForceAndTorqueCallback() {
		return getForceAndTorqueCallback(this, scope);
	}
	
	public static NewtonApplyForceAndTorque getForceAndTorqueCallback(NewtonBody body, ResourceScope scope) {
		return NewtonApplyForceAndTorque.ofAddress(Newton_h.NewtonBodyGetForceAndTorqueCallback(body.address), scope);
	}
	
	public void setForceAndTorqueCallback(NewtonApplyForceAndTorque callback) {
		setForceAndTorqueCallback(this, callback, scope);
	}
	
	public static void setForceAndTorqueCallback(NewtonBody body, NewtonApplyForceAndTorque callback, ResourceScope scope) {
		Newton_h.NewtonBodySetForceAndTorqueCallback(body.address, NewtonApplyForceAndTorque.allocate(callback, scope));
	}
	
	public NewtonWorld getNewtonWorld() {
		return getNewtonWorld(this);
	}
	
	public static NewtonWorld getNewtonWorld(NewtonBody body) {
		return NewtonWorld.wrap(Newton_h.NewtonBodyGetWorld(body.address));
	}
	
	public static float[] getMass(NewtonBody body, ResourceScope scope) {
		MemorySegment mass = Newton.createFloatSegment(scope);
		MemorySegment Ixx = Newton.createFloatSegment(scope);
		MemorySegment Iyy = Newton.createFloatSegment(scope);
		MemorySegment Izz = Newton.createFloatSegment(scope);
		
		Newton_h.NewtonBodyGetMass(body.address, mass, Ixx, Iyy, Izz);
		return mass.toFloatArray();
	}
	
	public void cleanup() {
		if (scope.isAlive()) {
			scope.close();
		}
		scope = ResourceScope.newConfinedScope();
	}
	
	public void destroy() {
		Newton_h.NewtonDestroyBody(address);
		if (scope.isAlive()) {
			scope.close();
		}
	}
	
	public static NewtonBody wrap(MemoryAddress address) {
		int i = Newton_h.NewtonBodyGetType(address);
		switch (i) {
			case 0:
				return NewtonDynamicBody.wrapImpl(address);
			case 1:
				return NewtonKinematicBody.wrapImpl(address);
			case 2:
				// Didn't create DynamicAsymetrixBody class yet
				return null;
			default:
				throw new RuntimeException("Cannot wrap MemoryAddress");
		}	
	}
}
