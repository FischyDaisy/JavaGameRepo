package main.engine.graphics.camera;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import main.engine.Window;
import main.engine.graphics.Transformation;
import main.engine.items.GameItem;
import main.engine.items.Portal;
import main.engine.utility.AxisRotation;

public class Camera {

    private final Vector3f position;
    
    private final Vector3f rotationEuler;
    
    private final Quaternionf rotationQ;
    
    private final Matrix4f viewMatrix;
    
    private boolean hasMoved;
    
    public Camera() {
        position = new Vector3f();
        rotationEuler = new Vector3f();
        rotationQ = new Quaternionf();
        viewMatrix = new Matrix4f();
    }
    
    public Camera(Vector3f position, Vector3f rotation) {
    	this();
        this.position.set(position);
        this.rotationEuler.set(rotation);
    }
    
    public Camera(Vector3f position, Quaternionf rotation) {
    	this();
        this.position.set(position);
        this.rotationQ.set(rotation);
    }
    
    public boolean hasMoved() {
        return hasMoved;
    }
    
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
        
        hasMoved = true;
    }
    
    public void setPosition(Vector3f v) {
    	position.set(v);
    	
    	hasMoved = true;
    }
    
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }
    
    public void setViewMatrix(Matrix4f m) {
    	viewMatrix.set(m);
    }
    
    public Matrix4f updateViewMatrixEuler() {
        return Transformation.updateGenericViewMatrix(position, rotationEuler, viewMatrix);
    }
    
    public Matrix4f updateViewMatrixQuat() {
        return Transformation.updateGenericViewMatrix(position, rotationQ, viewMatrix);
    }
    
    public Matrix4f clipOblique(Window window, GameItem gameItem) {
    	float distance = gameItem.getPosition().length();
    	Quaternionf rotation = gameItem.getRotation();
    	Vector4f clipPlane = new Vector4f(new Vector3f(0.0f, 0.0f, -1.0f).rotate(rotation), distance);
    	Matrix4f vClipMatrix = new Matrix4f();
    	vClipMatrix = this.viewMatrix.transpose(vClipMatrix).invert();
    	clipPlane = clipPlane.mul(vClipMatrix);
    	
    	if (clipPlane.w > 0.0f) {
    		return window.getProjectionMatrix();
    	}
    	
    	Matrix4f projMatrix = window.getProjectionMatrix();
    	Matrix4f tempProj = new Matrix4f();
    	Vector4f q = new Vector4f(sign(clipPlane.x), sign(clipPlane.y), 1.0f, 1.0f)
    			.mul(projMatrix.invert(tempProj));
    	
    	Vector4f c = clipPlane.mul(2.0f / clipPlane.dot(q));
    	
    	Matrix4f clipMatrix = new Matrix4f(projMatrix);
    	Vector4f temp = new Vector4f();
    	clipMatrix.setRow(2, c.sub(clipMatrix.getRow(3, temp)));
    	
    	return clipMatrix;
    }
    
    public float sign(float a) {
    	if (a > 0.0f) return (1.0f);
        if (a < 0.0f) return (-1.0f);
        return (0.0f);
    }
    
    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if ( offsetZ != 0 ) {
            position.x += (float)Math.sin(Math.toRadians(rotationEuler.y)) * -1.0f * offsetZ;
            position.z += (float)Math.cos(Math.toRadians(rotationEuler.y)) * offsetZ;
        }
        if ( offsetX != 0) {
            position.x += (float)Math.sin(Math.toRadians(rotationEuler.y - 90)) * -1.0f * offsetX;
            position.z += (float)Math.cos(Math.toRadians(rotationEuler.y - 90)) * offsetX;
        }
        position.y += offsetY;
        
        hasMoved = true;
    }

    public Vector3f getRotationEuler() {
        return rotationEuler;
    }
    
    public void setRotationEuler(float x, float y, float z) {
        rotationEuler.x = x;
        rotationEuler.y = y;
        rotationEuler.z = z;
    }
    
    public Quaternionf getRotationQ() {
    	return rotationQ;
    }
    
    public void setRotationQ(Quaternionf q) {
    	rotationQ.set(q);
    }
    
    public void updateQuat() {
    	updateQuat(this.rotationEuler);
    }
    
    public void updateQuat(Vector3f euler) {
    	AxisRotation rot = AxisRotation.LEFT;
    	rot.setRotation((float) Math.toRadians(euler.x));
    	Quaternionf pPitch = rot.getQuatRotation();
    	rot = AxisRotation.UP;
    	rot.setRotation((float) Math.toRadians(euler.y));
        Quaternionf pYaw = rot.getQuatRotation();
        pPitch.mul(pYaw);
        rotationQ.set(pPitch);
        
        hasMoved = true;
    }

    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotationEuler.x += offsetX;
        rotationEuler.y += offsetY;
        rotationEuler.z += offsetZ;
        
        updateQuat();
        
        //rotationQ.rotateXYZ((float) Math.toRadians(offsetX), (float) Math.toRadians(offsetY), (float) Math.toRadians(offsetZ));
    }
    
    public Camera createPortalCam(Portal fromPortal, Portal toPortal) {
    	Matrix4f fromPMat = fromPortal.buildModelMatrix().invert();
    	Matrix4f toPMat = toPortal.buildModelMatrix();
    	
    	// Position
    	Vector3f relativePos = new Vector3f();
    	AxisRotation rot = AxisRotation.UP;
    	rot.setRotation((float) Math.toRadians(180.0));
    	relativePos = fromPMat.transformPosition(this.getPosition(), relativePos);
    	relativePos = relativePos.rotate(rot.getQuatRotation());
    	Vector3f finalPos = new Vector3f(toPMat.transformPosition(relativePos));
    	
    	// Rotation
    	Quaternionf relativeRot = new Quaternionf();
    	relativeRot = this.getRotationQ().mul(fromPortal.getRotation().invert(), relativeRot);
    	relativeRot = relativeRot.mul(rot.getQuatRotation());
    	Quaternionf finalRot = new Quaternionf(relativeRot.mul(toPortal.getRotation()));
    	return new Camera(finalPos, finalRot);
    }
}