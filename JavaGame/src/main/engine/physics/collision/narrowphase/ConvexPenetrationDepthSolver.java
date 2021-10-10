package main.engine.physics.collision.narrowphase;

import org.joml.Vector3f;

import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.IDebugDraw;

import main.engine.utility.physUtils.Transform;

/**
 * ConvexPenetrationDepthSolver provides an interface for penetration depth calculation.
 * 
 * @author jezek2
 */
public abstract class ConvexPenetrationDepthSolver {

	public abstract boolean calcPenDepth(SimplexSolverInterface simplexSolver,
			ConvexShape convexA, ConvexShape convexB,
			Transform transA, Transform transB,
			Vector3f v, Vector3f pa, Vector3f pb,
			IDebugDraw debugDraw/*, btStackAlloc* stackAlloc*/);
	
}