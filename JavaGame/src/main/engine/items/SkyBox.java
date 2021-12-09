package main.engine.items;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import main.engine.graphics.IRenderer;
import main.engine.graphics.ModelData;

public class SkyBox extends GameItem {

    public SkyBox(ModelData modelData, IRenderer renderer, String textureFile) throws Exception {
    	super("SkyBox", modelData.getModelId());
        renderer.loadSkyBox(modelData);
        setPosition(0, 0, 0);
    }
    
    public SkyBox(ModelData modelData, IRenderer renderer, Vector4f color) throws Exception {
        super("SkyBox", modelData.getModelId());
        renderer.loadSkyBox(modelData);
        setPosition(0, 0, 0);
    }
}