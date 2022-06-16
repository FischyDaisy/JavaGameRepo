package com.newton;

import java.util.Objects;

import com.newton.generated.*;

import jdk.incubator.foreign.*;

public class NewtonHeightField extends NewtonCollision {
	
	protected NewtonHeightField(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonHeightField create(NewtonWorld world, int width,  int height,  int gridsDiagonals,  int elevationdatType,  Object elevationMap,  String attributeMap,  float verticalScale,  float horizontalScale_x,  float horizontalScale_z,  int shapeID, SegmentAllocator allocator) {
		Objects.requireNonNull(elevationMap);
		MemorySegment elevationSeg;
		if (!elevationMap.getClass().isArray()) {
			throw new IllegalArgumentException();
		} else {
			switch (elevationdatType) {
				case 0 -> elevationSeg = allocator.allocateArray(Newton_h.C_FLOAT, (float[]) elevationMap);
				case 1 -> elevationSeg = allocator.allocateArray(Newton_h.C_SHORT, (short[]) elevationMap);
				default -> throw new IllegalArgumentException();
			}
		}
		MemorySegment attributeSeg = allocator.allocateUtf8String(attributeMap);
		return new NewtonHeightField(Newton_h.NewtonCreateHeightFieldCollision(world.address, width, height, gridsDiagonals, elevationdatType, elevationSeg, attributeSeg, verticalScale, horizontalScale_x, horizontalScale_z, shapeID));
	}
	
	public void setUserRaycastCallback(NewtonHeightFieldRayCastCallback rayHitCallback, ResourceScope scope) {
		NativeSymbol rayHitCallbackFunc = NewtonHeightFieldRayCastCallback.allocate(rayHitCallback, scope);
		Newton_h.NewtonHeightFieldSetUserRayCastCallback(address, rayHitCallbackFunc);
	}
}
