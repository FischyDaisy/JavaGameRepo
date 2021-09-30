package main.engine.graphics.camera;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import main.engine.Window;
import main.engine.graphics.Transformation;
import main.engine.items.GameItem;

public class Camera {

    private final Vector3f position;
    
    private final Vector3f rotationEuler;
    
    private final Quaternionf rotationQ;
    
    private final Matrix4f viewMatrix;
    
    public Camera() {
        position = new Vector3f();
        rotationEuler = new Vector3f();
        rotationQ = new Quaternionf();
        viewMatrix = new Matrix4f();
    }
    
    public Camera(Vector3f position, Vector3f rotation) {
    	this();
        position.set(position);
        rotation.set(rotation);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
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
    	Vector4f clipPlane = new Vector4f();
    	
    	Matrix4f projMatrix = window.getProjectionMatrix();
    	Vector4f q = new Vector4f();
    	
    	q.x = (sgn(clipPlane.x) + projMatrix.m02()) / projMatrix.m00();
    	q.y = (sgn(clipPlane.y) + projMatrix.m12()) / projMatrix.m11();
    	q.z = -1.0f;
    	q.w = (1.0f + projMatrix.m22()) / projMatrix.m23();
    	
    	Vector4f c = clipPlane.mul(2.0f / clipPlane.dot(q));
    	
    	Matrix4f clipMatrix = new Matrix4f(projMatrix);
    	clipMatrix.m20(c.x);
    	clipMatrix.m21(c.y);
    	clipMatrix.m22(c.z + 1.0f);
    	clipMatrix.m23(c.w);
    	
    	return clipMatrix;
    }
    
    public float sgn(float a) {
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

    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotationEuler.x += offsetX;
        rotationEuler.y += offsetY;
        rotationEuler.z += offsetZ;
        
        AxisAngle4f pitch = new AxisAngle4f((float) Math.toRadians(rotationEuler.x), new Vector3f(1.0f, 0.0f, 0.0f));
        AxisAngle4f yaw = new AxisAngle4f((float) Math.toRadians(rotationEuler.y), new Vector3f(0.0f, 1.0f, 0.0f));
        Quaternionf pPitch = new Quaternionf(pitch);
        Quaternionf pYaw = new Quaternionf(yaw);
        pPitch.mul(pYaw);
        rotationQ.set(pPitch);
        
        //rotationQ.rotateXYZ((float) Math.toRadians(offsetX), (float) Math.toRadians(offsetY), (float) Math.toRadians(offsetZ));
    }
}