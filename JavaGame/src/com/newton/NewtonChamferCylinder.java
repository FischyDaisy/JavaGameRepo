package com.newton;

import org.joml.Matrix4f;

import com.newton.generated.Constants$root;
import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonChamferCylinder extends NewtonCollision {
	
	protected NewtonChamferCylinder(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision create(NewtonWorld world, float radius,  float height,  int shapeID,  Addressable offsetMatrix) {
		return new NewtonChamferCylinder(Newton_h.NewtonCreateChamferCylinder(world.address, radius, height, shapeID, offsetMatrix));
	}
	
	public static NewtonCollision create(NewtonWorld world, float radius,  float height,  int shapeID,  Matrix4f offsetMatrix, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		float[] matArr = new float[16];
		offsetMatrix.get(matArr);
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, matArr);
		return create(world, radius, height, shapeID, matrix);
	}
}
