package main.engine.items;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import main.engine.graphics.ModelData;
import main.engine.graphics.opengl.GLRenderer;
import main.engine.graphics.vulkan.VKRenderer;

public class SkyBox extends GameItem {

    public SkyBox(ModelData modelData, GLRenderer renderer, String textureFile) throws Exception {
    	super("SkyBox", modelData.getModelId());
        renderer.loadSkyBox(modelData);
        setPosition(0, 0, 0);
    }
    
    public SkyBox(ModelData modelData, GLRenderer renderer, Vector4f color) throws Exception {
        super("SkyBox", modelData.getModelId());
        renderer.loadSkyBox(modelData);
        setPosition(0, 0, 0);
    }
    
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