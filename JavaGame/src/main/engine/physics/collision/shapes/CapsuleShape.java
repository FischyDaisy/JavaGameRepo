package main.engine.physics.collision.shapes;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.VectorUtil;

import main.engine.physics.BulletGlobals;
import main.engine.physics.collision.broadphase.BroadphaseNativeType;
import main.engine.utility.physUtils.Transform;

/**
 * CapsuleShape represents a capsule around the Y axis, there is also the
 * {@link CapsuleShapeX} aligned around the X axis and {@link CapsuleShapeZ} around
 * the Z axis.<p>
 *
 * The total height is height+2*radius, so the height is just the height between
 * the center of each "sphere" of the capsule caps.<p>
 *
 * CapsuleShape is a convex hull of two spheres. The {@link MultiSphereShape} is
 * a more general collision shape that takes the convex hull of multiple sphere,
 * so it can also represent a capsule when just using two spheres.
 * 
 * @author jezek2
 */
public class CapsuleShape extends ConvexInternalShape {
	
	protected int upAxis;

	// only used for CapsuleShapeZ and CapsuleShapeX subclasses.
	CapsuleShape() {
	}
	
	public CapsuleShape(float radius, float height) {
		upAxis = 1;
		implicitShapeDimensions.set(radius, 0.5f * height, radius);
	}

	@Override
	public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec0, Vector3f out) {
		Vector3f supVec = out;
		supVec.set(0f, 0f, 0f);

		float maxDot = -1e30f;

		Vector3f vec = Stack.alloc(vec0);
		float lenSqr = vec.lengthSquared();
		if (lenSqr < 0.0001f) {
			vec.set(1f, 0f, 0f);
		}
		else {
			float rlen = 1f / (float) Math.sqrt(lenSqr);
			vec.scale(rlen);
		}

		Vector3f vtx = Stack.alloc(Vector3f.class);
		float newDot;

		float radius = getRadius();

		Vector3f tmp1 = Stack.alloc(Vector3f.class);
		Vector3f tmp2 = Stack.alloc(Vector3f.class);
		Vector3f pos = Stack.alloc(Vector3f.class);

		{
			pos.set(0f, 0f, 0f);
			VectorUtil.setCoord(pos, getUpAxis(), getHalfHeight());
			
			VectorUtil.mul(tmp1, vec, localScaling);
			tmp1.scale(radius);
			tmp2.scale(getMargin(), vec);
			vtx.add(pos, tmp1);
			vtx.sub(tmp2);
			newDot = vec.dot(vtx);
			if (newDot > maxDot) {
				maxDot = newDot;
				supVec.set(vtx);
			}
		}
		{
			pos.set(0f, 0f, 0f);
			VectorUtil.setCoord(pos, getUpAxis(), -getHalfHeight());
			
			VectorUtil.mul(tmp1, vec, localScaling);
			tmp1.scale(radius);
			tmp2.scale(getMargin(), vec);
			vtx.add(pos, tmp1);
			vtx.sub(tmp2);
			newDot = vec.dot(vtx);
			if (newDot > maxDot) {
				maxDot = newDot;
				supVec.set(vtx);
			}
		}

		return out;
	}

	@Override
	public void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector3f[] vectors, Vector3f[] supportVerticesOut, int numVectors) {
		// TODO: implement
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void calculateLocalInertia(float mass, Vector3f inertia) {
		// as an approximation, take the inertia of the box that bounds the spheres

		Transform ident = Stack.alloc(Transform.class);
		ident.setIdentity();

		float radius = getRadius();

		Vector3f halfExtents = Stack.alloc(Vector3f.class);
		halfExtents.set(radius, radius, radius);
		VectorUtil.setCoord(halfExtents, getUpAxis(), radius + getHalfHeight());

		float margin = BulletGlobals.CONVEX_DISTANCE_MARGIN;

		float lx = 2f * (halfExtents.x + margin);
		float ly = 2f * (halfExtents.y + margin);
		float lz = 2f * (halfExtents.z + margin);
		float x2 = lx * lx;
		float y2 = ly * ly;
		float z2 = lz * lz;
		float scaledmass = mass * 0.08333333f;

		inertia.x = scaledmass * (y2 + z2);
		inertia.y = scaledmass * (x2 + z2);
		inertia.z = scaledmass * (x2 + y2);
	}

	@Override
	public BroadphaseNativeType getShapeType() {
		return BroadphaseNativeType.CAPSULE_SHAPE_PROXYTYPE;
	}
	
	@Override
	public void getAabb(Transform t, Vector3f aabbMin, Vector3f aabbMax) {
		Vector3f tmp = Stack.alloc(Vector3f.class);

		Vector3f halfExtents = Stack.alloc(Vector3f.class);
		halfExtents.set(getRadius(), getRadius(), getRadius());
		VectorUtil.setCoord(halfExtents, upAxis, getRadius() + getHalfHeight());

		halfExtents.x += getMargin();
		halfExtents.y += getMargin();
		halfExtents.z += getMargin();

		Matrix3f abs_b = Stack.alloc(Matrix3f.class);
		abs_b.set(t.basis);
		MatrixUtil.absolute(abs_b);

		Vector3f center = t.origin;
		Vector3f extent = Stack.alloc(Vector3f.class);

		abs_b.getRow(0, tmp);
		extent.x = tmp.dot(halfExtents);
		abs_b.getRow(1, tmp);
		extent.y = tmp.dot(halfExtents);
		abs_b.getRow(2, tmp);
		extent.z = tmp.dot(halfExtents);

		aabbMin.sub(center, extent);
		aabbMax.add(center, extent);
	}

	@Override
	public String getName() {
		return "CapsuleShape";
	}
	
	public int getUpAxis() {
		return upAxis;
	}
	
	public float getRadius() {
		int radiusAxis = (upAxis + 2) % 3;
		return VectorUtil.getCoord(implicitShapeDimensions, radiusAxis);
	}

	public float getHalfHeight() {
		return VectorUtil.getCoord(implicitShapeDimensions, upAxis);
	}

}