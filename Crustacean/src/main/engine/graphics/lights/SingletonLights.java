package main.engine.graphics.lights;

import dev.dominion.ecs.api.Dominion;
import main.engine.enginelayouts.Vector3fLayout;
import main.engine.enginelayouts.Vector4fLayout;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public record SingletonLights(MemorySegment data) {
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            Vector4fLayout.LAYOUT.withName("light"),
            Vector3fLayout.LAYOUT.withName("skyBoxLight")
    );
    public static final MethodHandle AMBIENT_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("light"));
    public static final MethodHandle SKYBOX_HANDLE = LAYOUT.sliceHandle(MemoryLayout.PathElement.groupElement("skyBoxLight"));

    public static SingletonLights fromDominion(Dominion dominion) {
        return dominion.findEntitiesWith(SingletonLights.class).iterator().next().comp();
    }

    public Vector4f getAmbientLight() {
        try {
            MemorySegment ambientLight = (MemorySegment) AMBIENT_HANDLE.invokeExact(data);
            return Vector4fLayout.getVector4f(ambientLight);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setAmbientLight(Vector4f lightColor) {
        try {
            MemorySegment ambientLight = (MemorySegment) AMBIENT_HANDLE.invokeExact(data);
            Vector4fLayout.setVector4f(ambientLight, lightColor);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Vector3f getSkyboxLight() {
        try {
            MemorySegment skyboxLight = (MemorySegment) SKYBOX_HANDLE.invokeExact(data);
            return Vector3fLayout.getVector3f(skyboxLight);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void setSkyboxLight(Vector3f lightColor) {
        try {
            MemorySegment skyboxLight = (MemorySegment) SKYBOX_HANDLE.invokeExact(data);
            Vector3fLayout.setVector3f(skyboxLight, lightColor);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
