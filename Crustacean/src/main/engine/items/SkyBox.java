package main.engine.items;

import org.joml.*;

import main.engine.EngineProperties;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.VKTexture;

public class SkyBox  {
	
	private final String[] skyboxTexturePaths;
	
	private String modelId;
	private final Quaternionf rotation;
	private final Vector3f scale;
	private final Matrix4f modelMatrix;
	
    public SkyBox(ModelData modelData) throws Exception {
    	EngineProperties engProps = EngineProperties.INSTANCE;
    	skyboxTexturePaths = new String[engProps.getMaxSkyboxTextures()];
    	int offset = 0;
    	for (ModelData.Material material : modelData.getMaterialList()) {
    		skyboxTexturePaths[offset++] = material.texturePath();
    		skyboxTexturePaths[offset++] = material.normalMapPath();
    		skyboxTexturePaths[offset++] = material.metalRoughMap();
    	}
    	rotation = new Quaternionf();
    	scale = new Vector3f();
    	modelMatrix = new Matrix4f();
    	modelId = modelData.getModelId();
    }
    
    public boolean isSkyboxTexture(VKTexture texture) {
    	for (String texturePath : skyboxTexturePaths) {
    		if (texturePath != null && texturePath.equals(texture.getFileName())) return true;
    	}
    	return false;
    }
    
    public String[] gettexturePaths() {
    	return skyboxTexturePaths;
    }
    
    public String getModelId() {
    	return modelId;
    }
    
    public Quaternionf getRotation() {
    	return rotation;
    }
    
    public void setRotation(Quaternionf rotation) {
    	this.rotation.set(rotation);
    }
    
    public Vector3f getScale() {
    	return scale;
    }
    
    public void setScale(Vector3f scale) {
    	this.scale.set(scale);
    }
    
    public void setScale(float scale) {
    	this.scale.set(scale);
    }
    
    public void setScale(float x, float y, float z) {
    	this.scale.set(x, y, z);
    }
    
    public void setScaleX(float x) {
    	this.scale.x = x;
    }
    
    public void setScaleY(float y) {
    	this.scale.y = y;
    }
    
    public void setScaleZ(float z) {
    	this.scale.z = z;
    }
    
    public Matrix4f getModelMatrix() {
    	return modelMatrix;
    }
    
    public Matrix4f buildModelMatrix() {
    	return modelMatrix.translationRotateScale(
    			0, 0, 0, 
    			rotation.x, rotation.y, rotation.z, rotation.w, 
    			scale.x, scale.y, scale.z);
    }
}