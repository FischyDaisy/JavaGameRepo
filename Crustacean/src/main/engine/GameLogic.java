package main.engine;

import dev.dominion.ecs.api.Dominion;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.physics.Physics;
import main.engine.scene.Scene;
import main.engine.scene.SceneBuilder;
import main.engine.sound.SoundManager;

public interface GameLogic {
	
	void initialize(Window window, SceneBuilder builder, VKRenderer renderer,
                    Physics physics, SoundManager soundManager) throws Throwable;
    
    void inputAndUpdate(Window window, Dominion dominion, VKRenderer renderer,
                        Physics physics, SoundManager soundManager) throws Throwable;
    
    void cleanup();
}