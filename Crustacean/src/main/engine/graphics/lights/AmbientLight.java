package main.engine.graphics.lights;

import org.joml.Vector4f;

public record AmbientLight(Vector4f ambientLight) {
    public AmbientLight() {
        this(new Vector4f());
    }
}
