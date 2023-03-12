package main.engine.enginelayouts;

import org.joml.Vector2f;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

public final class Vector2fLayout {
    public static final SequenceLayout LAYOUT = MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_FLOAT);
    public static final VarHandle X_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.sequenceElement(0));
    public static final VarHandle Y_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.sequenceElement(1));

    private Vector2fLayout() {}

    public static float getX(MemorySegment segment, long offset) {
        return (float) X_HANDLE.get(segment.asSlice(offset));
    }

    public static float getX(MemorySegment segment) {
        return getX(segment, 0);
    }

    public static float getY(MemorySegment segment, long offset) {
        return (float) Y_HANDLE.get(segment.asSlice(offset));
    }

    public static float getY(MemorySegment segment) {
        return getY(segment, 0);
    }

    public static void setX(MemorySegment segment, long offset, float x) {
        X_HANDLE.set(segment.asSlice(offset), x);
    }

    public static void setX(MemorySegment segment, float x) {
        setX(segment, 0, x);
    }

    public static void setY(MemorySegment segment, long offset, float y) {
        Y_HANDLE.set(segment.asSlice(offset), y);
    }

    public static void setY(MemorySegment segment, float y) {
        setY(segment, 0, y);
    }

    public static Vector2f getVector2f(MemorySegment segment, long offset) {
        MemorySegment vecSegment = segment.asSlice(offset);
        Vector2f vector = new Vector2f(
                (float) X_HANDLE.get(vecSegment),
                (float) Y_HANDLE.get(vecSegment)
        );
        return vector;
    }

    public static Vector2f getVector2f(MemorySegment segment) {
        return getVector2f(segment, 0);
    }

    public static void setVector2f(MemorySegment segment, long offset, Vector2f value) {
        MemorySegment vecSegment = segment.asSlice(offset);
        X_HANDLE.set(vecSegment, value.x);
        Y_HANDLE.set(vecSegment, value.y);
    }

    public static void setVector2f(MemorySegment segment, Vector2f value) {
        setVector2f(segment, 0, value);
    }
}
