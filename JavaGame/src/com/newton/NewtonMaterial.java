package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonMaterial {
	
	protected final MemoryAddress address;
	
	protected NewtonMaterial(MemoryAddress address) {
		this.address = address;
	}
	
	public MemoryAddress getMaterialPairUserData() {
		return Newton_h.NewtonMaterialGetMaterialPairUserData(address);
	}
	
	public int getContactFaceAttribute() {
		return Newton_h.NewtonMaterialGetContactFaceAttribute(address);
	}
	
	public NewtonCollision getBodyCollidingShape(NewtonBody body) {
		MemoryAddress collisionPtr = Newton_h.NewtonMaterialGetBodyCollidingShape(address, body.address);
		return NewtonCollision.wrap(collisionPtr);
	}
	
	public float getContactNormalSpeed() {
		return Newton_h.NewtonMaterialGetContactNormalSpeed(address);
	}
	
	public void getContactForce(NewtonBody body, float[] force, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment forceSegment = allocator.allocateArray(Newton_h.C_FLOAT, new float[] {0f, 0f, 0f});
		Newton_h.NewtonMaterialGetContactForce(address, body.address, forceSegment);
		float[] tempArr = forceSegment.toArray(Newton_h.C_FLOAT);
		force[0] = tempArr[0];
		force[1] = tempArr[1];
		force[2] = tempArr[2];
	}
	
	public void getContactPositionAndNormal(NewtonBody body, float[] position, float[] normal, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment positionSegment = allocator.allocateArray(Newton_h.C_FLOAT, new float[] {0f, 0f, 0f});
		MemorySegment normalSegment = allocator.allocateArray(Newton_h.C_FLOAT, new float[] {0f, 0f, 0f});
		Newton_h.NewtonMaterialGetContactPositionAndNormal(address, body.address, positionSegment, normalSegment);
		float[] tempArr = positionSegment.toArray(Newton_h.C_FLOAT);
		position[0] = tempArr[0];
		position[1] = tempArr[1];
		position[2] = tempArr[2];
		tempArr = normalSegment.toArray(Newton_h.C_FLOAT);
		normal[0] = tempArr[0];
		normal[1] = tempArr[1];
		normal[2] = tempArr[2];
	}
	
	public void getContactTangentDirections(NewtonBody body, float[] dir0, float[] dir1, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment dir0Segment = allocator.allocateArray(Newton_h.C_FLOAT, new float[] {0f, 0f, 0f});
		MemorySegment dir1Segment = allocator.allocateArray(Newton_h.C_FLOAT, new float[] {0f, 0f, 0f});
		Newton_h.NewtonMaterialGetContactTangentDirections(address, body.address, dir0Segment, dir1Segment);
		float[] tempArr = dir0Segment.toArray(Newton_h.C_FLOAT);
		dir0[0] = tempArr[0];
		dir0[1] = tempArr[1];
		dir0[2] = tempArr[2];
		tempArr = dir1Segment.toArray(Newton_h.C_FLOAT);
		dir1[0] = tempArr[0];
		dir1[1] = tempArr[1];
		dir1[2] = tempArr[2];
	}
	
	public float getContactTangentSpeed(int index) {
		return Newton_h.NewtonMaterialGetContactTangentSpeed(address, index);
	}
	
	public float getContactMaxNormalImpact() {
		return Newton_h.NewtonMaterialGetContactMaxNormalImpact(address);
	}
	
	public float getContactMaxTangentImpact(int index) {
		return Newton_h.NewtonMaterialGetContactMaxTangentImpact(address, index);
	}
	
	public float getContactPenetration() {
		return Newton_h.NewtonMaterialGetContactPenetration(address);
	}
	
	public void setAsSoftContact(float relaxation) {
		Newton_h.NewtonMaterialSetAsSoftContact(address, relaxation);
	}
	
	public void setContactSoftness(float softness) {
		Newton_h.NewtonMaterialSetContactSoftness(address, softness);
	}
	
	public void setContactThickness(float thickness) {
		Newton_h.NewtonMaterialSetContactThickness(address, thickness);
	}
	
	public void setContactElasticity(float elasticity) {
		Newton_h.NewtonMaterialSetContactElasticity(address, elasticity);
	}
	
	public void setContactFrictionState(int state, int index) {
		Newton_h.NewtonMaterialSetContactFrictionState(address, state, index);
	}
	
	public void setContactFrictionCoefficient(float staticFrictionCoef, float kineticFrictionCoef, int index) {
		Newton_h.NewtonMaterialSetContactFrictionCoef(address, staticFrictionCoef, kineticFrictionCoef, index);
	}
	
	public void setContactNormalAcceleration(float accel) {
		Newton_h.NewtonMaterialSetContactNormalAcceleration(address, accel);
	}
	
	public void setContactNormalDirection(float[] direction, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment dirSegment = allocator.allocateArray(Newton_h.C_FLOAT, direction);
		Newton_h.NewtonMaterialSetContactNormalDirection(address, dirSegment);
	}
	
	public void setContactPosition(float[] position, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment positionSegment = allocator.allocateArray(Newton_h.C_FLOAT, position);
		Newton_h.NewtonMaterialSetContactPosition(address, positionSegment);
	}
	
	public void setContactTangentFriction(float friction, int index) {
		Newton_h.NewtonMaterialSetContactTangentFriction(address, friction, index);
	}
	
	public void setContactTangentAcceleration(float accel, int index) {
		Newton_h.NewtonMaterialSetContactTangentAcceleration(address, accel, index);
	}
	
	public void rotateContactTangentDirections(float[] alignVector, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment alignVecSegment = allocator.allocateArray(Newton_h.C_FLOAT, alignVector);
		Newton_h.NewtonMaterialContactRotateTangentDirections(address, alignVecSegment);
	}
}
