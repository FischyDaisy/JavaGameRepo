package main.engine.enginelayouts;

import org.joml.Vector4f;

import java.lang.foreign.*;
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

    private MaterialLayout() {}

    public static Vector4f getDiffuseColor(MemorySegment segment, long offset) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        return Vector4fLayout.getVector4f(material);
    }

    public static Vector4f getDiffuseColor(MemorySegment segment) {
        return getDiffuseColor(segment, 0);
    }

    public static int getTextureIdx(MemorySegment segment, long offset) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        long textureIdxOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("textureIdx"));
        return material.get(ValueLayout.JAVA_INT, textureIdxOffset);
    }

    public static int getTextureIdx(MemorySegment segment) {
        return getTextureIdx(segment, 0);
    }

    public static int getNormalMapIdx(MemorySegment segment, long offset) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        long normalMapIdxOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("normalMapIdx"));
        return material.get(ValueLayout.JAVA_INT, normalMapIdxOffset);
    }

    public static int getNormalMapIdx(MemorySegment segment) {
        return getNormalMapIdx(segment, 0);
    }

    public static int getMetalRoughMapIdx(MemorySegment segment, long offset) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        long metalRoughMapIdxOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("metalRoughMapIdx"));
        return material.get(ValueLayout.JAVA_INT, metalRoughMapIdxOffset);
    }

    public static int getMetalRoughMapIdx(MemorySegment segment) {
        return getMetalRoughMapIdx(segment, 0);
    }

    public static float getRoughnessFactor(MemorySegment segment, long offset) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        long roughnessOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("roughnessFactor"));
        return material.get(ValueLayout.JAVA_FLOAT, roughnessOffset);
    }

    public static float getRoughnessFactor(MemorySegment segment) {
        return getRoughnessFactor(segment, 0);
    }

    public static float getMetallicFactor(MemorySegment segment, long offset) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        long metallicOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("metallicFactor"));
        return material.get(ValueLayout.JAVA_FLOAT, metallicOffset);
    }

    public static float getMetallicFactor(MemorySegment segment) {
        return getMetallicFactor(segment, 0);
    }

    public static void setDiffuseColor(MemorySegment segment, long offset, Vector4f value) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        Vector4fLayout.setVector4f(material, value);
    }

    public static void setDiffuseColor(MemorySegment segment, Vector4f value) {
        setDiffuseColor(segment, 0, value);
    }

    public static void setTextureIdx(MemorySegment segment, long offset, int value) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        long textureIdxOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("textureIdx"));
        material.set(ValueLayout.JAVA_INT, textureIdxOffset, value);
    }

    public static void setTextureIdx(MemorySegment segment, int value) {
        setTextureIdx(segment, 0, value);
    }

    public static void setNormalMapIdx(MemorySegment segment, long offset, int value) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        long normalMapIdxOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("normalMapIdx"));
        material.set(ValueLayout.JAVA_INT, normalMapIdxOffset, value);
    }

    public static void setNormalMapIdx(MemorySegment segment, int value) {
        setNormalMapIdx(segment, 0, value);
    }

    public static void setMetalRoughMapIdx(MemorySegment segment, long offset, int value) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        long metalRoughMapIdxOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("metalRoughMapIdx"));
        material.set(ValueLayout.JAVA_INT, metalRoughMapIdxOffset, value);
    }

    public static void setMetalRoughMapIdx(MemorySegment segment, int value) {
        setMetalRoughMapIdx(segment, 0, value);
    }

    public static void setRoughnessFactor(MemorySegment segment, long offset, float value) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        long roughnessOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("roughnessFactor"));
        material.set(ValueLayout.JAVA_FLOAT, roughnessOffset, value);
    }

    public static void setRoughnessFactor(MemorySegment segment, float value) {
        setRoughnessFactor(segment, 0, value);
    }

    public static void setMetallicFactor(MemorySegment segment, long offset, float value) {
        MemorySegment material = segment.asSlice(offset, LAYOUT.byteSize());
        long metallicOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("metallicFactor"));
        material.set(ValueLayout.JAVA_FLOAT, metallicOffset, value);
    }

    public static void setMetallicFactor(MemorySegment segment, float value) {
        setMetallicFactor(segment, 0, value);
    }
}
