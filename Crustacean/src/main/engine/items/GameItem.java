package main.engine.items;

import main.engine.graphics.ModelData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import crab.newton.NewtonBody;
import main.engine.graphics.Transformation;

public final class GameItem {
	
	private final String modelId;
    private final Vector3f scale;
    private final Vector3f position;
    private final Quaternionf rotation;
    private final Matrix4f modelMatrix;
    
    public GameItem(String modelId) {
        this.modelId = modelId;
        scale = new Vector3f(1.0f, 1.0f, 1.0f);
        modelMatrix = new Matrix4f();
        position = new Vector3f();
        rotation = new Quaternionf();
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public String getModelId() {
        return modelId;
    }

    public Vector3f getScale() {
        return scale;
    }
    
    public float getUniformScale() throws Exception {
    	if ((scale.x == scale.y) && (scale.y == scale.z) && (scale.z == scale.x)) {
    		return scale.x;
    	} else {
    		throw new RuntimeException("Scale is not uniform");
    	}
    }
    
    public float getLargestScale() {
    	if ((scale.x >= scale.y) && (scale.x >= scale.z)) {
    		return scale.x;
    	} else if ((scale.y >= scale.x) && (scale.y >= scale.z)) {
    		return scale.y;
    	} else {
    		return scale.z;
    	}
    }

    public final void setScale(float scale) {
        this.scale.set(scale);
    }
    
    public final void setScale(Vector3f v) {
    	this.scale.set(v);
    }
    
    public final void setScaleX(float scale) {
        this.scale.x = scale;
    }
    
    public final void setScaleY(float scale) {
        this.scale.y = scale;
    }
    
    public final void setScaleZ(float scale) {
        this.scale.z = scale;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public final void setRotation(Quaternionf q) {
        this.rotation.set(q);
    }
    
    public Matrix4f getModelMatrix() {
    	return modelMatrix;
    }
    
    public Matrix4f buildModelMatrix() {
    	return Transformation.buildModelMatrix(this, modelMatrix);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof GameItem item &&
                this.modelId.equals(item.modelId) &&
                this.scale.equals(item.scale) &&
                this.position.equals(item.position) &&
                this.rotation.equals(item.rotation) &&
                this.modelMatrix.equals(item.modelMatrix);
    }
}