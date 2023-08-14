package main.engine.utility;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public enum AxisRotation {
	UP(new Vector3f(0.0f, 1.0f, 0.0f)),
	LEFT(new Vector3f(1.0f, 0.0f, 0.0f)),
	FORWARD(new Vector3f(0.0f, 0.0f, 1.0f));
	
	private Vector3f direction;
	
	private float angle;
	
	private AxisAngle4f rotation;
	
	AxisRotation(Vector3f direction) {
		this.direction = direction;
		setRotation(0.0f);
	}
	
	public void setRotation(float angle) {
		this.angle = angle;
		rotation = new AxisAngle4f(this.angle, this.direction);
	}
	
	public AxisAngle4f getAxisRotation() {
		return rotation;
	}
	
	public Quaternionf getQuatRotation() {
		return new Quaternionf(rotation);
	}
	
	public Vector3f getDirection() {
		return direction;
	}
	
	public float getAngle() {
		return angle;
	}
}
