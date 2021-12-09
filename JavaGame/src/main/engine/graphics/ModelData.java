package main.engine.graphics;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joml.Vector4f;
import org.joml.primitives.AABBf;

import main.engine.graphics.animation.Animation;
import main.engine.graphics.opengl.Mesh;

public class ModelData {
    private List<MeshData> meshDataList;
    private List<Material> materialList;
    private Map<String, Animation> animations;
    private String modelId;

    public ModelData(String modelId, List<MeshData> meshDataList, List<Material> materialList) {
        this.modelId = modelId;
        this.meshDataList = meshDataList;
        this.materialList = materialList;
        animations = null;
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
    
    public Map<String, Animation> getAnimations() {
    	return animations;
    }
    
    public void setAnimations(Map<String, Animation> animations) {
    	this.animations = animations;
    }
    
    public record Material(String texturePath, Vector4f ambientColor, Vector4f diffuseColor, Vector4f specularColor, Vector4f reflectance, 
    		int cols, int rows) {
        public static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        public Material() {
            this(null, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, 1, 1);
        }
        
        public Material(String filePath) {
        	this(filePath, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, 1, 1);
        }
        
        public Material(String filePath, int cols, int rows) {
        	this(filePath, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, cols, rows);
        }
        
        public Material(Vector4f color) {
        	this(null, color, color, color, DEFAULT_COLOR, 1, 1);
        }
        
        public Material(Vector4f color, float reflectance) {
        	this(null, color, color, color, new Vector4f(reflectance), 1, 1);
        }
    }

    public record MeshData(float[] positions, float[] textCoords, float[] normals, int[] indices, int[] jointIndices, float[] weights, int materialIdx, AABBf boundingBox) {
    	public static final int MAX_WEIGHTS = 4;
    	
    	public MeshData(float[] positions, float[] textCoords, float[] normals, int[] indices, int materialIdx, AABBf boundingBox) {
    		this(positions, textCoords, normals, indices, MeshData.createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), MeshData.createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0), materialIdx, boundingBox);
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