package main.engine.physics.collision.shapes;

import java.io.Serializable;

import org.joml.Vector3f;


/**
 * OptimizedBvhNode contains both internal and leaf node information.
 * 
 * @author jezek2
 */
public class OptimizedBvhNode implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public final Vector3f aabbMinOrg = new Vector3f();
	public final Vector3f aabbMaxOrg = new Vector3f();

	public int escapeIndex;

	// for child nodes
	public int subPart;
	public int triangleIndex;
	
	public void set(OptimizedBvhNode n) {
		aabbMinOrg.set(n.aabbMinOrg);
		aabbMaxOrg.set(n.aabbMaxOrg);
		escapeIndex = n.escapeIndex;
		subPart = n.subPart;
		triangleIndex = n.triangleIndex;
	}

}