package main.engine.enginelayouts;

import java.lang.foreign.*;

public final class VertexLayout {
    public static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            Vector3fLayout.LAYOUT.withName("position"),
            Vector3fLayout.LAYOUT.withName("normals"),
            Vector3fLayout.LAYOUT.withName("tangents"),
            Vector3fLayout.LAYOUT.withName("biTangents"),
            MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_FLOAT).withName("textCoords")
    );

    private VertexLayout() {}
}
