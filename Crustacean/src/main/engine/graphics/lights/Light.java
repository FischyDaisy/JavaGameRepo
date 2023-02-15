package main.engine.graphics.lights;

import org.joml.Vector4f;

public class Light {

    private final Vector4f color;
    /**
     * For directional lights, the "w" coordinate will be 0. For point lights it will be "1". For directional lights
     * this attribute should be read as a direction.
     */
    private final Vector4f position;

    private boolean changed;

    public Light() {
        color = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
        position = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
        changed = true;
    }

    public Vector4f getColor() {
        return color;
    }

    public Vector4f getPosition() {
        return position;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}