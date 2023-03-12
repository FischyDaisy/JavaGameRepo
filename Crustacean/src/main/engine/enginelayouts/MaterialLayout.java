package main.engine.enginelayouts;

import org.joml.Vector4f;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

public final class MaterialLayout {
    public static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            Vector4fLayout.LAYOUT.withName("diffuseColor"),
            ValueLayout.JAVA_INT.withName("textureIdx"),
            ValueLayout.JAVA_INT.withName("normalMapIdx"),
            ValueLayout.JAVA_INT.withName("metalRoughMapIdx"),
            ValueLayout.JAVA_FLOAT.withName("roughnessFactor"),
            ValueLayout.JAVA_FLOAT.withName("metallicFactor"),
            MemoryLayout.paddingLayout(96)
    );
    public static final MethodHandle DIFFUSE_COLOR = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("diffuseColor"));
    public static final VarHandle TEXTURE_IDX = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("textureIdx"));
    public static final VarHandle NORMAL_MAP_IDX = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("normalMapIdx"));
    public static final VarHandle METAL_ROUGH_MAP_IDX = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("metalRoughMapIdx"));
    public static final VarHandle ROUGHNESS_FACTOR = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("roughnessFactor"));
    public static final VarHandle METALLIC_FACTOR = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("metallicFactor"));

    private MaterialLayout() {}

    public static Vector4f getDiffuseColor(MemorySegment segment, long offset) throws Throwable {
        return Vector4fLayout.getVector4f((MemorySegment) DIFFUSE_COLOR.invokeExact(segment.asSlice(offset)));
    }

    public static Vector4f getDiffuseColor(MemorySegment segment) throws Throwable {
        return getDiffuseColor(segment, 0);
    }

    public static int getTextureIdx(MemorySegment segment, long offset) {
        return (int) TEXTURE_IDX.get(segment.asSlice(offset));
    }

    public static int getTextureIdx(MemorySegment segment) {
        return getTextureIdx(segment, 0);
    }

    public static int getNormalMapIdx(MemorySegment segment, long offset) {
        return (int) NORMAL_MAP_IDX.get(segment.asSlice(offset));
    }

    public static int getNormalMapIdx(MemorySegment segment) {
        return getNormalMapIdx(segment, 0);
    }

    public static int getMetalRoughMapIdx(MemorySegment segment, long offset) {
        return (int) METAL_ROUGH_MAP_IDX.get(segment.asSlice(offset));
    }

    public static int getMetalRoughMapIdx(MemorySegment segment) {
        return getMetalRoughMapIdx(segment, 0);
    }

    public static float getRoughnessFactor(MemorySegment segment, long offset) {
        return (float) ROUGHNESS_FACTOR.get(segment.asSlice(offset));
    }

    public static float getRoughnessFactor(MemorySegment segment) {
        return getRoughnessFactor(segment, 0);
    }

    public static float getMetallicFactor(MemorySegment segment, long offset) {
        return (float) METALLIC_FACTOR.get(segment.asSlice(offset));
    }

    public static float getMetallicFactor(MemorySegment segment) {
        return getMetallicFactor(segment, 0);
    }

    public static void setDiffuseColor(MemorySegment segment, long offset, Vector4f value) throws Throwable {
        Vector4fLayout.setVector4f((MemorySegment) DIFFUSE_COLOR.invokeExact(segment.asSlice(offset)), value);
    }

    public static void setDiffuseColor(MemorySegment segment, Vector4f value) throws Throwable {
        setDiffuseColor(segment, 0, value);
    }

    public static void setTextureIdx(MemorySegment segment, long offset, int value) {
        TEXTURE_IDX.set(segment.asSlice(offset), value);
    }

    public static void setTextureIdx(MemorySegment segment, int value) {
        setTextureIdx(segment, 0, value);
    }

    public static void setNormalMapIdx(MemorySegment segment, long offset, int value) {
        NORMAL_MAP_IDX.set(segment.asSlice(offset), value);
    }

    public static void setNormalMapIdx(MemorySegment segment, int value) {
        setNormalMapIdx(segment, 0, value);
    }

    public static void setMetalRoughMapIdx(MemorySegment segment, long offset, int value) {
        METAL_ROUGH_MAP_IDX.set(segment.asSlice(offset), value);
    }

    public static void setMetalRoughMapIdx(MemorySegment segment, int value) {
        setMetalRoughMapIdx(segment, 0, value);
    }

    public static void setRoughnessFactor(MemorySegment segment, long offset, float value) {
        ROUGHNESS_FACTOR.set(segment.asSlice(offset), value);;
    }

    public static void setRoughnessFactor(MemorySegment segment, float value) {
        setRoughnessFactor(segment, 0, value);
    }

    public static void setMetallicFactor(MemorySegment segment, long offset, float value) {
        METALLIC_FACTOR.set(segment.asSlice(offset), value);
    }

    public static void setMetallicFactor(MemorySegment segment, float value) {
        setMetallicFactor(segment, 0, value);
    }
}
