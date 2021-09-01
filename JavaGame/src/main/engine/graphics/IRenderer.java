package main.engine.graphics;

import main.engine.Scene;
import main.engine.Window;

public interface IRenderer {
	
	public void init(Window window, Scene scene) throws Exception;
	
	public void render(Window window, Camera camera, Scene scene);
	
	public void cleanup();
}
