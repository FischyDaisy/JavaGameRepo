package main.engine.enginelayouts;

import org.joml.Vector4f;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

public final class Vector4fLayout {
    public static final SequenceLayout LAYOUT = MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_FLOAT);
    public static final VarHandle X_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.sequenceElement(0));
    public static final VarHandle Y_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.sequenceElement(1));
    public static final VarHandle Z_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.sequenceElement(2));
    public static final VarHandle W_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.sequenceElement(3));

    private Vector4fLayout() {}

    public static float getX(MemorySegment segment, long offset) {
        return (float) X_HANDLE.get(segment, offset);
    }

    public static float getX(MemorySegment segment) {
        return getX(segment, 0);
    }

    public static float getY(MemorySegment segment, long offset) {
        return (float) Y_HANDLE.get(segment, offset);
    }

    public static float getY(MemorySegment segment) {
        return getY(segment, 0);
    }

    public static float getZ(MemorySegment segment, long offset) {
        return (float) Z_HANDLE.get(segment, offset);
    }

    public static float getZ(MemorySegment segment) {
        return getZ(segment, 0);
    }

    public static float getW(MemorySegment segment, long offset) {
        return (float) W_HANDLE.get(segment, offset);
    }

    public static float getW(MemorySegment segment) {
        return getW(segment, 0);
    }

    public static void setX(MemorySegment segment, long offset, float x) {
        X_HANDLE.set(segment, offset, x);
    }

    public static void setX(MemorySegment segment, float x) {
        setX(segment, 0, x);
    }

    public static void setY(MemorySegment segment, long offset, float y) {
        Y_HANDLE.set(segment, offset, y);
    }

    public static void setY(MemorySegment segment, float y) {
        setY(segment, 0, y);
    }

    public static void setZ(MemorySegment segment, long offset, float z) {
        Z_HANDLE.set(segment, offset, z);
    }

    public static void setZ(MemorySegment segment, float z) {
        setZ(segment, 0, z);
    }

    public static void setW(MemorySegment segment, long offset, float w) {
        W_HANDLE.set(segment, offset, w);
    }

    public static void setW(MemorySegment segment, float w) {
        setW(segment, 0, w);
    }

    public static Vector4f getVector4f(MemorySegment segment, long offset) {
        MemorySegment vecSegment = segment.asSlice(offset, LAYOUT.byteSize());
        Vector4f vector = new Vector4f(
                vecSegment.getAtIndex(ValueLayout.JAVA_FLOAT, 0),
                vecSegment.getAtIndex(ValueLayout.JAVA_FLOAT, 1),
                vecSegment.getAtIndex(ValueLayout.JAVA_FLOAT, 2),
                vecSegment.getAtIndex(ValueLayout.JAVA_FLOAT, 3)
        );
        return vector;
    }

    public static Vector4f getVector4f(MemorySegment segment) {
        return getVector4f(segment, 0);
    }

    public static void setVector4f(MemorySegment segment, long offset, Vector4f value) {
        MemorySegment vecSegment = segment.asSlice(offset, LAYOUT.byteSize());
        vecSegment.setAtIndex(ValueLayout.JAVA_FLOAT, 0, value.x);
        vecSegment.setAtIndex(ValueLayout.JAVA_FLOAT, 1, value.y);
        vecSegment.setAtIndex(ValueLayout.JAVA_FLOAT, 2, value.z);
        vecSegment.setAtIndex(ValueLayout.JAVA_FLOAT, 3, value.w);
    }

    public static void setVector4f(MemorySegment segment, Vector4f value) {
        setVector4f(segment, 0, value);
    }
}
