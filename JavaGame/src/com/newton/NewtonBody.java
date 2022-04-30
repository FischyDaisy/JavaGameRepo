package com.newton;

import org.joml.Vector3f;

import com.newton.generated.NewtonApplyForceAndTorque;
import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public abstract class NewtonBody {
	
	protected final MemoryAddress address;
	
	protected NewtonBody(MemoryAddress address) {
		this.address = address;
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
	
	public NewtonJoint findJoint(NewtonBody otherBody) {
		return new NewtonJoint(Newton_h.NewtonWorldFindJoint(address, otherBody.address));
	}
	
	public void addForce(float[] force) {
	}
	
	public void addForce(Vector3f force) {
	}
	
	public void addTorque(float[] torque) {
	}
	
	public void addTorque(Vector3f torque) {
	}
	
	public NewtonApplyForceAndTorque getForceAndTorqueCallback(ResourceScope scope) {
		return getForceAndTorqueCallback(this, scope);
	}
	
	public static NewtonApplyForceAndTorque getForceAndTorqueCallback(NewtonBody body, ResourceScope scope) {
		return NewtonApplyForceAndTorque.ofAddress(Newton_h.NewtonBodyGetForceAndTorqueCallback(body.address), scope);
	}
	
	public void setForceAndTorqueCallback(NewtonApplyForceAndTorque callback, ResourceScope scope) {
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
		//MemorySegment mass = Newton.createFloatSegment(scope);
		//MemorySegment Ixx = Newton.createFloatSegment(scope);
		//MemorySegment Iyy = Newton.createFloatSegment(scope);
		//MemorySegment Izz = Newton.createFloatSegment(scope);
		
		//Newton_h.NewtonBodyGetMass(body.address, mass, Ixx, Iyy, Izz);
		return new float[] {0f};
	}
	
	public void destroy() {
		Newton_h.NewtonDestroyBody(address);
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
