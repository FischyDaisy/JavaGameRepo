package main.engine;

import main.engine.graphics.vulkan.VKRenderer;

public interface IGameLogic {
	
	void init(Window window, Scene scene, VKRenderer renderer) throws Exception;
    
    void inputAndUpdate(Window window, Scene scene, long diffTimeNanos);
    
    void cleanup();
}