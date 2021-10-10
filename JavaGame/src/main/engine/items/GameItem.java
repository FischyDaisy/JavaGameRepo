package main.engine.items;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import main.engine.graphics.opengl.Mesh;
import main.engine.utility.physUtils.Transform;

public class GameItem {
	
	private boolean selected;

	private Mesh[] meshes;
    
    private float scale;
    
    private final Transform transform;
    
    private int textPos;
    
    private boolean disableFrustumCulling;
    
    private boolean insideFrustum;
    
    public GameItem() {
    	selected = false;
        transform = new Transform();
        scale = 1;
        textPos = 0;
        insideFrustum = true;
        disableFrustumCulling = false;
    }

    public GameItem(Mesh mesh) {
        this();
        this.meshes = new Mesh[]{mesh};
    }

    public GameItem(Mesh[] meshes) {
        this();
        this.meshes = meshes;
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
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public float getScale() {
        return scale;
    }

    public final void setScale(float scale) {
        this.scale = scale;
    }

    public Quaternionf getRotation() {
        return transform.rotation;
    }

    public final void setRotation(Quaternionf q) {
        this.transform.rotation.set(q);
    }
    
    public Mesh getMesh() {
        return meshes[0];
    }
    
    public Mesh[] getMeshes() {
        return meshes;
    }

    public void setMeshes(Mesh[] meshes) {
        this.meshes = meshes;
    }

    public void setMesh(Mesh mesh) {
        this.meshes = new Mesh[]{mesh};
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
    
    public void cleanup() {
        int numMeshes = this.meshes != null ? this.meshes.length : 0;
        for(int i = 0; i < numMeshes; i++) {
            this.meshes[i].cleanup();
        }
    }
}