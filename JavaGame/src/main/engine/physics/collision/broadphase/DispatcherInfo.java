package main.engine.physics.collision.broadphase;

import com.bulletphysics.collision.broadphase.DispatchFunc;
import com.bulletphysics.linearmath.IDebugDraw;

/**
 * Current state of {@link Dispatcher}.
 * 
 * @author jezek2
 */
public class DispatcherInfo {

	public float timeStep;
	public int stepCount;
	public DispatchFunc dispatchFunc;
	public float timeOfImpact;
	public boolean useContinuous;
	public IDebugDraw debugDraw;
	public boolean enableSatConvex;
	public boolean enableSPU = true;
	public boolean useEpa = true;
	public float allowedCcdPenetration = 0.04f;
	//btStackAlloc*	m_stackAllocator;

	public DispatcherInfo() {
		dispatchFunc = DispatchFunc.DISPATCH_DISCRETE;
		timeOfImpact = 1f;
	}
	
}