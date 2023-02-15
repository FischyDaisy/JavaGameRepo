package main.engine;

import dev.dominion.ecs.api.Dominion;
import main.engine.graphics.vulkan.VKRenderer;

public interface IGameLogic {
	
	void init(Window window, Dominion dominion, VKRenderer renderer) throws Exception;
    
    void inputAndUpdate(Window window, Dominion dominion, VKRenderer renderer, long diffTimeNanos) throws Exception;
    
    void cleanup();
}