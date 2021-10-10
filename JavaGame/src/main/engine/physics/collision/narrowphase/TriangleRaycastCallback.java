package main.engine.physics.collision.narrowphase;

import org.joml.Vector3f;

import com.bulletphysics.collision.shapes.TriangleCallback;
import com.bulletphysics.linearmath.VectorUtil;

/**
*
* @author jezek2
*/
public abstract class TriangleRaycastCallback extends TriangleCallback {
	
	//protected final BulletStack stack = BulletStack.get();

	public final Vector3f from = new Vector3f();
	public final Vector3f to = new Vector3f();

	public float hitFraction;

	public TriangleRaycastCallback(Vector3f from, Vector3f to) {
		this.from.set(from);
		this.to.set(to);
		this.hitFraction = 1f;
	}
	
	public void processTriangle(Vector3f[] triangle, int partId, int triangleIndex) {
		Vector3f vert0 = triangle[0];
		Vector3f vert1 = triangle[1];
		Vector3f vert2 = triangle[2];

		Vector3f v10 = new Vector3f();
		v10.sub(vert1, vert0);

		Vector3f v20 = new Vector3f();
		v20.sub(vert2, vert0);

		Vector3f triangleNormal = new Vector3f();
		triangleNormal.cross(v10, v20);

		float dist = vert0.dot(triangleNormal);
		float dist_a = triangleNormal.dot(from);
		dist_a -= dist;
		float dist_b = triangleNormal.dot(to);
		dist_b -= dist;

		if (dist_a * dist_b >= 0f) {
			return; // same sign
		}

		float proj_length = dist_a - dist_b;
		float distance = (dist_a) / (proj_length);
		// Now we have the intersection point on the plane, we'll see if it's inside the triangle
		// Add an epsilon as a tolerance for the raycast,
		// in case the ray hits exacly on the edge of the triangle.
		// It must be scaled for the triangle size.

		if (distance < hitFraction) {
			float edge_tolerance = triangleNormal.lengthSquared();
			edge_tolerance *= -0.0001f;
			Vector3f point = new Vector3f();
			VectorUtil.setInterpolate3(point, from, to, distance);
			{
				Vector3f v0p = new Vector3f();
				v0p.sub(vert0, point);
				Vector3f v1p = new Vector3f();
				v1p.sub(vert1, point);
				Vector3f cp0 = new Vector3f();
				cp0.cross(v0p, v1p);

				if (cp0.dot(triangleNormal) >= edge_tolerance) {
					Vector3f v2p = new Vector3f();
					v2p.sub(vert2, point);
					Vector3f cp1 = new Vector3f();
					cp1.cross(v1p, v2p);
					if (cp1.dot(triangleNormal) >= edge_tolerance) {
						Vector3f cp2 = new Vector3f();
						cp2.cross(v2p, v0p);

						if (cp2.dot(triangleNormal) >= edge_tolerance) {

							if (dist_a > 0f) {
								hitFraction = reportHit(triangleNormal, distance, partId, triangleIndex);
							}
							else {
								Vector3f tmp = new Vector3f();
								tmp.negate(triangleNormal);
								hitFraction = reportHit(tmp, distance, partId, triangleIndex);
							}
						}
					}
				}
			}
		}
	}

	public abstract float reportHit(Vector3f hitNormalLocal, float hitFraction, int partId, int triangleIndex );

}