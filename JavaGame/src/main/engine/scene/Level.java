package main.engine.scene;

import crab.newton.NewtonWorld;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.physics.Physics;
import jdk.incubator.foreign.*;

/**
 * Interface for creating levels
 * 
 * A Level is a class that contains all the necessary objects to be passed to the Scene, Renderer and NewtonWorld.
 * The level can be used to create classes or can simply just be a lambda expression. In the future a way to generate
 * Levels from external files will be developed.
 * 
 * @author Christopher
 *
 */
public interface Level {
	/**
	 * Loads the level into the engine
	 * @param scne 
	 * @param vkRenderer
	 * @param world
	 * @param scope
	 */
	public void load(Scene scene, VKRenderer renderer, NewtonWorld world, Physics physics, ResourceScope scope) throws Exception;
}
