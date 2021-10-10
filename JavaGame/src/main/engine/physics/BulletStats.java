package main.engine.physics;

import org.joml.Vector3f;

import com.bulletphysics.linearmath.CProfileManager;
import com.bulletphysics.linearmath.Clock;

public class BulletStats {
	
	public static int gTotalContactPoints;
	
	// GjkPairDetector
	// temp globals, to improve GJK/EPA/penetration calculations
	public static int gNumDeepPenetrationChecks = 0;
	public static int gNumGjkChecks = 0;
	public static int gNumSplitImpulseRecoveries = 0;
	
	public static int gNumAlignedAllocs;
	public static int gNumAlignedFree;
	public static int gTotalBytesAlignedAllocs;	
	
	public static int gPickingConstraintId = 0;
	public static final Vector3f gOldPickingPos = new Vector3f();
	public static float gOldPickingDist = 0.f;
	
	public static int gOverlappingPairs = 0;
	public static int gRemovePairs = 0;
	public static int gAddedPairs = 0;
	public static int gFindPairs = 0;
	
	public static final Clock gProfileClock = new Clock();

	// DiscreteDynamicsWorld:
	public static int gNumClampedCcdMotions = 0;

	// JAVA NOTE: added for statistics in applet demo
	public static long stepSimulationTime;
	public static long updateTime;
	
	private static boolean enableProfile = false;
	
	////////////////////////////////////////////////////////////////////////////
	
	public static boolean isProfileEnabled() {
		return enableProfile;
	}

	public static void setProfileEnabled(boolean b) {
		enableProfile = b;
	}
	
	public static long profileGetTicks() {
		long ticks = gProfileClock.getTimeMicroseconds();
		return ticks;
	}

	public static float profileGetTickRate() {
		//return 1000000f;
		return 1000f;
	}
	
	/**
	 * Pushes profile node. Use try/finally block to call {@link #popProfile} method.
	 * 
	 * @param name must be {@link String#intern interned} String (not needed for String literals)
	 */
	public static void pushProfile(String name) {
		if (enableProfile) {
			CProfileManager.startProfile(name);
		}
	}
	
	/**
	 * Pops profile node.
	 */
	public static void popProfile() {
		if (enableProfile) {
			CProfileManager.stopProfile();
		}
	}
	
}