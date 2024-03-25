package main.engine.scene;

import crab.newton.NewtonWorld;
import dev.dominion.ecs.api.Dominion;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.physics.Physics;
import java.lang.foreign.*;

/**
 * Interface for creating levels
 * 
 * A Level is a class that contains all the necessary objects to be passed to the Scene, Renderer and NewtonWorld.
 * In the future a way to generate Levels from external files will be developed.
 * 
 * @author Christopher
 *
 */
public interface Level {
	/**
	 * Loads the level into the engine
	 * @param dominion
	 * @param renderer
	 * @param world
	 * @param physics
	 * @param session
	 */
	void load(Scene scene, VKRenderer renderer, Physics physics) throws Exception;

	void reset(Scene scene, VKRenderer renderer, Physics physics) throws Exception;
}
