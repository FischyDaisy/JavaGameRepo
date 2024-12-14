package main.engine.scripts;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import main.engine.Window;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.physics.Physics;
import main.engine.scene.Scene;
import main.engine.sound.SoundManager;

public record EntityInitializer(EntityInitializerLambda lambda) {

    @FunctionalInterface
    public interface EntityInitializerLambda {
        void execute(Entity entity);
    }
}
