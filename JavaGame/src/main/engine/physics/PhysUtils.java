package main.engine.physics;

import java.util.ArrayList;
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
	public static ModelData convertToModelData(NewtonMesh mesh, String modelId, String texturePath) {
		if (mesh.hasNormalChannel() && mesh.hasUV0Channel()) {
			int vertexCount = mesh.getPointCount();
			float[] vertexData = mesh.getVertexChannel(vertexCount);
			float[] normalData = mesh.getNormalChannel(vertexCount);
			float[] uvData = mesh.getUV0Channel(vertexCount);
			float[] tangentData = new float[vertexCount * 3];
			float[] biTangentData = new float[vertexCount * 3];
			int[] indicesData = mesh.getIndexToVertexMap(vertexCount);
			calcTangent(vertexData, normalData, uvData, tangentData, biTangentData, indicesData, vertexCount);
			
			MemoryAddress geometryHandle = mesh.beginHandle();
			for (int handle = mesh.firstMaterial(geometryHandle); handle != -1; handle = mesh.nextMaterial(geometryHandle, handle)) {
				int materialIdx = mesh.materialGetMaterial(geometryHandle, handle);
				int indexCount = mesh.materialGetIndexCount(geometryHandle, handle);
			}
			
			ModelData.MeshData meshData = new ModelData.MeshData(vertexData, normalData, tangentData, biTangentData, 
					uvData, indicesData, 0);
			ModelData.Material material = new ModelData.Material(texturePath);
			List<ModelData.MeshData> meshDataList = new ArrayList<ModelData.MeshData>();
			List<ModelData.Material> materialList = new ArrayList<ModelData.Material>();
			meshDataList.add(meshData);
			materialList.add(material);
			return new ModelData(modelId, meshDataList, materialList);
		}
		return null;
	}
	
	private static void calcTangent(float[] vertex, float[] normals, float[] textCoords, float[] tangents, float[] biTangents, int[] indices, int indexCount) {
		Vector3f tangent = new Vector3f(), biTangent = new Vector3f(), localTan = new Vector3f(), localBiTan = new Vector3f();
		
		for (int idx = 0; idx < indexCount; idx += 3) {
			int p0 = (idx * 3) + 0, p1 = (idx * 3) + 3, p2 = (idx * 3) + 6;
			float vx, vy, vz, wx, wy, wz;
			vx = vertex[p1] - vertex[p0];
			vy = vertex[p1 + 1] - vertex[p0 + 1];
			vz = vertex[p1 + 2] - vertex[p0 + 2];
			
			wx = vertex[p2] - vertex[p0];
			wy = vertex[p2 + 1] - vertex[p0 + 1];
			wz = vertex[p2 + 2] - vertex[p0 + 2];
		}
	}
}
