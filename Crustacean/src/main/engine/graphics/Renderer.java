package main.engine.graphics;

import main.engine.Window;
import main.engine.physics.Physics;
import main.engine.scene.Scene;

public interface Renderer {
    void loadSkybox(Scene scene);

    void loadModels(Scene scene);

    void render(Window window, Scene scene, Physics physics);

    void unloadSkybox();

    void unloadModels();
}
