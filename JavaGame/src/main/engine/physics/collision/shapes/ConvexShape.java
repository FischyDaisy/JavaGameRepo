package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

import main.engine.utility.physUtils.Transform;

/**
 * ConvexShape is an abstract shape class. It describes general convex shapes
 * using the {@link #localGetSupportingVertex localGetSupportingVertex} interface
 * used in combination with GJK or ConvexCast.
 * 
 * @author jezek2
 */
public abstract class ConvexShape extends CollisionShape {

	public static final int MAX_PREFERRED_PENETRATION_DIRECTIONS = 10;
	
	public abstract Vector3f localGetSupportingVertex(Vector3f vec, Vector3f out);

	//#ifndef __SPU__
	public abstract Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec, Vector3f out);

	//notice that the vectors should be unit length
	public abstract void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector3f[] vectors, Vector3f[] supportVerticesOut, int numVectors);
	//#endif
	
	public abstract void getAabbSlow(Transform t, Vector3f aabbMin, Vector3f aabbMax);

	public abstract void setLocalScaling(Vector3f scaling);

	public abstract Vector3f getLocalScaling(Vector3f out);

	public abstract void setMargin(float margin);

	public abstract float getMargin();

	public abstract int getNumPreferredPenetrationDirections();

	public abstract void getPreferredPenetrationDirection(int index, Vector3f penetrationVector);
	
}