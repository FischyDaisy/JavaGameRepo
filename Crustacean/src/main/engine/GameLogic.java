package main.engine;

import main.engine.graphics.vulkan.VKRenderer;
import main.engine.physics.Physics;
import main.engine.scene.Scene;
import main.engine.sound.SoundManager;

public interface GameLogic {
	
	void initialize(Window window, Scene scene, VKRenderer renderer,
                    Physics physics, SoundManager soundManager) throws Throwable;
    
    void inputAndUpdate(Window window, Scene scene, VKRenderer renderer,
                        Physics physics, SoundManager soundManager, long diffTimeNanos) throws Throwable;
    
    void cleanup();
}