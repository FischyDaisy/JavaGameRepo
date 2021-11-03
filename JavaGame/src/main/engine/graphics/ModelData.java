package main.engine.graphics;

import java.util.Arrays;
import java.util.List;

import org.joml.Vector4f;

import main.engine.graphics.opengl.Mesh;

public class ModelData {
    private List<MeshData> meshDataList;
    private List<Material> materialList;
    private String modelId;

    public ModelData(String modelId, List<MeshData> meshDataList, List<Material> materialList) {
        this.modelId = modelId;
        this.meshDataList = meshDataList;
        this.materialList = materialList;
    }

    public List<MeshData> getMeshDataList() {
        return meshDataList;
    }
    
    public List<Material> getMaterialList() {
        return materialList;
    }

    public String getModelId() {
        return modelId;
    }
    
    public record Material(String texturePath, Vector4f diffuseColor) {
        public static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        public Material() {
            this(null, DEFAULT_COLOR);
        }
    }

    public record MeshData(float[] positions, float[] textCoords, float[] normals, int[] indices, int[] jointIndices, float[] weights, int materialIdx) {
    	public static final int MAX_WEIGHTS = 4;
    	
    	public MeshData(float[] positions, float[] textCoords, float[] normals, int[] indices, int materialIdx) {
    		this(positions, textCoords, normals, indices, MeshData.createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), MeshData.createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0), materialIdx);
    	}
    	
    	public static float[] createEmptyFloatArray(int length, float defaultValue) {
            float[] result = new float[length];
            Arrays.fill(result, defaultValue);
            return result;
        }

        public static int[] createEmptyIntArray(int length, int defaultValue) {
            int[] result = new int[length];
            Arrays.fill(result, defaultValue);
            return result;
        }
    }
}