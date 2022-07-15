package main.engine.physics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joml.Vector3f;

import crab.newton.NewtonMesh;
import jdk.incubator.foreign.MemoryAddress;
import main.engine.graphics.ModelData;

public final class PhysUtils {
	
	/**
	 * Converts a NewtonMesh into a ModelData object to be used by the rendering engine.
	 * @param mesh - NewtonMesh
	 * @param modelId -
	 * @param texturePath -
	 * @return ModelData instance containing the data of the NewtonMesh. returns {@code null} if 
	 * NewtonMesh is unable to be turned into ModelData.
	 */
	public static ModelData convertToModelData(NewtonMesh mesh, String modelId, List<ModelData.Material> materialList) {
		if (mesh.hasNormalChannel() && mesh.hasUV0Channel()) {
			int vertexCount = mesh.getPointCount();
			float[] vertexData = mesh.getVertexChannel(vertexCount);
			float[] normalData = mesh.getNormalChannel(vertexCount);
			float[] uvData = mesh.getUV0Channel(vertexCount);
			
			List<ModelData.MeshData> meshDataList = new ArrayList<ModelData.MeshData>();
			
			MemoryAddress geometryHandle = mesh.beginHandle();
			for (int handle = mesh.firstMaterial(geometryHandle); handle != -1; handle = mesh.nextMaterial(geometryHandle, handle)) {
				int materialIdx = mesh.materialGetMaterial(geometryHandle, handle);
				int indexCount = mesh.materialGetIndexCount(geometryHandle, handle);
				
				int[] mIndices = mesh.materialGetIndexStream(geometryHandle, handle, indexCount), 
						newIndices = new int[indexCount];
				
				int mVertexCount = Arrays.stream(mIndices).distinct().toArray().length;
				
				float[] mVertex = new float[mVertexCount * 3], mNormal = new float[mVertexCount * 3], mTextCoords = new float[mVertexCount * 2],
						mTangent = new float[mVertexCount * 3], mBitTangent = new float[mVertexCount * 3];
				
				boolean[] checker = new boolean[vertexCount];
				int[] map = new int[vertexCount];
				int newIdx = 0;
				for (int i = 0; i < indexCount; i++) {
					int idx = mIndices[i];
					if (checker[idx]) {
						newIndices[i] = map[idx];
						continue;
					}
					mVertex[newIdx * 3] = vertexData[idx * 3];
					mVertex[(newIdx * 3) + 1] = vertexData[(idx * 3) + 1];
					mVertex[(newIdx * 3)+ 2] = vertexData[(idx * 3) + 2];
					
					mNormal[newIdx * 3] = normalData[idx * 3];
					mNormal[(newIdx * 3) + 1] = normalData[(idx * 3) + 1];
					mNormal[(newIdx * 3) + 2] = normalData[(idx * 3) + 2];
					
					mTextCoords[newIdx * 2] = uvData[idx * 2];
					mTextCoords[(newIdx * 2) + 1] = uvData[(idx * 2) + 1];
					
					newIndices[i] = newIdx;
					checker[idx] = true;
					map[idx] = newIdx;
					newIdx++;
				}
				
				for (int i = 0; i < newIndices.length; i += 3) {
					int p0 = newIndices[i], p1 = newIndices[i + 1], p2 = newIndices[i + 2];
					
					float vx, vy, vz, wx, wy, wz;
					vx = mVertex[p1] - mVertex[p0];
					vy = mVertex[p1 + 1] - mVertex[p0 + 1];
					vz = mVertex[p1 + 2] - mVertex[p0 + 2];
					
					wx = mVertex[p2] - mVertex[p0];
					wy = mVertex[p2 + 1] - mVertex[p0 + 1];
					wz = mVertex[p2 + 2] - mVertex[p0 + 2];
					
					float sx = mTextCoords[p1] - mTextCoords[p0], sy = mTextCoords[p1 + 1] - mTextCoords[p0 + 1];
					float tx = mTextCoords[p2] - mTextCoords[p0], ty = mTextCoords[p2 + 1] - mTextCoords[p0 + 1];
					float dirCorrection = (tx * sy - ty * sx) < 0.0f ? -1.0f : 1.0f;
					if (sx * ty == sy * tx) {
			            sx = 0.0f;
			            sy = 1.0f;
			            tx = 1.0f;
			            ty = 0.0f;
			        }
					
					float tangentX, tangentY, tangentZ, biTangentX, biTangentY, biTangentZ;
					tangentX = (wx * sy - vx * ty) * dirCorrection;
					tangentY = (wy * sy - vy * ty) * dirCorrection;
					tangentZ = (wz * sy - vz * ty) * dirCorrection;
					biTangentX = (- wx * sx + vx * tx) * dirCorrection;
					biTangentY = (- wy * sx + vy * tx) * dirCorrection;
					biTangentZ = (- wz * sx + vz * tx) * dirCorrection;
					
					Vector3f localTan = new Vector3f(), localBitan = new Vector3f(), 
							tangent = new Vector3f(tangentX, tangentY, tangentZ), biTangent = new Vector3f(biTangentX, biTangentY, biTangentZ), 
							normal = new Vector3f();
					for (int b = 0; b < 3; b++) {
						int p = newIndices[i + b];
						normal.x = mNormal[p];
						normal.y = mNormal[p + 1];
						normal.z = mNormal[p + 2];
						
						tangent.sub(normal.mul(tangent.mul(normal, localTan), localTan), localTan);
						biTangent.sub(normal.mul(biTangent.mul(normal, localBitan), localBitan), localBitan)
						.sub(localTan.mul(biTangent.mul(localTan, localBitan), localBitan));
						localTan.normalize();
						localBitan.normalize();
						
						boolean invalid_tangent = !localTan.isFinite();
						boolean invalid_bitangent = !localBitan.isFinite();
						if (invalid_tangent != invalid_bitangent) {
							if (invalid_tangent) {
								localTan.x = Float.intBitsToFloat(Float.floatToIntBits(normal.x) ^ Float.floatToIntBits(localBitan.x));
								localTan.y = Float.intBitsToFloat(Float.floatToIntBits(normal.y) ^ Float.floatToIntBits(localBitan.y));
								localTan.z = Float.intBitsToFloat(Float.floatToIntBits(normal.z) ^ Float.floatToIntBits(localBitan.z));
								localTan.normalize();
							} else {
								localBitan.x = Float.intBitsToFloat(Float.floatToIntBits(normal.x) ^ Float.floatToIntBits(localTan.x));
								localBitan.y = Float.intBitsToFloat(Float.floatToIntBits(normal.y) ^ Float.floatToIntBits(localTan.y));
								localBitan.z = Float.intBitsToFloat(Float.floatToIntBits(normal.z) ^ Float.floatToIntBits(localTan.z));
								localTan.normalize();
							}
						}
						
						mTangent[p] = localTan.x;
						mTangent[p + 1] = localTan.y;
						mTangent[p + 2] = localTan.z;
						mBitTangent[p] = localBitan.x;
						mBitTangent[p + 1] = localBitan.y;
						mBitTangent[p + 2] = localBitan.z;
					}
				}
				
				ModelData.MeshData mMesh = new ModelData.MeshData(mVertex, mNormal, mTangent, mBitTangent, mTextCoords, newIndices, materialIdx);
				meshDataList.add(mMesh);
			}
			
			return new ModelData(modelId, meshDataList, materialList);
		}
		return null;
	}
}
