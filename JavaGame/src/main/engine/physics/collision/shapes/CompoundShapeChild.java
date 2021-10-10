package main.engine.physics.collision.shapes;

import main.engine.physics.collision.broadphase.BroadphaseNativeType;
import main.engine.utility.physUtils.Transform;

/**
 * Compound shape child.
 * 
 * @author jezek2
 */
public class CompoundShapeChild {
	
	public final Transform transform = new Transform();
	public CollisionShape childShape;
	public BroadphaseNativeType childShapeType;
	public float childMargin;

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CompoundShapeChild)) return false;
		CompoundShapeChild child = (CompoundShapeChild)obj;
		return transform.equals(child.transform) &&
		       childShape == child.childShape &&
		       childShapeType == child.childShapeType &&
		       childMargin == child.childMargin;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + transform.hashCode();
		hash = 19 * hash + childShape.hashCode();
		hash = 19 * hash + childShapeType.hashCode();
		hash = 19 * hash + Float.floatToIntBits(childMargin);
		return hash;
	}

}