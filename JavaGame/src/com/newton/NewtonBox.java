package com.newton;

import org.joml.Matrix4f;

import com.newton.generated.*;

import jdk.incubator.foreign.*;
import main.engine.graphics.GraphConstants;

public class NewtonBox extends NewtonCollision {
	
	private NewtonBox(MemoryAddress address) {
		super(address, ResourceScope.newConfinedScope());
	}
	
	private NewtonBox(MemoryAddress address, ResourceScope scope) {
		super(address, scope);
	}
	
	public static NewtonCollision create(NewtonWorld world, float dx, float dy, float dz, int shapeID, Addressable offsetMatrix) {
		return new NewtonBox(Newton_h.NewtonCreateBox(world.address, dx, dy, dz, shapeID, offsetMatrix));
	}
	
	public static NewtonCollision create(NewtonWorld world, float dx, float dy, float dz, int shapeID, Addressable offsetMatrix, ResourceScope scope) {
		return new NewtonBox(Newton_h.NewtonCreateBox(world.address, dx, dy, dz, shapeID, offsetMatrix), scope);
	}
	
	public static NewtonCollision create(NewtonWorld world, float dx, float dy, float dz, int shapeID, Matrix4f offsetMatrix) {
		ResourceScope scope = ResourceScope.newConfinedScope();
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		float[] matArr = new float[16];
		offsetMatrix.get(matArr);
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, matArr);
		return create(world, dx, dy, dz, shapeID, matrix, scope);
	}
	
	public static NewtonCollision create(NewtonWorld world, float dx, float dy, float dz, int shapeID, Matrix4f offsetMatrix, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		float[] matArr = new float[16];
		offsetMatrix.get(matArr);
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, matArr);
		return create(world, dx, dy, dz, shapeID, matrix);
	}
}
