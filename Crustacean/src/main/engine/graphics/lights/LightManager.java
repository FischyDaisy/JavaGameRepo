package main.engine.graphics.lights;

import main.engine.EngineProperties;
import main.engine.enginelayouts.Vector3fLayout;
import main.engine.enginelayouts.Vector4fLayout;
import main.engine.items.GameItem;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public class LightManager implements AutoCloseable {

    private static final long LIGHT_BUFFER_SIZE = EngineProperties.INSTANCE.getMaxLightBuffer();
    private static final long LIGHT_LAYOUT_SIZE = Light.LAYOUT.byteSize();

    private static final MemoryLayout SINGLETON_LIGHTS = MemoryLayout.structLayout(
            Vector4fLayout.LAYOUT.withName("ambientLight"),
            Vector3fLayout.LAYOUT.withName("skyBoxLight")
    );

    private MemorySegment lights;
    private Arena arena;
    private long pos;

    public LightManager() {
        arena = Arena.openShared();
        long totalSize = SINGLETON_LIGHTS.byteSize() + (Light.LAYOUT.byteSize() * LIGHT_BUFFER_SIZE);
        lights = arena.allocate(totalSize);
        pos = SINGLETON_LIGHTS.byteSize();
    }

    public Vector4f getAmbientLight() {
        return Vector4fLayout.getVector4f(lights);
    }

    public void setAmbientLight(Vector4f value) {
        Vector4fLayout.setVector4f(lights, value);
    }

    public Vector3f getSkyboxLight() {
        long offset = Vector4fLayout.LAYOUT.byteSize();
        return Vector3fLayout.getVector3f(lights, offset);
    }

    public void setSkyboxLight(Vector3f value) {
        long offset = Vector4fLayout.LAYOUT.byteSize();
        Vector3fLayout.setVector3f(lights, offset, value);
    }

    public Light createLight() {
        Light light = new Light(lights.asSlice(pos, LIGHT_LAYOUT_SIZE));
        pos += LIGHT_LAYOUT_SIZE;
        return light;
    }

    public Light[] createLights(int count) {
        Light[] lights = new Light[count];
        for (int i = 0; i < count; i++) {
            Light light = new Light(this.lights.asSlice(pos, LIGHT_LAYOUT_SIZE));
            pos += LIGHT_LAYOUT_SIZE;
            lights[i] = light;
        }
        return lights;
    }

    /**
     * Sets the position of the internal MemorySegment back to 0, closes the internal Arena,
     * and creates a new Arena and MemorySegment.
     * All previously created Lights and MemorySegments from this manager should be discarded
     * as the owning Arena will be closed invalidating the backing MemorySegment.
     */
    public void resetBuffer() {
        arena.close();
        arena = Arena.openShared();
        long totalSize = SINGLETON_LIGHTS.byteSize() + Light.LAYOUT.byteSize();
        lights = arena.allocate(totalSize);
        pos = SINGLETON_LIGHTS.byteSize();
    }

    @Override
    public void close() throws Exception {
        arena.close();
    }
}
