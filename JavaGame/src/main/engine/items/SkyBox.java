package main.engine.items;

import org.joml.Vector4f;

import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.VKRenderer;

public class SkyBox extends GameItem {
    public SkyBox(ModelData modelData, VKRenderer renderer, String textureFile) throws Exception {
    	super("SkyBox", modelData.getModelId());
        renderer.loadSkyBox(modelData);
        setPosition(0, 0, 0);
    }
    
    public SkyBox(ModelData modelData, VKRenderer renderer, Vector4f color) throws Exception {
        super("SkyBox", modelData.getModelId());
        renderer.loadSkyBox(modelData);
        setPosition(0, 0, 0);
    }
}