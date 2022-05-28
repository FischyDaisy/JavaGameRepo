package com.newton;


import com.newton.generated.NewtonApplyForceAndTorque;
import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public abstract class NewtonBody {
	
	protected final MemoryAddress address;
	
	protected NewtonBody(MemoryAddress address) {
		this.address = address;
	}
	
	public int getSimulationState() {
		return Newton_h.NewtonBodyGetSimulationState(address);
	}
	
	public void setSimulationState(int state) {
		Newton_h.NewtonBodySetSimulationState(address, state);
	}
	
	public int getType() {
		return Newton_h.NewtonBodyGetType(address);
	}
	
	public int getCollidable() {
		return Newton_h.NewtonBodyGetCollidable(address);
	}
	
	public void setCollidable(int collidableState) {
		Newton_h.NewtonBodySetCollidable(address, collidableState);
	}
	
	public void addForce(float[] force, SegmentAllocator allocator) {
		MemorySegment forceSeg = allocator.allocateArray(Newton_h.C_FLOAT, force);
		Newton_h.NewtonBodyAddForce(address, forceSeg);
	}
	
	public void addTorque(float[] torque, SegmentAllocator allocator) {
		MemorySegment torqueSeg = allocator.allocateArray(Newton_h.C_FLOAT, torque);
		Newton_h.NewtonBodyAddTorque(address, torqueSeg);
	}
	
	public void setCenterOfMass(float[] center, SegmentAllocator allocator) {
		MemorySegment centerSeg = allocator.allocateArray(Newton_h.C_FLOAT, center);
		Newton_h.NewtonBodySetCentreOfMass(address, centerSeg);
	}
	
	public void setMassMatrix(float mass, float Ixx, float Iyy, float Izz) {
		Newton_h.NewtonBodySetMassMatrix(address, mass, Ixx, Iyy, Izz);
	}
	
	public void setFullMassMatrix(float mass, float[] inertiaMatrix, SegmentAllocator allocator) {
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, inertiaMatrix);
		Newton_h.NewtonBodySetFullMassMatrix(address, mass, matrix);
	}
	
	public void setMassProperties(float mass, NewtonCollision collision) {
		Newton_h.NewtonBodySetMassProperties(address, mass, collision.address);
	}
	
	public void setMatrix(float[] matrix, SegmentAllocator allocator) {
		MemorySegment matrixSeg = allocator.allocateArray(Newton_h.C_FLOAT, matrix);
		Newton_h.NewtonBodySetMatrix(address, matrixSeg);
	}
	
	public void setMatrixNoSleep(float[] matrix, SegmentAllocator allocator) {
		MemorySegment matrixSeg = allocator.allocateArray(Newton_h.C_FLOAT, matrix);
		Newton_h.NewtonBodySetMatrixNoSleep(address, matrixSeg);
	}
	
	public void setMatrixRecursive(float[] matrix, SegmentAllocator allocator) {
		MemorySegment matrixSeg = allocator.allocateArray(Newton_h.C_FLOAT, matrix);
		Newton_h.NewtonBodySetMatrixRecursive(address, matrixSeg);
	}
	
	public void setMaterialGroupdID(int id) {
		Newton_h.NewtonBodySetMaterialGroupID(address, id);
	}
	
	public void setContinuousCollisionMode(int state) {
		Newton_h.NewtonBodySetContinuousCollisionMode(address, state);
	}
	
	public void setJointRecursiveCollision(int state) {
		Newton_h.NewtonBodySetJointRecursiveCollision(address, state);
	}
	
	public void setOmega(float[] omega, SegmentAllocator allocator) {
		MemorySegment omegaSeg = allocator.allocateArray(Newton_h.C_FLOAT, omega);
		Newton_h.NewtonBodySetOmega(address, omegaSeg);
	}
	
	public void setOmegaNoSleep(float[] omega, SegmentAllocator allocator) {
		MemorySegment omegaSeg = allocator.allocateArray(Newton_h.C_FLOAT, omega);
		Newton_h.NewtonBodySetOmegaNoSleep(address, omegaSeg);
	}
	
	public void setVelocity(float[] velocity, SegmentAllocator allocator) {
		MemorySegment velSeg = allocator.allocateArray(Newton_h.C_FLOAT, velocity);
		Newton_h.NewtonBodySetVelocity(address, velSeg);
	}
	
	public void setVelocityNoSleep(float[] velocity, SegmentAllocator allocator) {
		MemorySegment velSeg = allocator.allocateArray(Newton_h.C_FLOAT, velocity);
		Newton_h.NewtonBodySetVelocityNoSleep(address, velSeg);
	}
	
	public void setForce(float[] force, SegmentAllocator allocator) {
		MemorySegment forceSeg = allocator.allocateArray(Newton_h.C_FLOAT, force);
		Newton_h.NewtonBodySetForce(address, forceSeg);
	}
	
	public void setTorque(float[] torque, SegmentAllocator allocator) {
		MemorySegment torqueSeg = allocator.allocateArray(Newton_h.C_FLOAT, torque);
		Newton_h.NewtonBodySetTorque(address, torqueSeg);
	}
	
	public void setLinearDamping(float linearDamp) {
		Newton_h.NewtonBodySetLinearDamping(address, linearDamp);
	}
	
	public void destroy() {
		Newton_h.NewtonDestroyBody(address);
	}
	
	protected static NewtonBody wrap(MemoryAddress address) {
		int bodyType = Newton_h.NewtonBodyGetType(address);
		return switch (bodyType) {
			case 0 -> new NewtonDynamicBody(address);
			case 1 -> new NewtonKinematicBody(address);
			case 2 -> new NewtonAsymetricDynamicBody(address);
			default -> throw new RuntimeException("Error wrapping MemoryAddress");
		};
	}
}
