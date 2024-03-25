package main.engine.graphics.lights;

import main.engine.enginelayouts.Vector4fLayout;
import org.joml.Vector4f;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

public final class Light {

    private final Vector4f color;
    private final Vector4f position;
    private boolean changed;

    public Light(Vector4f color, Vector4f position, boolean changed) {
        this.color = color;
        this.position = position;
        this.changed = changed;
    }

    public Light(Vector4f color) {
        this(color, new Vector4f(), true);
    }

    public Light() {
        this(new Vector4f(), new Vector4f(), true);
    }

    public Vector4f getColor() {
        return color;
    }

    public void setColor(Vector4f value) {
        color.set(value);
    }

    public Vector4f getPosition() {
        return position;
    }

    public void setPosition(Vector4f value) {
        position.set(value);
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}