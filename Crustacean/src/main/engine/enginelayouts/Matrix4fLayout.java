package main.engine.enginelayouts;

import org.joml.Matrix4f;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import static main.engine.enginelayouts.Vector4fLayout.*;

public final class Matrix4fLayout {

    public static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            Vector4fLayout.LAYOUT.withName("col0"),
            Vector4fLayout.LAYOUT.withName("col1"),
            Vector4fLayout.LAYOUT.withName("col2"),
            Vector4fLayout.LAYOUT.withName("col3")
    );
    public static final MethodHandle COL0_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("col0"));
    public static final MethodHandle COL1_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("col1"));
    public static final MethodHandle COL2_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("col2"));
    public static final MethodHandle COL3_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("col3"));

    private Matrix4fLayout() {}

    public static float m00(MemorySegment segment, long offset) {
        try {
            MemorySegment col0 = (MemorySegment) COL0_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) X_HANDLE.get(col0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m00(MemorySegment segment) {
        return m00(segment, 0);
    }

    public static void m00(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col0 = (MemorySegment) COL0_HANDLE.invokeExact(segment.asSlice(offset));
            X_HANDLE.set(col0, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m00(MemorySegment segment, float value) {
        m00(segment, 0, value);
    }

    public static float m01(MemorySegment segment, long offset) {
        try {
            MemorySegment col0 = (MemorySegment) COL0_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) Y_HANDLE.get(col0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m01(MemorySegment segment) {
        return m01(segment, 0);
    }

    public static void m01(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col0 = (MemorySegment) COL0_HANDLE.invokeExact(segment.asSlice(offset));
            Y_HANDLE.set(col0, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m01(MemorySegment segment, float value) {
        m01(segment, 0, value);
    }

    public static float m02(MemorySegment segment, long offset) {
        try {
            MemorySegment col0 = (MemorySegment) COL0_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) Z_HANDLE.get(col0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m02(MemorySegment segment) {
        return m02(segment, 0);
    }

    public static void m02(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col0 = (MemorySegment) COL0_HANDLE.invokeExact(segment.asSlice(offset));
            Z_HANDLE.set(col0, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m02(MemorySegment segment, float value) {
        m02(segment, 0, value);
    }

    public static float m03(MemorySegment segment, long offset) {
        try {
            MemorySegment col0 = (MemorySegment) COL0_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) W_HANDLE.get(col0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m03(MemorySegment segment) {
        return m03(segment, 0);
    }

    public static void m03(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col0 = (MemorySegment) COL0_HANDLE.invokeExact(segment.asSlice(offset));
            W_HANDLE.set(col0, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m03(MemorySegment segment, float value) {
        m03(segment, 0, value);
    }

    public static float m10(MemorySegment segment, long offset) {
        try {
            MemorySegment col1 = (MemorySegment) COL1_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) X_HANDLE.get(col1);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m10(MemorySegment segment) {
        return m10(segment, 0);
    }

    public static void m10(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col1 = (MemorySegment) COL1_HANDLE.invokeExact(segment.asSlice(offset));
            X_HANDLE.set(col1, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m10(MemorySegment segment, float value) {
        m10(segment, 0, value);
    }

    public static float m11(MemorySegment segment, long offset) {
        try {
            MemorySegment col1 = (MemorySegment) COL1_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) Y_HANDLE.get(col1);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m11(MemorySegment segment) {
        return m11(segment, 0);
    }

    public static void m11(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col1 = (MemorySegment) COL1_HANDLE.invokeExact(segment.asSlice(offset));
            Y_HANDLE.set(col1, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m11(MemorySegment segment, float value) {
        m11(segment, 0, value);
    }

    public static float m12(MemorySegment segment, long offset) {
        try {
            MemorySegment col1 = (MemorySegment) COL1_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) Z_HANDLE.get(col1);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m12(MemorySegment segment) {
        return m12(segment, 0);
    }

    public static void m12(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col1 = (MemorySegment) COL1_HANDLE.invokeExact(segment.asSlice(offset));
            Z_HANDLE.set(col1, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m12(MemorySegment segment, float value) {
        m12(segment, 0, value);
    }

    public static float m13(MemorySegment segment, long offset) {
        try {
            MemorySegment col1 = (MemorySegment) COL1_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) W_HANDLE.get(col1);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m13(MemorySegment segment) {
        return m13(segment, 0);
    }

    public static void m13(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col1 = (MemorySegment) COL1_HANDLE.invokeExact(segment.asSlice(offset));
            W_HANDLE.set(col1, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m13(MemorySegment segment, float value) {
        m13(segment, 0, value);
    }

    public static float m20(MemorySegment segment, long offset) {
        try {
            MemorySegment col2 = (MemorySegment) COL2_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) X_HANDLE.get(col2);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m20(MemorySegment segment) {
        return m20(segment, 0);
    }

    public static void m20(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col2 = (MemorySegment) COL2_HANDLE.invokeExact(segment.asSlice(offset));
            X_HANDLE.set(col2, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m20(MemorySegment segment, float value) {
        m20(segment, 0, value);
    }

    public static float m21(MemorySegment segment, long offset) {
        try {
            MemorySegment col2 = (MemorySegment) COL2_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) Y_HANDLE.get(col2);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m21(MemorySegment segment) {
        return m21(segment, 0);
    }

    public static void m21(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col2 = (MemorySegment) COL2_HANDLE.invokeExact(segment.asSlice(offset));
            Y_HANDLE.set(col2, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m21(MemorySegment segment, float value) {
        m21(segment, 0, value);
    }

    public static float m22(MemorySegment segment, long offset) {
        try {
            MemorySegment col2 = (MemorySegment) COL2_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) Z_HANDLE.get(col2);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m22(MemorySegment segment) {
        return m22(segment, 0);
    }

    public static void m22(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col2 = (MemorySegment) COL2_HANDLE.invokeExact(segment.asSlice(offset));
            Z_HANDLE.set(col2, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m22(MemorySegment segment, float value) {
        m22(segment, 0, value);
    }

    public static float m23(MemorySegment segment, long offset) {
        try {
            MemorySegment col2 = (MemorySegment) COL2_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) W_HANDLE.get(col2);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m23(MemorySegment segment) {
        return m23(segment, 0);
    }

    public static void m23(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col2 = (MemorySegment) COL2_HANDLE.invokeExact(segment.asSlice(offset));
            W_HANDLE.set(col2, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m23(MemorySegment segment, float value) {
        m23(segment, 0, value);
    }

    public static float m30(MemorySegment segment, long offset) {
        try {
            MemorySegment col3 = (MemorySegment) COL3_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) X_HANDLE.get(col3);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m30(MemorySegment segment) {
        return m30(segment, 0);
    }

    public static void m30(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col3 = (MemorySegment) COL3_HANDLE.invokeExact(segment.asSlice(offset));
            X_HANDLE.set(col3, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m30(MemorySegment segment, float value) {
        m30(segment, 0, value);
    }

    public static float m31(MemorySegment segment, long offset) {
        try {
            MemorySegment col3 = (MemorySegment) COL3_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) Y_HANDLE.get(col3);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m31(MemorySegment segment) {
        return m31(segment, 0);
    }

    public static void m31(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col3 = (MemorySegment) COL3_HANDLE.invokeExact(segment.asSlice(offset));
            Y_HANDLE.set(col3, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m31(MemorySegment segment, float value) {
        m31(segment, 0, value);
    }

    public static float m32(MemorySegment segment, long offset) {
        try {
            MemorySegment col3 = (MemorySegment) COL3_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) Z_HANDLE.get(col3);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m32(MemorySegment segment) {
        return m32(segment, 0);
    }

    public static void m32(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col3 = (MemorySegment) COL3_HANDLE.invokeExact(segment.asSlice(offset));
            Z_HANDLE.set(col3, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m32(MemorySegment segment, float value) {
        m32(segment, 0, value);
    }

    public static float m33(MemorySegment segment, long offset) {
        try {
            MemorySegment col3 = (MemorySegment) COL3_HANDLE.invokeExact(segment.asSlice(offset));
            return (float) W_HANDLE.get(col3);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float m33(MemorySegment segment) {
        return m33(segment, 0);
    }

    public static void m33(MemorySegment segment, long offset, float value) {
        try {
            MemorySegment col3 = (MemorySegment) COL3_HANDLE.invokeExact(segment.asSlice(offset));
            W_HANDLE.set(col3, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void m33(MemorySegment segment, float value) {
        m33(segment, 0, value);
    }

    public static Matrix4f getMatrix(MemorySegment segment, long offset) {
        MemorySegment matSegment = segment.asSlice(offset);
        Matrix4f matrix = new Matrix4f(
                m00(matSegment),
                m01(matSegment),
                m02(matSegment),
                m03(matSegment),
                m10(matSegment),
                m11(matSegment),
                m12(matSegment),
                m13(matSegment),
                m20(matSegment),
                m21(matSegment),
                m22(matSegment),
                m23(matSegment),
                m30(matSegment),
                m31(matSegment),
                m32(matSegment),
                m33(matSegment)
        );
        return matrix;
    }

    public static Matrix4f getMatrix(MemorySegment segment) {
        return getMatrix(segment, 0);
    }

    public static void setMatrix(MemorySegment segment, long offset, Matrix4f value) {
        MemorySegment matSegment = segment.asSlice(offset);
        m00(matSegment, value.m00());
        m01(matSegment, value.m01());
        m02(matSegment, value.m02());
        m03(matSegment, value.m03());
        m10(matSegment, value.m10());
        m11(matSegment, value.m11());
        m12(matSegment, value.m12());
        m13(matSegment, value.m13());
        m20(matSegment, value.m20());
        m21(matSegment, value.m21());
        m22(matSegment, value.m22());
        m23(matSegment, value.m23());
        m30(matSegment, value.m30());
        m31(matSegment, value.m31());
        m32(matSegment, value.m32());
        m33(matSegment, value.m33());
    }

    public static void setMatrix(MemorySegment segment, Matrix4f value) {
        setMatrix(segment, 0, value);
    }

    public static void identity(MemorySegment segment, long offset) {
        MemorySegment matSegment = segment.asSlice(offset);
        m00(matSegment, 1.0f);
        m01(matSegment, 0.0f);
        m02(matSegment, 0.0f);
        m03(matSegment, 0.0f);
        m10(matSegment, 0.0f);
        m11(matSegment, 1.0f);
        m12(matSegment, 0.0f);
        m13(matSegment, 0.0f);
        m20(matSegment, 0.0f);
        m21(matSegment, 0.0f);
        m22(matSegment, 1.0f);
        m23(matSegment, 0.0f);
        m30(matSegment, 0.0f);
        m31(matSegment, 0.0f);
        m32(matSegment, 0.0f);
        m33(matSegment, 1.0f);
    }

    public static void identity(MemorySegment segment) {
        identity(segment, 0);
    }

    public static MemorySegment identity(Arena arena) {
        MemorySegment matrix = arena.allocate(LAYOUT);
        identity(matrix);
        return matrix;
    }
}
