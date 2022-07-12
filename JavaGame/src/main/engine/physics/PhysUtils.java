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
					
					for (int b = 0; b < 3; b++) {
						int p = newIndices[i + b];
						
						float localTanX, localTanY, localTanZ, localBitanX, localBitanY, localBitanZ;
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
