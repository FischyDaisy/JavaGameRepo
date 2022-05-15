package com.newton;

import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class NewtonCompoundCollision extends NewtonCollision {
	
	protected NewtonCompoundCollision(MemoryAddress address) {
		super(address);
	}
	
	public static NewtonCollision create(NewtonWorld world, int shapeID) {
		return new NewtonCompoundCollision(Newton_h.NewtonCreateCompoundCollision(world.address, shapeID));
	}
	
	public static NewtonCollision create(NewtonWorld world, NewtonMesh mesh, float hullTolerance, int shapeID, int subShapeID) {
		return new NewtonCompoundCollision(Newton_h.NewtonCreateCompoundCollisionFromMesh(world.address, mesh.address, hullTolerance, shapeID, subShapeID));
	}
	
	public void collisionBeginAddRemove() {
		Newton_h.NewtonCompoundCollisionBeginAddRemove(address);
	}
	
	public CollisionNode collisionAddSubCollision(NewtonCollision convexCollision) {
		return new CollisionNode(Newton_h.NewtonCompoundCollisionAddSubCollision(address, convexCollision.address));
	}
	
	public record CollisionNode(MemoryAddress address) {}
}
