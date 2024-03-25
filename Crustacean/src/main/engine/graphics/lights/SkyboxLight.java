package main.engine.graphics.lights;

import org.joml.Vector3f;

public record SkyboxLight(Vector3f light) {
    public SkyboxLight() {
        this(new Vector3f());
    }
}
