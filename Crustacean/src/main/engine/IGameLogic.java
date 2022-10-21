package main.engine;

import main.engine.graphics.vulkan.VKRenderer;
import main.engine.scene.Scene;

public interface IGameLogic {
	
	void init(Window window, Scene scene, VKRenderer renderer) throws Exception;
    
    void inputAndUpdate(Window window, Scene scene, long diffTimeNanos);
    
    void cleanup();
}