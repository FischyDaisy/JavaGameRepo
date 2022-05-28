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
	
	public int getType() {
		return Newton_h.NewtonCollisionGetType(address);
	}
	
	public boolean isConvex() {
		return Newton_h.NewtonCollisionIsConvexShape(address) == 1 ? true : false;
	}
	
	public boolean isStatic() {
		return Newton_h.NewtonCollisionIsStaticShape(address) == 1 ? true : false;
	}
	
	public void setUserData(Addressable data) {
		Newton_h.NewtonCollisionSetUserData(address, data);
	}
	
	public MemoryAddress getUserData() {
		return Newton_h.NewtonCollisionGetUserData(address);
	}
	
	public void setUserID(long id) {
		Newton_h.NewtonCollisionSetUserID(address, id);
	}
	
	public long getUserID() {
		return Newton_h.NewtonCollisionGetUserID(address);
	}
	
	public MemoryAddress getSubCollisionHandle() {
		return Newton_h.NewtonCollisionGetSubCollisionHandle(address);
	}
	
	public NewtonCollision getParentInstance() {
		MemoryAddress ptr = Newton_h.NewtonCollisionGetParentInstance(address);
		return ptr.equals(MemoryAddress.NULL) ? null : NewtonCollision.wrap(ptr);
	}
	
	public void setMatrix(float[] matrix, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment matrixSegment = allocator.allocateArray(Newton_h.C_FLOAT, matrix);
		Newton_h.NewtonCollisionSetMatrix(address, matrixSegment);
	}
	
	public void setMatrix(float[] matrix, SegmentAllocator allocator) {
		MemorySegment matrixSegment = allocator.allocateArray(Newton_h.C_FLOAT, matrix);
		Newton_h.NewtonCollisionSetMatrix(address, matrixSegment);
	}
	
	public float[] getMatrix(ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment matrixSegment = allocator.allocateArray(Newton_h.C_FLOAT, new float[16]);
		Newton_h.NewtonCollisionGetMatrix(address, matrixSegment);
		return matrixSegment.toArray(Newton_h.C_FLOAT);
	}
	
	public float[] getMatrix(SegmentAllocator allocator) {
		MemorySegment matrixSegment = allocator.allocateArray(Newton_h.C_FLOAT, new float[16]);
		Newton_h.NewtonCollisionGetMatrix(address, matrixSegment);
		return matrixSegment.toArray(Newton_h.C_FLOAT);
	}
	
	public void setScale(float x, float y, float z) {
		Newton_h.NewtonCollisionSetScale(address, x, y, z);
	}
	
	public float[] getScale(ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment xyzSeg = allocator.allocateArray(Newton_h.C_FLOAT, new float[3]);
		Newton_h.NewtonCollisionGetScale(address, 
				xyzSeg.asSlice(0L, Newton_h.C_FLOAT.byteSize()), 
				xyzSeg.asSlice(4L, Newton_h.C_FLOAT.byteSize()), 
				xyzSeg.asSlice(8L, Newton_h.C_FLOAT.byteSize()));
		return xyzSeg.toArray(Newton_h.C_FLOAT);
	}
	
	public float[] getScale(SegmentAllocator allocator) {
		MemorySegment xyzSeg = allocator.allocateArray(Newton_h.C_FLOAT, new float[3]);
		Newton_h.NewtonCollisionGetScale(address, 
				xyzSeg.asSlice(0L, Newton_h.C_FLOAT.byteSize()), 
				xyzSeg.asSlice(4L, Newton_h.C_FLOAT.byteSize()), 
				xyzSeg.asSlice(8L, Newton_h.C_FLOAT.byteSize()));
		return xyzSeg.toArray(Newton_h.C_FLOAT);
	}
	
	public void destroy() {
		Newton_h.NewtonDestroyCollision(address);
	}
	
	public float getSkinThickness() {
		return Newton_h.NewtonCollisionGetSkinThickness(address);
	}
	
	public void setSkinThickness(float thickness) {
		Newton_h.NewtonCollisionSetSkinThickness(address, thickness);
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