package main.engine.physics.collision.broadphase;

import org.joml.Vector3f;

/**
 * AxisSweep3_32 allows higher precision quantization and more objects compared
 * to the {@link AxisSweep3} sweep and prune. This comes at the cost of more memory
 * per handle, and a bit slower performance.
 *
 * @author jezek2
 */
public class AxisSweep3_32 extends AxisSweep3Internal {

	public AxisSweep3_32(Vector3f worldAabbMin, Vector3f worldAabbMax) {
		this(worldAabbMin, worldAabbMax, 1500000, null);
	}

	public AxisSweep3_32(Vector3f worldAabbMin, Vector3f worldAabbMax, int maxHandles) {
		this(worldAabbMin, worldAabbMax, maxHandles, null);
	}
	
	public AxisSweep3_32(Vector3f worldAabbMin, Vector3f worldAabbMax, int maxHandles/* = 1500000*/, OverlappingPairCache pairCache/* = 0*/) {
		super(worldAabbMin, worldAabbMax, 0xfffffffe, 0x7fffffff, maxHandles, pairCache);
		// 1 handle is reserved as sentinel
		assert (maxHandles > 1 && maxHandles < 2147483647);
	}
	
	@Override
	protected EdgeArray createEdgeArray(int size) {
		return new EdgeArrayImpl(size);
	}

	@Override
	protected Handle createHandle() {
		return new HandleImpl();
	}
	
	protected int getMask() {
		return 0xFFFFFFFF;
	}
	
	protected static class EdgeArrayImpl extends EdgeArray {
		private int[] pos;
		private int[] handle;

		public EdgeArrayImpl(int size) {
			pos = new int[size];
			handle = new int[size];
		}
		
		@Override
		public void swap(int idx1, int idx2) {
			int tmpPos = pos[idx1];
			int tmpHandle = handle[idx1];
			
			pos[idx1] = pos[idx2];
			handle[idx1] = handle[idx2];
			
			pos[idx2] = tmpPos;
			handle[idx2] = tmpHandle;
		}
		
		@Override
		public void set(int dest, int src) {
			pos[dest] = pos[src];
			handle[dest] = handle[src];
		}
		
		@Override
		public int getPos(int index) {
			return pos[index];
		}

		@Override
		public void setPos(int index, int value) {
			pos[index] = value;
		}

		@Override
		public int getHandle(int index) {
			return handle[index];
		}

		@Override
		public void setHandle(int index, int value) {
			handle[index] = value;
		}
	}
	
	protected static class HandleImpl extends Handle {
		private int minEdges0;
		private int minEdges1;
		private int minEdges2;

		private int maxEdges0;
		private int maxEdges1;
		private int maxEdges2;
		
		@Override
		public int getMinEdges(int edgeIndex) {
			switch (edgeIndex) {
				default:
				case 0: return minEdges0;
				case 1: return minEdges1;
				case 2: return minEdges2;
			}
		}
		
		@Override
		public void setMinEdges(int edgeIndex, int value) {
			switch (edgeIndex) {
				case 0: minEdges0 = value; break;
				case 1: minEdges1 = value; break;
				case 2: minEdges2 = value; break;
			}
		}
		
		@Override
		public int getMaxEdges(int edgeIndex) {
			switch (edgeIndex) {
				default:
				case 0: return maxEdges0;
				case 1: return maxEdges1;
				case 2: return maxEdges2;
			}
		}
		
		@Override
		public void setMaxEdges(int edgeIndex, int value) {
			switch (edgeIndex) {
				case 0: maxEdges0 = value; break;
				case 1: maxEdges1 = value; break;
				case 2: maxEdges2 = value; break;
			}
		}
	}

}