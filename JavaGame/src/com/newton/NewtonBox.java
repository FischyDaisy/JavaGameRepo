package com.newton;

import org.joml.Matrix4f;

import com.newton.generated.*;

import jdk.incubator.foreign.*;
import main.engine.graphics.GraphConstants;

public class NewtonBox extends NewtonCollision {
	
	protected NewtonBox(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonBox create(NewtonWorld world, float dx, float dy, float dz, int shapeID, Addressable offsetMatrix) {
		return new NewtonBox(Newton_h.NewtonCreateBox(world.address, dx, dy, dz, shapeID, offsetMatrix));
	}
	
	public static NewtonBox create(NewtonWorld world, float dx, float dy, float dz, int shapeID, Matrix4f offsetMatrix, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		float[] matArr = new float[16];
		offsetMatrix.get(matArr);
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, matArr);
		return create(world, dx, dy, dz, shapeID, matrix);
	}
}
