package main.engine.items;

import org.joml.Vector4f;

import main.engine.Scene;
import main.engine.Window;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.VKRenderer;

public class SkyBox extends GameItem {
    public SkyBox(ModelData modelData, Window window, Scene scene, VKRenderer renderer) throws Exception {
    	super("SkyBox", modelData.getModelId());
    	scene.addGameItem(this);
        renderer.loadSkyBox(modelData, window, scene);
    }
}