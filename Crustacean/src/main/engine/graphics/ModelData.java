package main.engine.graphics;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.joml.primitives.AABBf;

public class ModelData {
	private List<AnimMeshData> animMeshDataList;
    private List<Animation> animationsList;
    private List<MeshData> meshDataList;
    private List<Material> materialList;
    private String modelId;

    public ModelData(String modelId, List<MeshData> meshDataList, List<Material> materialList) {
        this.modelId = modelId;
        this.meshDataList = meshDataList;
        this.materialList = materialList;
    }
    
    public List<AnimMeshData> getAnimMeshDataList() {
        return animMeshDataList;
    }

    public List<Animation> getAnimationsList() {
        return animationsList;
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
    
    public boolean hasAnimations() {
        return animationsList != null && !animationsList.isEmpty();
    }

    public void setAnimMeshDataList(List<AnimMeshData> animMeshDataList) {
        this.animMeshDataList = animMeshDataList;
    }

    public void setAnimationsList(List<Animation> animationsList) {
        this.animationsList = animationsList;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ModelData modelData &&
                this.modelId.equals(modelData.modelId);
    }

    public record AnimMeshData(float[] weights, float[] boneIds) {}

    public record AnimatedFrame(Matrix4f[] jointMatrices) {}

    public record Animation(String name, double duration, List<AnimatedFrame> frames) {}
    
    public record Material(String texturePath, String normalMapPath, String metalRoughMap, Vector4f diffuseColor, float roughnessFactor, float metallicFactor, 
    		int cols, int rows) {
        public static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        public Material() {
            this("", "", "", DEFAULT_COLOR, 0.0f, 0.0f, 1, 1);
        }
        
        public Material(String filePath) {
        	this(filePath, "", "", DEFAULT_COLOR, 0.0f, 0.0f, 1, 1);
        }
        
        public Material(String filePath, int cols, int rows) {
        	this(filePath, "", "", DEFAULT_COLOR, 0.0f, 0.0f, cols, rows);
        }
        
        public Material(String filePath, float roughnessFactor, float metllicaFactor) {
        	this(filePath, "", "", DEFAULT_COLOR, roughnessFactor, metllicaFactor, 1, 1);
        }
        
        public Material(Vector4f color) {
        	this("", "", "", color, 0.0f, 0.0f, 1, 1);
        }
        
        public Material(Vector4f color, int cols, int rows) {
        	this("", "", "", color, 0.0f, 0.0f, cols, rows);
        }
        
        public Material(Vector4f color, float roughnessFactor, float metallicaFactor) {
        	this("", "", "", color, roughnessFactor, metallicaFactor, 1, 1);
        }
    }

    public record MeshData(float[] positions, float[] normals, float[] tangents, float[] biTangents, float[] textCoords, int[] indices, int materialIdx) {}
}