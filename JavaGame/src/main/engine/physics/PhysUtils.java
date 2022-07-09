package main.engine.physics;

import crab.newton.NewtonMesh;
import main.engine.graphics.ModelData;

public final class PhysUtils {
	
	/**
	 * Converts a NewtonMesh into a ModelData object to be used by the rendering engine.
	 * @param mesh - NewtonMesh
	 * @return ModelData instance containing the data of the NewtonMesh. returns {@code null} if 
	 * NewtonMesh is unable to be turned into ModelData.
	 */
	public static ModelData convertToModelData(NewtonMesh mesh, String texturePath) {
		if (mesh.hasNormalChannel() && mesh.hasUV0Channel()) {
			int vertexCount = mesh.getPointCount();
			float[] vertexData = mesh.getVertexChannel(vertexCount);
			float[] normalData = mesh.getNormalChannel(vertexCount);
			float[] uvData = mesh.getUV0Channel(vertexCount);
			float[] tangentData = calcTangent(normalData, uvData);
			float[] biTangentData = calcBiTangent(normalData, uvData);
		}
		return null;
	}
	
	private static float[] calcTangent(float[] normal, float[] uv) {
		return null;
	}
	
	private static float[] calcBiTangent(float[] normal, float[] uv) {
		return null;
	}
}
