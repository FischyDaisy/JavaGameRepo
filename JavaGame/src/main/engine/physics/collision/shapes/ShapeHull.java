package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

import com.bulletphysics.linearmath.MiscUtil;

import main.engine.utility.physUtils.IntArrayList;
import main.engine.utility.physUtils.ObjectArrayList;

/**
 * ShapeHull takes a {@link ConvexShape}, builds the convex hull using {@link HullLibrary}
 * and provides triangle indices and vertices.
 * 
 * @author jezek2
 */
public class ShapeHull {

	protected ObjectArrayList<Vector3f> vertices = new ObjectArrayList<Vector3f>();
	protected IntArrayList indices = new IntArrayList();
	protected int numIndices;
	protected ConvexShape shape;

	protected ObjectArrayList<Vector3f> unitSpherePoints = new ObjectArrayList<Vector3f>();

	public ShapeHull(ConvexShape shape) {
		this.shape = shape;
		this.vertices.clear();
		this.indices.clear();
		this.numIndices = 0;

		MiscUtil.resize(unitSpherePoints, NUM_UNITSPHERE_POINTS+ConvexShape.MAX_PREFERRED_PENETRATION_DIRECTIONS*2, Vector3f.class);
		for (int i=0; i<constUnitSpherePoints.size(); i++) {
			unitSpherePoints.getQuick(i).set(constUnitSpherePoints.getQuick(i));
		}
	}

	public boolean buildHull(float margin) {
		Vector3f norm = Stack.alloc(Vector3f.class);

		int numSampleDirections = NUM_UNITSPHERE_POINTS;
		{
			int numPDA = shape.getNumPreferredPenetrationDirections();
			if (numPDA != 0) {
				for (int i=0; i<numPDA; i++) {
					shape.getPreferredPenetrationDirection(i, norm);
					unitSpherePoints.getQuick(numSampleDirections).set(norm);
					numSampleDirections++;
				}
			}
		}

		ObjectArrayList<Vector3f> supportPoints = new ObjectArrayList<Vector3f>();
		MiscUtil.resize(supportPoints, NUM_UNITSPHERE_POINTS + ConvexShape.MAX_PREFERRED_PENETRATION_DIRECTIONS * 2, Vector3f.class);

		for (int i=0; i<numSampleDirections; i++) {
			shape.localGetSupportingVertex(unitSpherePoints.getQuick(i), supportPoints.getQuick(i));
		}

		HullDesc hd = new HullDesc();
		hd.flags = HullFlags.TRIANGLES;
		hd.vcount = numSampleDirections;

		//#ifdef BT_USE_DOUBLE_PRECISION
		//hd.mVertices = &supportPoints[0];
		//hd.mVertexStride = sizeof(btVector3);
		//#else
		hd.vertices = supportPoints;
		//hd.vertexStride = 3 * 4;
		//#endif

		HullLibrary hl = new HullLibrary();
		HullResult hr = new HullResult();
		if (!hl.createConvexHull(hd, hr)) {
			return false;
		}

		MiscUtil.resize(vertices, hr.numOutputVertices, Vector3f.class);

		for (int i=0; i<hr.numOutputVertices; i++) {
			vertices.getQuick(i).set(hr.outputVertices.getQuick(i));
		}
		numIndices = hr.numIndices;
		MiscUtil.resize(indices, numIndices, 0);
		for (int i=0; i<numIndices; i++) {
			indices.set(i, hr.indices.get(i));
		}

		// free temporary hull result that we just copied
		hl.releaseResult(hr);

		return true;
	}

	public int numTriangles() {
		return numIndices / 3;
	}

	public int numVertices() {
		return vertices.size();
	}

	public int numIndices() {
		return numIndices;
	}

	public ObjectArrayList<Vector3f> getVertexPointer() {
		return vertices;
	}

	public IntArrayList getIndexPointer() {
		return indices;
	}

	////////////////////////////////////////////////////////////////////////////
	
	private static int NUM_UNITSPHERE_POINTS = 42;
	
	private static ObjectArrayList<Vector3f> constUnitSpherePoints = new ObjectArrayList<Vector3f>();
	
