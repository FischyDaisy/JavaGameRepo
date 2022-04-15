package com.newton;

import org.joml.Matrix4f;

import com.newton.generated.*;

import jdk.incubator.foreign.*;

public class NewtonCapsule extends NewtonCollision {
	
	private NewtonCapsule(MemoryAddress address) {
		super(address, ResourceScope.newConfinedScope());
	}
	
	private NewtonCapsule(MemoryAddress address, ResourceScope scope) {
		super(address, scope);
	}
	
	public static NewtonCollision create(NewtonWorld world, float radius0,  float radius1,  float height,  int shapeID,  Addressable offsetMatrix) {
		return new NewtonCapsule(Newton_h.NewtonCreateCapsule(world.address, radius0, radius1, height, shapeID, offsetMatrix));
	}
	
	public static NewtonCollision create(NewtonWorld world, float radius0,  float radius1,  float height,  int shapeID,  Addressable offsetMatrix, ResourceScope scope) {
		return new NewtonCapsule(Newton_h.NewtonCreateCapsule(world.address, radius0, radius1, height, shapeID, offsetMatrix), scope);
	}
	
	public static NewtonCollision create(NewtonWorld world, float radius0, float radius1, float height, int shapeID, Matrix4f offsetMatrix) {
		ResourceScope scope = ResourceScope.newConfinedScope();
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		float[] matArr = new float[16];
		offsetMatrix.get(matArr);
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, matArr);
		return create(world, radius0, radius1, height, shapeID, matrix, scope);
	}
	
	public static NewtonCollision create(NewtonWorld world, float radius0, float radius1, float height, int shapeID, Matrix4f offsetMatrix, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		float[] matArr = new float[16];
		offsetMatrix.get(matArr);
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, matArr);
		return create(world, radius0, radius1, height, shapeID, matrix, scope);
	}
}
