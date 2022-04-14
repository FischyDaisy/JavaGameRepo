package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonConvexHull extends NewtonCollision {
	
	private NewtonConvexHull(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision createConvexHullCollision(NewtonWorld world, int count,  Addressable vertexCloud,  int strideInBytes,  float tolerance,  int shapeID,  Addressable offsetMatrix) {
		return new NewtonConvexHull(Newton_h.NewtonCreateConvexHull(world.address, count, vertexCloud, strideInBytes, tolerance, shapeID, offsetMatrix));
	}
}
