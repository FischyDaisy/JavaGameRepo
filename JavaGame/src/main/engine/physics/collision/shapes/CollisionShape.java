package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

import main.engine.physics.collision.broadphase.BroadphaseNativeType;
import main.engine.utility.physUtils.Transform;

/**
 * CollisionShape class provides an interface for collision shapes that can be
 * shared among {@link CollisionObject}s.
 * 
 * @author jezek2
 */
public abstract class CollisionShape {

	//protected final BulletStack stack = BulletStack.get();

	protected Object userPointer;
	
	///getAabb returns the axis aligned bounding box in the coordinate frame of the given transform t.
	public abstract void getAabb(Transform t, Vector3f aabbMin, Vector3f aabbMax);

	public void getBoundingSphere(Vector3f center, float[] radius) {
		Vector3f tmp = new Vector3f();

		Transform tr = new Transform();
		tr.setIdentity();
		Vector3f aabbMin = new Vector3f(), aabbMax = new Vector3f();

		getAabb(tr, aabbMin, aabbMax);

		tmp.sub(aabbMax, aabbMin);
		radius[0] = tmp.length() * 0.5f;

		tmp.add(aabbMin, aabbMax);
		center.scale(0.5f, tmp);
	}

	///getAngularMotionDisc returns the maximus radius needed for Conservative Advancement to handle time-of-impact with rotations.
	public float getAngularMotionDisc() {
		Vector3f center = new Vector3f();
		float[] disc = new float[1]; // TODO: stack
		getBoundingSphere(center, disc);
		disc[0] += center.length();
		return disc[0];
	}

	///calculateTemporalAabb calculates the enclosing aabb for the moving object over interval [0..timeStep)
	///result is conservative
	public void calculateTemporalAabb(Transform curTrans, Vector3f linvel, Vector3f angvel, float timeStep, Vector3f temporalAabbMin, Vector3f temporalAabbMax) {
		//start with static aabb
		getAabb(curTrans, temporalAabbMin, temporalAabbMax);

		float temporalAabbMaxx = temporalAabbMax.x;
		float temporalAabbMaxy = temporalAabbMax.y;
		float temporalAabbMaxz = temporalAabbMax.z;
		float temporalAabbMinx = temporalAabbMin.x;
		float temporalAabbMiny = temporalAabbMin.y;
		float temporalAabbMinz = temporalAabbMin.z;

		// add linear motion
		Vector3f linMotion = new Vector3f(linvel);
		linMotion.scale(timeStep);

		//todo: simd would have a vector max/min operation, instead of per-element access
		if (linMotion.x > 0f) {
			temporalAabbMaxx += linMotion.x;
		}
		else {
			temporalAabbMinx += linMotion.x;
		}
		if (linMotion.y > 0f) {
			temporalAabbMaxy += linMotion.y;
		}
		else {
			temporalAabbMiny += linMotion.y;
		}
		if (linMotion.z > 0f) {
			temporalAabbMaxz += linMotion.z;
		}
		else {
			temporalAabbMinz += linMotion.z;
		}

		//add conservative angular motion
		float angularMotion = angvel.length() * getAngularMotionDisc() * timeStep;
		Vector3f angularMotion3d = new Vector3f();
		angularMotion3d.set(angularMotion, angularMotion, angularMotion);
		temporalAabbMin.set(temporalAabbMinx, temporalAabbMiny, temporalAabbMinz);
		temporalAabbMax.set(temporalAabbMaxx, temporalAabbMaxy, temporalAabbMaxz);

		temporalAabbMin.sub(angularMotion3d);
		temporalAabbMax.add(angularMotion3d);
	}

//#ifndef __SPU__
	public boolean isPolyhedral() {
		return getShapeType().isPolyhedral();
	}

	public boolean isConvex() {
		return getShapeType().isConvex();
	}

	public boolean isConcave() {
		return getShapeType().isConcave();
	}

	public boolean isCompound() {
		return getShapeType().isCompound();
	}

	///isInfinite is used to catch simulation error (aabb check)
	public boolean isInfinite() {
		return getShapeType().isInfinite();
	}

	public abstract BroadphaseNativeType getShapeType();

	public abstract void setLocalScaling(Vector3f scaling);
	
	// TODO: returns const
	public abstract Vector3f getLocalScaling(Vector3f out);

	public abstract void calculateLocalInertia(float mass, Vector3f inertia);


//debugging support
	public abstract String getName();
//#endif //__SPU__
	public abstract void setMargin(float margin);

	public abstract float getMargin();
	
	// optional user data pointer
	public void setUserPointer(Object userPtr) {
		userPointer = userPtr;
	}

	public Object getUserPointer() {
		return userPointer;
	}
	
}