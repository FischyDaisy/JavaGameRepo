package main.engine.items;

import main.engine.enginelayouts.Matrix4fLayout;
import main.engine.enginelayouts.QuaternionfLayout;
import main.engine.enginelayouts.Vector3fLayout;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class GameItem {

    private final String modelId;
    private final Vector3f position;
    private final Vector3f scale;
    private final Quaternionf rotation;
    private final Matrix4f modelMatrix;

    public GameItem(String modelId, Vector3f position, Vector3f scale, Quaternionf rotation) {
        this.modelId = modelId;
        this.position = position;
        this.scale = scale;
        this.rotation = rotation;
        this.modelMatrix = new Matrix4f();
    }

    public GameItem(String modelId, Vector3f position, float scale, Quaternionf rotation) {
        this(modelId, position, new Vector3f(scale), rotation);
    }

    public GameItem(String modelId, Vector3f position, Quaternionf rotation) {
        this(modelId, position, 1.0f, rotation);
    }

    public GameItem(String modelId) {
        this(modelId, new Vector3f(), new Vector3f(1.0f), new Quaternionf());
    }

    public String getModelId() {
        return modelId;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f value) {
        position.set(value);
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale.set(scale);
    }

    public void setScale(Vector3f v) {
        scale.set(v);
    }

    public void setScaleX(float scale) {
        this.scale.x = scale;
    }

    public void setScaleY(float scale) {
        this.scale.y = scale;
    }

    public void setScaleZ(float scale) {
        this.scale.z = scale;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public void setRotation(Quaternionf q) {
        rotation.set(q);
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public Matrix4f buildModelMatrix() {
        return modelMatrix.translationRotateScale(
                position.x, position.y, position.z,
                rotation.x, rotation.y, rotation.z, rotation.w,
                scale.x, scale.y, scale.z
        );
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof GameItem item &&
                this.modelId.equals(item.modelId) &&
                this.position.equals(item.position) &&
                this.scale.equals(item.scale) &&
                this.rotation.equals(item.rotation);
    }
}