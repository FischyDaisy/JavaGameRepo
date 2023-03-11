package main.engine.enginelayouts;

import org.joml.Quaternionf;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

public final class QuaternionfLayout {
    public static final SequenceLayout LAYOUT = MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_FLOAT);
    public static final VarHandle X_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.sequenceElement(0));
    public static final VarHandle Y_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.sequenceElement(1));
    public static final VarHandle Z_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.sequenceElement(2));
    public static final VarHandle W_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.sequenceElement(3));

    private QuaternionfLayout() {}

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

    public static Quaternionf getQuaternion(MemorySegment segment, long offset) {
        MemorySegment quatSegment = segment.asSlice(offset, LAYOUT.byteSize());
        Quaternionf quat = new Quaternionf(
                quatSegment.getAtIndex(ValueLayout.JAVA_FLOAT, 0),
                quatSegment.getAtIndex(ValueLayout.JAVA_FLOAT, 1),
                quatSegment.getAtIndex(ValueLayout.JAVA_FLOAT, 2),
                quatSegment.getAtIndex(ValueLayout.JAVA_FLOAT, 3)
        );
        return quat;
    }

    public static Quaternionf getQuaternion(MemorySegment segment) {
        return getQuaternion(segment, 0);
    }

    public static void setQuaternion(MemorySegment segment, long offset, Quaternionf value) {
        MemorySegment quatSegment = segment.asSlice(offset, LAYOUT.byteSize());
        quatSegment.setAtIndex(ValueLayout.JAVA_FLOAT, 0, value.x);
        quatSegment.setAtIndex(ValueLayout.JAVA_FLOAT, 1, value.y);
        quatSegment.setAtIndex(ValueLayout.JAVA_FLOAT, 2, value.z);
        quatSegment.setAtIndex(ValueLayout.JAVA_FLOAT, 3, value.w);
    }

    public static void setQuaternion(MemorySegment segment, Quaternionf value) {
        setQuaternion(segment, 0, value);
    }
}
