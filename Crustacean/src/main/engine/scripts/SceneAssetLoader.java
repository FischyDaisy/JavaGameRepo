package main.engine.scripts;

import dev.dominion.ecs.api.Dominion;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.Engine;
import main.engine.scene.Scene;

import java.util.Objects;

public record SceneAssetLoader(SceneAssetLoaderLambda lambda) {

    public SceneAssetLoader {
        Objects.requireNonNull(lambda);
    }

    @FunctionalInterface
    public interface SceneAssetLoaderLambda {
        void execute(Scene scene, Engine engine);
    }
}
