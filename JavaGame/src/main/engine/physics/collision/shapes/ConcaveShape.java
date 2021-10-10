package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

/**
 * ConcaveShape class provides an interface for non-moving (static) concave shapes.
 * 
 * @author jezek2
 */
public abstract class ConcaveShape extends CollisionShape {

	protected float collisionMargin = 0f;

	public abstract void processAllTriangles(TriangleCallback callback, Vector3f aabbMin, Vector3f aabbMax);

	public float getMargin() {
		return collisionMargin;
	}

	public void setMargin(float margin) {
		this.collisionMargin = margin;
	}
	
}