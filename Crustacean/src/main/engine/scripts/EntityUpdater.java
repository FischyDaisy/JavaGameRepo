package main.engine.scripts;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import main.engine.Engine;
import main.engine.Window;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.input.EngineInput;
import main.engine.physics.Physics;
import main.engine.sound.SoundManager;

public record EntityUpdater(EntityUpdaterLambda lambda) {

    @FunctionalInterface
    public interface EntityUpdaterLambda {
        void execute(Entity entity, EngineInput engineInput);
    }
}
