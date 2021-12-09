package com.newton;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;
import main.engine.graphics.GraphConstants;

public class Newton {
	
	public static final Map<Integer, Integer> ctr = createColToRowMap();
	
	private Newton() {}
	
	/**
	 * Converts Matrix4f objects into memory segment for passing to Newton library
	 * @param matrix - matrix to pass to Newton. Should be in row major order.
	 * @param scope - resource scope for allocating memory segment
	 * @return memory segment filled with values of matrix array
	 */
	public static MemorySegment createMatrixSegment(float[] matrix, ResourceScope scope) {
		MemorySegment matrixSegment = MemorySegment.allocateNative(GraphConstants.MAT4X4_SIZE_BYTES, scope);
		for (int i = 0; i < matrix.length; i++) {
			MemoryAccess.setFloatAtIndex(matrixSegment, i, matrix[i]);
		}
		return matrixSegment;
	}
	
	public static MemorySegment createMatrixSegment(ResourceScope scope) {
		MemorySegment matrixSegment = MemorySegment.allocateNative(GraphConstants.MAT4X4_SIZE_BYTES, scope);
		return matrixSegment;
	}
	
	/**
	 * Converts Vector3f objects into MemorySegments for passing to Newton library
	 * @param vector - float array with vector values
	 * @param scope - ResourceScope for allocating memory
	 * @return MemorySegment with vector3 values in the segment
	 */
	public static MemorySegment createVector3fSegment(float[] vector, ResourceScope scope) {
		MemorySegment vectorSegment = MemorySegment.allocateNative(GraphConstants.VECTOR3F_SIZE_BYTES, scope);
		for (int i = 0; i < vector.length; i++) {
			MemoryAccess.setFloatAtIndex(vectorSegment, i, vector[i]);
		}
		return vectorSegment;
	}
	
	public static MemorySegment createVector3fSegment(ResourceScope scope) {
		MemorySegment vectorSegment = MemorySegment.allocateNative(GraphConstants.VECTOR3F_SIZE_BYTES, scope);
		return vectorSegment;
	}
	
	/**
	 * Converts
	 * @param vector
	 * @param scope
	 * @return
	 */
	public static MemorySegment createVector4fSegment(float[] vector, ResourceScope scope) {
		MemorySegment vectorSegment = MemorySegment.allocateNative(GraphConstants.VECTOR4F_SIZE_BYTES, scope);
		for (int i = 0; i < vector.length; i++) {
			MemoryAccess.setFloatAtIndex(vectorSegment, i, vector[i]);
		}
		return vectorSegment;
	}
	
	public static MemorySegment createVector4fSegment(ResourceScope scope) {
		MemorySegment vectorSegment = MemorySegment.allocateNative(GraphConstants.VECTOR4F_SIZE_BYTES, scope);
		return vectorSegment;
	}
	
	public static MemorySegment createQuaternionSegment(float[] quat, ResourceScope scope) {
		MemorySegment quatSegment = MemorySegment.allocateNative(GraphConstants.QUATF_SIZE_BYTES, scope);
		for (int i = 0; i < quat.length; i++) {
			MemoryAccess.setFloatAtIndex(quatSegment, i, quat[i]);
		}
		return quatSegment;
	}
	
	public static MemorySegment createQuaternionSegment(ResourceScope scope) {
		MemorySegment quatSegment = MemorySegment.allocateNative(GraphConstants.QUATF_SIZE_BYTES, scope);
		return quatSegment;
	}
	
	public static MemorySegment createFloatSegment(float f, ResourceScope scope) {
		MemorySegment floatSegment = MemorySegment.allocateNative(GraphConstants.FLOAT_SIZE_BYTES, scope);
		MemoryAccess.setFloatAtIndex(floatSegment, 0, f);
		return floatSegment;
	}
	
	public static MemorySegment createFloatSegment(ResourceScope scope) {
		MemorySegment floatSegment = MemorySegment.allocateNative(GraphConstants.FLOAT_SIZE_BYTES, scope);
		return floatSegment;
	}
	
	/**
	 * Converts a memory segment into a float array
	 * @param segment - memory segment holding float array
	 * @return float array.
	 */
	public static float[] segmentToFloatArray(MemorySegment segment) {
		float[] arr = segment.toFloatArray();
		return arr;
	}
	
	public static float[] vector3fToFloatArray(Vector3f vector) {
		float[] arr = new float[] {
				vector.x(),
				vector.y(),
				vector.z()
		};
		return arr;
	}
	
	/**
	 * Converts order of matrix in the array to and from row to col major and vice versa.
	 * @param matrix - A 4x4 matrix in either row-major or col-major order
	 * @return matrix - A 4x4 matrix of the opposite order
	 */
	public static float[] convertOrder(float[] matrix) {
		if(matrix.length != 16) {
			throw new RuntimeException("Not a 4x4 Matrix");
		}
		float[] result = new float[16];
		for (int i = 0; i < matrix.length; i++) {
			if (i == 0 || i == 5 || i == 10 || i == 15) {
				result[i] = matrix[i];
				continue;
			}
			result[i] = matrix[ctr.get(i)];
			
		}
		return result;
	}
	
	/**
	 * Internal method to create the conversion mapping
	 * @return the conversion map
	 */
	private static final Map<Integer, Integer> createColToRowMap() {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(1, 4);
		map.put(2, 8);
		map.put(3, 12);
		map.put(4, 1);
		map.put(6, 9);
		map.put(7, 13);
		map.put(8, 2);
		map.put(9, 6);
		map.put(11, 14);
		map.put(12, 3);
		map.put(13, 7);
		map.put(14, 11);
		return map;
	}
}
