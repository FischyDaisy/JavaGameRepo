package main.engine.enginelayouts;

import org.joml.Vector4f;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

public final class Vector4fLayout {

    public static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_FLOAT.withName("x"),
            ValueLayout.JAVA_FLOAT.withName("y"),
            ValueLayout.JAVA_FLOAT.withName("z"),
            ValueLayout.JAVA_FLOAT.withName("w")
    );
    public static final VarHandle X_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("x"));
    public static final VarHandle Y_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("y"));
    public static final VarHandle Z_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("z"));
    public static final VarHandle W_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("w"));

    private Vector4fLayout() {}

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

    public static float getZ(MemorySegment segment, long offset) {
        return (float) Z_HANDLE.get(segment.asSlice(offset));
    }

    public static float getZ(MemorySegment segment) {
        return getZ(segment, 0);
    }

    public static float getW(MemorySegment segment, long offset) {
        return (float) W_HANDLE.get(segment.asSlice(offset));
    }

    public static float getW(MemorySegment segment) {
        return getW(segment, 0);
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

    public static void setZ(MemorySegment segment, long offset, float z) {
        Z_HANDLE.set(segment.asSlice(offset), z);
    }

    public static void setZ(MemorySegment segment, float z) {
        setZ(segment, 0, z);
    }

    public static void setW(MemorySegment segment, long offset, float w) {
        W_HANDLE.set(segment.asSlice(offset), w);
    }

    public static void setW(MemorySegment segment, float w) {
        setW(segment, 0, w);
    }

    public static Vector4f getVector4f(MemorySegment segment, long offset) {
        MemorySegment vecSegment = segment.asSlice(offset);
        return new Vector4f(
                (float) X_HANDLE.get(vecSegment),
                (float) Y_HANDLE.get(vecSegment),
                (float) Z_HANDLE.get(vecSegment),
                (float) W_HANDLE.get(vecSegment)
        );
    }

    public static Vector4f getVector4f(MemorySegment segment) {
        return getVector4f(segment, 0);
    }

    public static void setVector4f(MemorySegment segment, long offset, Vector4f value) {
        MemorySegment vecSegment = segment.asSlice(offset);
        X_HANDLE.set(vecSegment, value.x);
        Y_HANDLE.set(vecSegment, value.y);
        Z_HANDLE.set(vecSegment, value.z);
        W_HANDLE.set(vecSegment, value.w);
    }

    public static void setVector4f(MemorySegment segment, Vector4f value) {
        setVector4f(segment, 0, value);
    }

    public static void setVector4f(MemorySegment segment, long offset, float x, float y, float z, float w) {
        MemorySegment vecSegment = segment.asSlice(offset);
        X_HANDLE.set(vecSegment, x);
        Y_HANDLE.set(vecSegment, y);
        Z_HANDLE.set(vecSegment, z);
        W_HANDLE.set(vecSegment, w);
    }

    public static void setVector4f(MemorySegment segment, float x, float y, float z, float w) {
        setVector4f(segment, 0, x, y, z, w);
    }
}
