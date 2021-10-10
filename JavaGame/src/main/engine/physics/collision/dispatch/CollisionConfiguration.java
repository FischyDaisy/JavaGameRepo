package main.engine.physics.collision.dispatch;

import main.engine.physics.collision.broadphase.BroadphaseNativeType;

/**
 * CollisionConfiguration allows to configure Bullet default collision algorithms.
 * 
 * @author jezek2
 */
public abstract class CollisionConfiguration {

	/*
	///memory pools
	virtual btPoolAllocator* getPersistentManifoldPool() = 0;
	virtual btPoolAllocator* getCollisionAlgorithmPool() = 0;
	virtual btStackAlloc*	getStackAllocator() = 0;
	 */
	
	public abstract CollisionAlgorithmCreateFunc getCollisionAlgorithmCreateFunc(BroadphaseNativeType proxyType0, BroadphaseNativeType proxyType1);
	
}