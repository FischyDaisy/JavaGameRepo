package main.engine.items;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import crab.newton.NewtonBody;
import main.engine.graphics.Transformation;

public class GameItem {
	
	private GameItemAnimation gameItemAnimation;
	
	private NewtonBody body;
	
	private String id;
	
	private String modelId;
	
	private boolean selected;
    
    private final Vector3f scale;
    
    private final Transform transform;
    
    private final Matrix4f modelMatrix;
    
    private int textPos;
    
    private boolean disableFrustumCulling;
    
    private boolean insideFrustum;
    
    public GameItem() {
    	selected = false;
        transform = new Transform();
        scale = new Vector3f(1.0f, 1.0f, 1.0f);
        modelMatrix = new Matrix4f();
        textPos = 0;
        insideFrustum = true;
        disableFrustumCulling = false;
        body = null;
    }
    
    public GameItem(String id, String modelId) {
    	this();
    	this.id = id;
    	this.modelId = modelId;
    }
    
    public GameItemAnimation getGameItemAnimation() {
        return gameItemAnimation;
    }

    public void setGameItemAnimation(GameItemAnimation entityAnimation) {
        this.gameItemAnimation = entityAnimation;
    }
    
    public NewtonBody getBody() {
    	return body;
    }
    
    public void setBody(NewtonBody body) {
    	this.body = body;
    }

    public boolean hasAnimation() {
        return gameItemAnimation != null;
    }

    public Vector3f getPosition() {
        return transform.position;
    }
    
    public int getTextPos() {
        return textPos;
    }
    
    public void setTextPos(int textPos) {
        this.textPos = textPos;
    }

    public void setPosition(float x, float y, float z) {
        this.transform.position.x = x;
        this.transform.position.y = y;
        this.transform.position.z = z;
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
    
    public void setModelId(String modelId) {
    	this.modelId = modelId;
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
        return transform.rotation;
    }

    public final void setRotation(Quaternionf q) {
        this.transform.rotation.set(q);
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
    
    public boolean isInsideFrustum() {
        return insideFrustum;
    }

    public void setInsideFrustum(boolean insideFrustum) {
        this.insideFrustum = insideFrustum;
    }
    
    public boolean isDisableFrustumCulling() {
        return disableFrustumCulling;
    }

    public void setDisableFrustumCulling(boolean disableFrustumCulling) {
        this.disableFrustumCulling = disableFrustumCulling;
    }
    
    public static class GameItemAnimation {
        private int animationIdx;
        private int currentFrame;
        private boolean started;
        
        public final int maxFrames;

        public GameItemAnimation(boolean started, int animationIdx, int currentFrame, int maxFrames) {
            this.started = started;
            this.animationIdx = animationIdx;
            this.currentFrame = currentFrame;
            this.maxFrames = maxFrames;
        }

        public int getAnimationIdx() {
            return animationIdx;
        }

        public void setAnimationIdx(int animationIdx) {
            this.animationIdx = animationIdx;
        }

        public int getCurrentFrame() {
            return currentFrame;
        }

        public void setCurrentFrame(int currentFrame) {
            this.currentFrame = currentFrame;
        }

        public boolean isStarted() {
            return started;
        }

        public void setStarted(boolean started) {
            this.started = started;
        }
    }
}