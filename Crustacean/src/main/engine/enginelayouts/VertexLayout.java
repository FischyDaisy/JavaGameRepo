package main.engine.enginelayouts;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class VertexLayout {
    public static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            Vector3fLayout.LAYOUT.withName("position"),
            Vector3fLayout.LAYOUT.withName("normals"),
            Vector3fLayout.LAYOUT.withName("tangents"),
            Vector3fLayout.LAYOUT.withName("biTangents"),
            Vector2fLayout.LAYOUT.withName("textCoords")
    );
    public static final MethodHandle POSITION = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("position"));
    public static final MethodHandle NORMALS = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("normals"));
    public static final MethodHandle TANGENTS = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("tangents"));
    public static final MethodHandle BITANGENTS = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("biTangents"));
    public static final MethodHandle TEXT_COORDS = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("textCoords"));

    private VertexLayout() {}

    public static Vector3f getPosition(MemorySegment segment, long offset) {
        try {
            return Vector3fLayout.getVector3f((MemorySegment) POSITION.invokeExact(segment.asSlice(offset)));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Vector3f getPosition(MemorySegment segment) {
        return getPosition(segment, 0);
    }

    public static Vector3f getNormals(MemorySegment segment, long offset) {
        try {
            return Vector3fLayout.getVector3f((MemorySegment) NORMALS.invokeExact(segment.asSlice(offset)));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Vector3f getNormals(MemorySegment segment) {
        return getNormals(segment, 0);
    }

    public static Vector3f getTangents(MemorySegment segment, long offset) {
        try {
            return Vector3fLayout.getVector3f((MemorySegment) TANGENTS.invokeExact(segment.asSlice(offset)));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Vector3f getTangents(MemorySegment segment) {
        return getTangents(segment, 0);
    }

    public static Vector3f getBiTangents(MemorySegment segment, long offset) {
        try {
            return Vector3fLayout.getVector3f((MemorySegment) BITANGENTS.invokeExact(segment.asSlice(offset)));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Vector3f getBiTangents(MemorySegment segment) {
        return getBiTangents(segment, 0);
    }

    public static Vector2f getTextCoords(MemorySegment segment, long offset) {
        try {
            return Vector2fLayout.getVector2f((MemorySegment) TEXT_COORDS.invokeExact(segment.asSlice(offset)));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Vector2f getTextCoords(MemorySegment segment) {
        return getTextCoords(segment, 0);
    }
}
