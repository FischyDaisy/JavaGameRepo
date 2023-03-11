package main.engine.enginelayouts;

import org.joml.Matrix4f;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

public final class Matrix4fLayout {
    public static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            Vector4fLayout.LAYOUT.withName("col0"),
            Vector4fLayout.LAYOUT.withName("col1"),
            Vector4fLayout.LAYOUT.withName("col2"),
            Vector4fLayout.LAYOUT.withName("col3")
    );
    public static final VarHandle M00_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col0"),
            MemoryLayout.PathElement.sequenceElement(0));
    public static final VarHandle M01_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col0"),
            MemoryLayout.PathElement.sequenceElement(1));
    public static final VarHandle M02_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col0"),
            MemoryLayout.PathElement.sequenceElement(2));
    public static final VarHandle m03_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col0"),
            MemoryLayout.PathElement.sequenceElement(3));
    public static final VarHandle M10_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col1"),
            MemoryLayout.PathElement.sequenceElement(0));
    public static final VarHandle M11_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col1"),
            MemoryLayout.PathElement.sequenceElement(1));
    public static final VarHandle M12_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col1"),
            MemoryLayout.PathElement.sequenceElement(2));
    public static final VarHandle M13_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col1"),
            MemoryLayout.PathElement.sequenceElement(3));
    public static final VarHandle M20_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col2"),
            MemoryLayout.PathElement.sequenceElement(0));
    public static final VarHandle M21_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col2"),
            MemoryLayout.PathElement.sequenceElement(1));
    public static final VarHandle M22_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col2"),
            MemoryLayout.PathElement.sequenceElement(2));
    public static final VarHandle M23_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col2"),
            MemoryLayout.PathElement.sequenceElement(3));
    public static final VarHandle M30_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col3"),
            MemoryLayout.PathElement.sequenceElement(0));
    public static final VarHandle M31_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col3"),
            MemoryLayout.PathElement.sequenceElement(1));
    public static final VarHandle M32_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col3"),
            MemoryLayout.PathElement.sequenceElement(2));
    public static final VarHandle M33_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("col3"),
            MemoryLayout.PathElement.sequenceElement(3));

    private Matrix4fLayout() {}

    public static float m00(MemorySegment segment, long offset) {
        return (float) M00_HANDLE.get(segment, offset);
    }

    public static float m00(MemorySegment segment) {
        return m00(segment, 0);
    }

    public static void m00(MemorySegment segment, long offset, float value) {
        M00_HANDLE.set(segment, offset, value);
    }

    public static void m00(MemorySegment segment, float value) {
        m00(segment, 0, value);
    }

    public static float m01(MemorySegment segment, long offset) {
        return (float) M01_HANDLE.get(segment, offset);
    }

    public static float m01(MemorySegment segment) {
        return m01(segment, 0);
    }

    public static void m01(MemorySegment segment, long offset, float value) {
        M01_HANDLE.set(segment, offset, value);
    }

    public static void m01(MemorySegment segment, float value) {
        m01(segment, 0, value);
    }

    public static float m02(MemorySegment segment, long offset) {
        return (float) M02_HANDLE.get(segment, offset);
    }

    public static float m02(MemorySegment segment) {
        return m02(segment, 0);
    }

    public static void m02(MemorySegment segment, long offset, float value) {
        M02_HANDLE.set(segment, offset, value);
    }

    public static void m02(MemorySegment segment, float value) {
        m02(segment, 0, value);
    }

    public static float m03(MemorySegment segment, long offset) {
        return (float) m03_HANDLE.get(segment, offset);
    }

    public static float m03(MemorySegment segment) {
        return m03(segment, 0);
    }

    public static void m03(MemorySegment segment, long offset, float value) {
        m03_HANDLE.set(segment, offset, value);
    }

    public static void m03(MemorySegment segment, float value) {
        m03(segment, 0, value);
    }

    public static float m10(MemorySegment segment, long offset) {
        return (float) M10_HANDLE.get(segment, offset);
    }

    public static float m10(MemorySegment segment) {
        return m10(segment, 0);
    }

    public static void m10(MemorySegment segment, long offset, float value) {
        M10_HANDLE.set(segment, offset, value);
    }

    public static void m10(MemorySegment segment, float value) {
        m10(segment, 0, value);
    }

    public static float m11(MemorySegment segment, long offset) {
        return (float) M11_HANDLE.get(segment, offset);
    }

    public static float m11(MemorySegment segment) {
        return m11(segment, 0);
    }

    public static void m11(MemorySegment segment, long offset, float value) {
        M11_HANDLE.set(segment, offset, value);
    }

    public static void m11(MemorySegment segment, float value) {
        m11(segment, 0, value);
    }

    public static float m12(MemorySegment segment, long offset) {
        return (float) M12_HANDLE.get(segment, offset);
    }

    public static float m12(MemorySegment segment) {
        return m12(segment, 0);
    }

    public static void m12(MemorySegment segment, long offset, float value) {
        M12_HANDLE.set(segment, offset, value);
    }

    public static void m12(MemorySegment segment, float value) {
        m12(segment, 0, value);
    }

    public static float m13(MemorySegment segment, long offset) {
        return (float) M13_HANDLE.get(segment, offset);
    }

    public static float m13(MemorySegment segment) {
        return m13(segment, 0);
    }

    public static void m13(MemorySegment segment, long offset, float value) {
        M13_HANDLE.set(segment, offset, value);
    }

    public static void m13(MemorySegment segment, float value) {
        m13(segment, 0, value);
    }

    public static float m20(MemorySegment segment, long offset) {
        return (float) M20_HANDLE.get(segment, offset);
    }

    public static float m20(MemorySegment segment) {
        return m20(segment, 0);
    }

    public static void m20(MemorySegment segment, long offset, float value) {
        M20_HANDLE.set(segment, offset, value);
    }

    public static void m20(MemorySegment segment, float value) {
        m20(segment, 0, value);
    }

    public static float m21(MemorySegment segment, long offset) {
        return (float) M21_HANDLE.get(segment, offset);
    }

    public static float m21(MemorySegment segment) {
        return m21(segment, 0);
    }

    public static void m21(MemorySegment segment, long offset, float value) {
        M21_HANDLE.set(segment, offset, value);
    }

    public static void m21(MemorySegment segment, float value) {
        m21(segment, 0, value);
    }

    public static float m22(MemorySegment segment, long offset) {
        return (float) M22_HANDLE.get(segment, offset);
    }

    public static float m22(MemorySegment segment) {
        return m22(segment, 0);
    }

    public static void m22(MemorySegment segment, long offset, float value) {
        M22_HANDLE.set(segment, offset, value);
    }

    public static void m22(MemorySegment segment, float value) {
        m22(segment, 0, value);
    }

    public static float m23(MemorySegment segment, long offset) {
        return (float) M23_HANDLE.get(segment, offset);
    }

    public static float m23(MemorySegment segment) {
        return m23(segment, 0);
    }

    public static void m23(MemorySegment segment, long offset, float value) {
        M23_HANDLE.set(segment, offset, value);
    }

    public static void m23(MemorySegment segment, float value) {
        m23(segment, 0, value);
    }

    public static float m30(MemorySegment segment, long offset) {
        return (float) M30_HANDLE.get(segment, offset);
    }

    public static float m30(MemorySegment segment) {
        return m30(segment, 0);
    }

    public static void m30(MemorySegment segment, long offset, float value) {
        M30_HANDLE.set(segment, offset, value);
    }

    public static void m30(MemorySegment segment, float value) {
        m30(segment, 0, value);
    }

    public static float m31(MemorySegment segment, long offset) {
        return (float) M31_HANDLE.get(segment, offset);
    }

    public static float m31(MemorySegment segment) {
        return m31(segment, 0);
    }

    public static void m31(MemorySegment segment, long offset, float value) {
        M31_HANDLE.set(segment, offset, value);
    }

    public static void m31(MemorySegment segment, float value) {
        m31(segment, 0, value);
    }

    public static float m32(MemorySegment segment, long offset) {
        return (float) M32_HANDLE.get(segment, offset);
    }

    public static float m32(MemorySegment segment) {
        return m32(segment, 0);
    }

    public static void m32(MemorySegment segment, long offset, float value) {
        M32_HANDLE.set(segment, offset, value);
    }

    public static void m32(MemorySegment segment, float value) {
        m32(segment, 0, value);
    }

    public static float m33(MemorySegment segment, long offset) {
        return (float) M33_HANDLE.get(segment, offset);
    }

    public static float m33(MemorySegment segment) {
        return m33(segment, 0);
    }

    public static void m33(MemorySegment segment, long offset, float value) {
        M33_HANDLE.set(segment, offset, value);
    }

    public static void m33(MemorySegment segment, float value) {
        m33(segment, 0, value);
    }

    public static Matrix4f getMatrix(MemorySegment segment, long offset) {
        MemorySegment matSegment = segment.asSlice(offset, LAYOUT.byteSize());
        Matrix4f matrix = new Matrix4f();
        matrix.set(matSegment.asByteBuffer());
        return matrix;
    }

    public static Matrix4f getMatrix(MemorySegment segment) {
        return getMatrix(segment, 0);
    }

    public static void setMatrix(MemorySegment segment, long offset, Matrix4f value) {
        float[] matrix = new float[16];
        value.get(matrix);
        MemorySegment.copy(matrix, 0, segment, ValueLayout.JAVA_FLOAT, offset, matrix.length);
    }

    public static void setMatrix(MemorySegment segment, Matrix4f value) {
        setMatrix(segment, 0, value);
    }
}
