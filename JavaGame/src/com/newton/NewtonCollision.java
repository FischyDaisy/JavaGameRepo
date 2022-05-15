package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public abstract class NewtonCollision {
	
	protected final MemoryAddress address;
	
	protected NewtonCollision(MemoryAddress address) {
		this.address = address;
	}
	
	public int getMode() {
		return Newton_h.NewtonCollisionGetMode(address);
	}
	
	public void setMode(int mode) {
		Newton_h.NewtonCollisionSetMode(address, mode);
	}
	
	public float calculateVolume() {
		return Newton_h.NewtonConvexCollisionCalculateVolume(address);
	}
	
	public void calculateInertiaMatrix(float[] inertia, float[] origin, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment inertiaSegment = allocator.allocateArray(Newton_h.C_FLOAT, new float[] {0f, 0f, 0f});
		MemorySegment originSegment = allocator.allocateArray(Newton_h.C_FLOAT, new float[] {0f, 0f, 0f});
		Newton_h.NewtonConvexCollisionCalculateInertialMatrix(address, inertiaSegment, originSegment);
		MemorySegment.copy(inertiaSegment, Newton_h.C_FLOAT, 0, inertia, 0, 3);
		MemorySegment.copy(originSegment, Newton_h.C_FLOAT, 0, origin, 0, 3);
	}
	
	public void destroy() {
		Newton_h.NewtonDestroyCollision(address);
	}
	
	protected static NewtonCollision wrap(MemoryAddress address) {
		int collisionType = Newton_h.NewtonCollisionGetType(address);
		return switch (collisionType) {
			case 0 -> new NewtonSphere(address);
			case 1 -> new NewtonCapsule(address);
			case 2 -> new NewtonCylinder(address);
			case 3 -> new NewtonChamferCylinder(address);
			case 4 -> new NewtonBox(address);
			case 5 -> new NewtonCone(address);
			case 6 -> new NewtonConvexHull(address);
			case 7 -> new NewtonNull(address);
			case 8 -> new NewtonCompoundCollision(address);
			case 9 -> new NewtonTreeCollision(address);
			case 10 -> new NewtonHeightField(address);
			default -> throw new RuntimeException("Error wrapping MemoryAddress");
		};
	}
}