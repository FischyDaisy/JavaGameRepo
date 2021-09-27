package main.engine.graphics;

import main.engine.Scene;
import main.engine.Window;
import main.engine.graphics.camera.Camera;

public interface IRenderer {
	
	public void init(Window window, Scene scene) throws Exception;
	
	public void render(Window window, Camera camera, Scene scene);
	
	public void cleanup();
}
