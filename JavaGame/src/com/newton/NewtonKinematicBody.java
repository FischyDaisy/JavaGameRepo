package com.newton;

import org.joml.Matrix4f;

import com.newton.generated.Constants$root;
import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonKinematicBody extends NewtonBody {
	
	protected NewtonKinematicBody(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonBody create(NewtonWorld world, NewtonCollision collision, Addressable matrix) {
		return new NewtonKinematicBody(Newton_h.NewtonCreateKinematicBody(world.address, collision.address, matrix));
	}
	
	public static NewtonBody create(NewtonWorld world, NewtonCollision collision, Matrix4f matrix, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		float[] matArr = new float[16];
		matrix.get(matArr);
		MemorySegment matrixSegment = allocator.allocateArray(Newton_h.C_FLOAT, matArr);
		return create(world, collision, matrixSegment);
	}
	
	protected static NewtonBody wrapImpl(MemoryAddress address) {
		return new NewtonKinematicBody(address);
	}
}
