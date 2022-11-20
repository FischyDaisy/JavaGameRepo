package main.engine.items;

import main.engine.graphics.ModelData;
import main.engine.graphics.particles.Particle;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import crab.newton.NewtonBody;
import main.engine.graphics.Transformation;

public sealed class GameItem permits Particle {
	
	private String id;
	
	private final String modelId;
	
	private boolean selected;
    
    private final Vector3f scale;
    
    private final Vector3f position;

    private final Quaternionf rotation;
    
    private final Matrix4f modelMatrix;

    private GameItemAnimation animation;

    private NewtonBody body;
    
    public GameItem(String modelId) {
        this.modelId = modelId;
    	selected = false;
        scale = new Vector3f(1.0f, 1.0f, 1.0f);
        modelMatrix = new Matrix4f();
        position = new Vector3f();
        rotation = new Quaternionf();
    }
    
    public GameItem(String id, String modelId) {
    	this(modelId);
    	this.id = id;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }
    
    public String getId() {
    	return id;
    }
    
    public void setId(String id) {
    	this.id = id;
    }

    public String getModelId() {
        return modelId;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
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
    
    public Matrix4f setMatrix(Matrix4f m) {
    	return modelMatrix.set(m);
    }

    public GameItemAnimation getAnimation() {
        return animation;
    }

    public void setAnimation(GameItemAnimation animation) {
        this.animation = animation;
    }

    public boolean hasAnimation() {
        return animation != null;
    }

    public NewtonBody getBody() {
        return body;
    }

    public void setBody(NewtonBody body) {
        this.body = body;
    }

    public boolean hasPhysicsBody() {
        return body != null;
    }
}