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
	
	public void beginAddRemove() {
		Newton_h.NewtonCompoundCollisionBeginAddRemove(address);
	}
	
	public Node addSubCollision(NewtonCollision convexCollision) {
		return new Node(Newton_h.NewtonCompoundCollisionAddSubCollision(address, convexCollision.address));
	}
	
	public void removeSubCollision(Node collisionNode) {
		Newton_h.NewtonCompoundCollisionRemoveSubCollision(address, collisionNode.address());
	}
	
	public void removeSubCollisionByIndex(int index) {
		Newton_h.NewtonCompoundCollisionRemoveSubCollisionByIndex(address, index);
	}
	
	public void setSubCollisionMatrix(Node collisionNode, float[] matrix) {
		//NewtonCompoundCollisionSetSubCollisionMatrix
	}
	
	public void endAddRemove() {
		Newton_h.NewtonCompoundCollisionEndAddRemove(address);
	}
	
	public Node getFirstNode() {
		return new Node(Newton_h.NewtonCompoundCollisionGetFirstNode(address));
	}
	
	public Node getNextNode(Node nextNode) {
		MemoryAddress nodePtr = Newton_h.NewtonCompoundCollisionGetNextNode(address, nextNode.address());
		return nodePtr.equals(MemoryAddress.NULL) ? null : new Node(nodePtr);
	}
	
	public Node getNodeByIndex(int index) {
		return new Node(Newton_h.NewtonCompoundCollisionGetNodeByIndex(address, index));
	}
	
	public int getNodeIndex(Node collisionNode) {
		return Newton_h.NewtonCompoundCollisionGetNodeIndex(address, collisionNode.address());
	}
	
	public NewtonCollision getCollisionFromNode(Node collisionNode) {
		return NewtonCollision.wrap(Newton_h.NewtonCompoundCollisionGetCollisionFromNode(address, collisionNode.address()));
	}
	
	public record Node(MemoryAddress address) {}
}
