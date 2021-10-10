package main.engine.physics.collision.broadphase;

import org.joml.Vector3f;

import main.engine.utility.physUtils.ObjectArrayList;

/**
 * SimpleBroadphase is just a unit-test for {@link AxisSweep3}, {@link AxisSweep3_32},
 * or {@link DbvtBroadphase}, so use those classes instead. It is a brute force AABB
 * culling broadphase based on O(n^2) AABB checks.
 * 
 * @author jezek2
 */
public class SimpleBroadphase extends BroadphaseInterface {

	private final ObjectArrayList<SimpleBroadphaseProxy> handles = new ObjectArrayList<SimpleBroadphaseProxy>();
	private int maxHandles;						// max number of handles
	private OverlappingPairCache pairCache;
	private boolean ownsPairCache;

	public SimpleBroadphase() {
		this(16384, null);
	}

	public SimpleBroadphase(int maxProxies) {
		this(maxProxies, null);
	}
	
	public SimpleBroadphase(int maxProxies, OverlappingPairCache overlappingPairCache) {
		this.pairCache = overlappingPairCache;

		if (overlappingPairCache == null) {
			pairCache = new HashedOverlappingPairCache();
			ownsPairCache = true;
		}
	}

	public BroadphaseProxy createProxy(Vector3f aabbMin, Vector3f aabbMax, BroadphaseNativeType shapeType, Object userPtr, short collisionFilterGroup, short collisionFilterMask, Dispatcher dispatcher, Object multiSapProxy) {
		assert (aabbMin.x <= aabbMax.x && aabbMin.y <= aabbMax.y && aabbMin.z <= aabbMax.z);

		SimpleBroadphaseProxy proxy = new SimpleBroadphaseProxy(aabbMin, aabbMax, shapeType, userPtr, collisionFilterGroup, collisionFilterMask, multiSapProxy);
		proxy.uniqueId = handles.size();
		handles.add(proxy);
		return proxy;
	}

	public void destroyProxy(BroadphaseProxy proxyOrg, Dispatcher dispatcher) {
		handles.remove(proxyOrg);

		pairCache.removeOverlappingPairsContainingProxy(proxyOrg, dispatcher);
	}

	public void setAabb(BroadphaseProxy proxy, Vector3f aabbMin, Vector3f aabbMax, Dispatcher dispatcher) {
		SimpleBroadphaseProxy sbp = (SimpleBroadphaseProxy)proxy;
		sbp.min.set(aabbMin);
		sbp.max.set(aabbMax);
	}

	private static boolean aabbOverlap(SimpleBroadphaseProxy proxy0, SimpleBroadphaseProxy proxy1) {
		return proxy0.min.x <= proxy1.max.x && proxy1.min.x <= proxy0.max.x &&
				proxy0.min.y <= proxy1.max.y && proxy1.min.y <= proxy0.max.y &&
				proxy0.min.z <= proxy1.max.z && proxy1.min.z <= proxy0.max.z;
	}

	public void calculateOverlappingPairs(Dispatcher dispatcher) {
		for (int i=0; i<handles.size(); i++) {
			SimpleBroadphaseProxy proxy0 = handles.getQuick(i);
			for (int j=0; j<handles.size(); j++) {
				SimpleBroadphaseProxy proxy1 = handles.getQuick(j);
				if (proxy0 == proxy1) continue;
				
				if (aabbOverlap(proxy0, proxy1)) {
					if (pairCache.findPair(proxy0, proxy1) == null) {
						pairCache.addOverlappingPair(proxy0, proxy1);
					}
				}
				else {
					// JAVA NOTE: pairCache.hasDeferredRemoval() = true is not implemented
					
					if (!pairCache.hasDeferredRemoval()) {
						if (pairCache.findPair(proxy0, proxy1) != null) {
							pairCache.removeOverlappingPair(proxy0, proxy1, dispatcher);
						}
					}
				}
			}
		}
	}

	public OverlappingPairCache getOverlappingPairCache() {
		return pairCache;
	}

	public void getBroadphaseAabb(Vector3f aabbMin, Vector3f aabbMax) {
		aabbMin.set(-1e30f, -1e30f, -1e30f);
		aabbMax.set(1e30f, 1e30f, 1e30f);
	}

	public void printStats() {
//		System.out.printf("btSimpleBroadphase.h\n");
//		System.out.printf("numHandles = %d, maxHandles = %d\n", /*numHandles*/ handles.size(), maxHandles);
	}
	
}