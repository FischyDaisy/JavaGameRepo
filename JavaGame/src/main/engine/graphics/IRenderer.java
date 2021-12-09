package main.engine.graphics;

import java.util.List;

import main.engine.Scene;
import main.engine.Window;
import main.engine.graphics.camera.Camera;

public interface IRenderer {
	
	public void init(Window window, Scene scene) throws Exception;
	
	public void render(Window window, Camera camera, Scene scene);
	
	public void cleanup();
	
	public void loadModels(List<ModelData> modelDataList) throws Exception;
	
	public void loadSkyBox(ModelData skyBox) throws Exception;
	
	public void loadParticles(List<ModelData> modelDataList, int maxParticles) throws Exception;
}
