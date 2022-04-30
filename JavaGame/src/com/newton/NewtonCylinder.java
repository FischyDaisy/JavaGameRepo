package com.newton;

import org.joml.Matrix4f;

import com.newton.generated.Constants$root;
import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCylinder extends NewtonCollision {
	
	private NewtonCylinder(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision create(NewtonWorld world, float radio0,  float radio1,  float height,  int shapeID,  Addressable offsetMatrix) {
		return new NewtonCylinder(Newton_h.NewtonCreateCylinder(world.address, radio0, radio1, height, shapeID, offsetMatrix));
	}
	
	public static NewtonCollision create(NewtonWorld world, float radio0,  float radio1,  float height,  int shapeID,  Matrix4f offsetMatrix, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		float[] matArr = new float[16];
		offsetMatrix.get(matArr);
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, matArr);
		return create(world, radio0, radio1, height, shapeID, matrix);
	}
}
