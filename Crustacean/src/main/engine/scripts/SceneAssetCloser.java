package main.engine.scripts;

import dev.dominion.ecs.api.Dominion;
import main.engine.Engine;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.physics.Physics;

public record SceneAssetCloser(SceneAssetCloserLambda lambda) {

    @FunctionalInterface
    public interface SceneAssetCloserLambda {
        void execute(Dominion dominion, Engine engine);
    }
}