	static {
		constUnitSpherePoints.add(new Vector3f(0.000000f, -0.000000f, -1.000000f));
		constUnitSpherePoints.add(new Vector3f(0.723608f, -0.525725f, -0.447219f));
		constUnitSpherePoints.add(new Vector3f(-0.276388f, -0.850649f, -0.447219f));
		constUnitSpherePoints.add(new Vector3f(-0.894426f, -0.000000f, -0.447216f));
		constUnitSpherePoints.add(new Vector3f(-0.276388f, 0.850649f, -0.447220f));
		constUnitSpherePoints.add(new Vector3f(0.723608f, 0.525725f, -0.447219f));
		constUnitSpherePoints.add(new Vector3f(0.276388f, -0.850649f, 0.447220f));
		constUnitSpherePoints.add(new Vector3f(-0.723608f, -0.525725f, 0.447219f));
		constUnitSpherePoints.add(new Vector3f(-0.723608f, 0.525725f, 0.447219f));
		constUnitSpherePoints.add(new Vector3f(0.276388f, 0.850649f, 0.447219f));
		constUnitSpherePoints.add(new Vector3f(0.894426f, 0.000000f, 0.447216f));
		constUnitSpherePoints.add(new Vector3f(-0.000000f, 0.000000f, 1.000000f));
		constUnitSpherePoints.add(new Vector3f(0.425323f, -0.309011f, -0.850654f));
		constUnitSpherePoints.add(new Vector3f(-0.162456f, -0.499995f, -0.850654f));
		constUnitSpherePoints.add(new Vector3f(0.262869f, -0.809012f, -0.525738f));
		constUnitSpherePoints.add(new Vector3f(0.425323f, 0.309011f, -0.850654f));
		constUnitSpherePoints.add(new Vector3f(0.850648f, -0.000000f, -0.525736f));
		constUnitSpherePoints.add(new Vector3f(-0.525730f, -0.000000f, -0.850652f));
		constUnitSpherePoints.add(new Vector3f(-0.688190f, -0.499997f, -0.525736f));
		constUnitSpherePoints.add(new Vector3f(-0.162456f, 0.499995f, -0.850654f));
		constUnitSpherePoints.add(new Vector3f(-0.688190f, 0.499997f, -0.525736f));
		constUnitSpherePoints.add(new Vector3f(0.262869f, 0.809012f, -0.525738f));
		constUnitSpherePoints.add(new Vector3f(0.951058f, 0.309013f, 0.000000f));
		constUnitSpherePoints.add(new Vector3f(0.951058f, -0.309013f, 0.000000f));
		constUnitSpherePoints.add(new Vector3f(0.587786f, -0.809017f, 0.000000f));
		constUnitSpherePoints.add(new Vector3f(0.000000f, -1.000000f, 0.000000f));
		constUnitSpherePoints.add(new Vector3f(-0.587786f, -0.809017f, 0.000000f));
		constUnitSpherePoints.add(new Vector3f(-0.951058f, -0.309013f, -0.000000f));
		constUnitSpherePoints.add(new Vector3f(-0.951058f, 0.309013f, -0.000000f));
		constUnitSpherePoints.add(new Vector3f(-0.587786f, 0.809017f, -0.000000f));
		constUnitSpherePoints.add(new Vector3f(-0.000000f, 1.000000f, -0.000000f));
		constUnitSpherePoints.add(new Vector3f(0.587786f, 0.809017f, -0.000000f));
		constUnitSpherePoints.add(new Vector3f(0.688190f, -0.499997f, 0.525736f));
		constUnitSpherePoints.add(new Vector3f(-0.262869f, -0.809012f, 0.525738f));
		constUnitSpherePoints.add(new Vector3f(-0.850648f, 0.000000f, 0.525736f));
		constUnitSpherePoints.add(new Vector3f(-0.262869f, 0.809012f, 0.525738f));
		constUnitSpherePoints.add(new Vector3f(0.688190f, 0.499997f, 0.525736f));
		constUnitSpherePoints.add(new Vector3f(0.525730f, 0.000000f, 0.850652f));
		constUnitSpherePoints.add(new Vector3f(0.162456f, -0.499995f, 0.850654f));
		constUnitSpherePoints.add(new Vector3f(-0.425323f, -0.309011f, 0.850654f));
		constUnitSpherePoints.add(new Vector3f(-0.425323f, 0.309011f, 0.850654f));
		constUnitSpherePoints.add(new Vector3f(0.162456f, 0.499995f, 0.850654f));
	}
	
}