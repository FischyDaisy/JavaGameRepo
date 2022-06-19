package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonMesh {
	
	protected final MemoryAddress address;
	
	protected NewtonMesh(MemoryAddress address) {
		this.address = address;
	}
	
	public static NewtonMesh create(NewtonWorld world) {
		return new NewtonMesh(Newton_h.NewtonMeshCreate(world.address));
	}
	
	public static NewtonMesh createFromMesh(NewtonMesh mesh) {
		return new NewtonMesh(Newton_h.NewtonMeshCreateFromMesh(mesh.address));
	}
	
	public static NewtonMesh createFromCollision(NewtonCollision collision) {
		return new NewtonMesh(Newton_h.NewtonMeshCreateFromCollision(collision.address));
	}
	
	public static NewtonMesh createTetrahedraIsoSurface(NewtonMesh mesh) {
		return new NewtonMesh(Newton_h.NewtonMeshCreateTetrahedraIsoSurface(mesh.address));
	}
	
	public static NewtonMesh createConvexHull(NewtonWorld world, int pointCount, float[] vertexCloud, int strideInBytes, float tolerance, 
			SegmentAllocator allocator) {
		MemorySegment vertCloud = allocator.allocateArray(Newton_h.C_FLOAT, vertexCloud);
		return new NewtonMesh(Newton_h.NewtonMeshCreateConvexHull(world.address, pointCount, vertCloud, strideInBytes, tolerance));
	}
}
