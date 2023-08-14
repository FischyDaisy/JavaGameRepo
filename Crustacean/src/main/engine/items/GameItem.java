package main.engine.items;

import main.engine.enginelayouts.Matrix4fLayout;
import main.engine.enginelayouts.QuaternionfLayout;
import main.engine.enginelayouts.Vector3fLayout;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public record GameItem(String modelId, MemorySegment data) {

    public static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            Vector3fLayout.LAYOUT.withName("position"),
            Vector3fLayout.LAYOUT.withName("scale"),
            QuaternionfLayout.LAYOUT.withName("rotation"),
            Matrix4fLayout.LAYOUT.withName("modelMatrix")
    );
    public static final MethodHandle POSITION_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("position"));
    public static final MethodHandle SCALE_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("scale"));
    public static final MethodHandle ROTATION_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("rotation"));
    public static final MethodHandle MODEL_MATRIX_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("modelMatrix"));

    public Vector3f getPosition() {
        try {
            MemorySegment position = (MemorySegment) POSITION_HANDLE.invokeExact(data);
            return Vector3fLayout.getVector3f(position);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setPosition(Vector3f value) {
        try {
            MemorySegment position = (MemorySegment) POSITION_HANDLE.invokeExact(data);
            Vector3fLayout.setVector3f(position, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setPosition(float x, float y, float z) {
        try {
            MemorySegment position = (MemorySegment) POSITION_HANDLE.invokeExact(data);
            Vector3fLayout.setX(position, x);
            Vector3fLayout.setY(position, y);
            Vector3fLayout.setZ(position, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Vector3f getScale() {
        try {
            MemorySegment scale = (MemorySegment) SCALE_HANDLE.invokeExact(data);
            return Vector3fLayout.getVector3f(scale);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setScale(float scale) {
        try {
            MemorySegment scaleSegment = (MemorySegment) SCALE_HANDLE.invokeExact(data);
            Vector3fLayout.setX(scaleSegment, scale);
            Vector3fLayout.setY(scaleSegment, scale);
            Vector3fLayout.setZ(scaleSegment, scale);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setScale(Vector3f v) {
        try {
            MemorySegment scale = (MemorySegment) SCALE_HANDLE.invokeExact(data);
            Vector3fLayout.setVector3f(scale, v);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setScaleX(float scale) {
        try {
            MemorySegment scaleSegment = (MemorySegment) SCALE_HANDLE.invokeExact(data);
            Vector3fLayout.setX(scaleSegment, scale);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setScaleY(float scale) {
        try {
            MemorySegment scaleSegment = (MemorySegment) SCALE_HANDLE.invokeExact(data);
            Vector3fLayout.setY(scaleSegment, scale);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setScaleZ(float scale) {
        try {
            MemorySegment scaleSegment = (MemorySegment) SCALE_HANDLE.invokeExact(data);
            Vector3fLayout.setZ(scaleSegment, scale);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Quaternionf getRotation() {
        try {
            MemorySegment quaternion = (MemorySegment) ROTATION_HANDLE.invokeExact(data);
            return QuaternionfLayout.getQuaternion(quaternion);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setRotation(Quaternionf q) {
        try {
            MemorySegment quaternion = (MemorySegment) ROTATION_HANDLE.invokeExact(data);
            QuaternionfLayout.setQuaternion(quaternion, q);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Matrix4f getModelMatrix() {
        try {
            MemorySegment matrix = (MemorySegment) MODEL_MATRIX_HANDLE.invokeExact(data);
            return Matrix4fLayout.getMatrix(matrix);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Matrix4f buildModelMatrix() {
        try {
            Matrix4f matrix = getModelMatrix();
            Vector3f position = getPosition();
            Vector3f scale = getScale();
            Quaternionf rotation = getRotation();
            matrix.translationRotateScale(
                    position.x, position.y, position.z,
                    rotation.x, rotation.y, rotation.z, rotation.w,
                    scale.x, scale.y, scale.z
            );
            MemorySegment matrixSegment = (MemorySegment) MODEL_MATRIX_HANDLE.invokeExact(data);
            Matrix4fLayout.setMatrix(matrixSegment, matrix);
            return matrix;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof GameItem item &&
                this.modelId.equals(item.modelId) &&
                this.data.mismatch(item.data) == -1;
    }
}