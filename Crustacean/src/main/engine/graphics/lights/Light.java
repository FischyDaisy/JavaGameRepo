package main.engine.graphics.lights;

import main.engine.enginelayouts.Vector4fLayout;
import org.joml.Vector4f;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

public record Light(MemorySegment data) {

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            Vector4fLayout.LAYOUT.withName("color"),
            Vector4fLayout.LAYOUT.withName("position"),
            ValueLayout.JAVA_BOOLEAN.withName("changed")
    );
    public static final MethodHandle COLOR_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("color"));
    public static final MethodHandle POSITION_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("position"));
    public static final VarHandle CHANGED_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("changed"));

    public Light {
        try {
            CHANGED_HANDLE.set(data, true);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Vector4f getColor() {
        try {
            MemorySegment color = (MemorySegment) COLOR_HANDLE.invokeExact(data);
            return Vector4fLayout.getVector4f(color);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setColor(Vector4f value) {
        try {
            MemorySegment color = (MemorySegment) COLOR_HANDLE.invokeExact(data);
            Vector4fLayout.setVector4f(color, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Vector4f getPosition() {
        try {
            MemorySegment position = (MemorySegment) POSITION_HANDLE.invokeExact(data);
            return Vector4fLayout.getVector4f(position);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setPosition(Vector4f value) {
        try {
            MemorySegment position = (MemorySegment) POSITION_HANDLE.invokeExact(data);
            Vector4fLayout.setVector4f(position, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isChanged() {
        return (boolean) CHANGED_HANDLE.get(data);
    }

    public void setChanged(boolean changed) {
        CHANGED_HANDLE.set(data, changed);
    }
}