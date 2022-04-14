package com.newton;

import org.joml.Matrix4f;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;
import main.engine.graphics.GraphConstants;

public class NewtonBox extends NewtonCollision {
	
	private NewtonBox(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision create(NewtonWorld world, float dx, float dy, float dz, int shapeID, Addressable offsetMatrix) {
		return new NewtonBox(Newton_h.NewtonCreateBox(world.address, dx, dy, dz, shapeID, offsetMatrix));
	}
	
	public static NewtonCollision create(NewtonWorld world, float dx, float dy, float dz, int shapeID, Matrix4f offsetMatrix) {
		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			MemorySegment matrix = MemorySegment.allocateNative(GraphConstants.MAT4X4_SIZE_BYTES, scope);
			float[] matArr = new float[16];
			offsetMatrix.get(matArr);
			for (int i = 0; i < matArr.length; i++) {
				MemoryAccess.setFloatAtIndex(matrix, i, matArr[i]);
			}
			return create(world, dx, dy, dz, shapeID, matrix);
		}
	}
}
