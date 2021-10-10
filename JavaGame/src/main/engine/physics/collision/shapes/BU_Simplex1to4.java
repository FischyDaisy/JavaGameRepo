package main.engine.physics.collision.shapes;

import org.joml.Vector3f;

import main.engine.physics.collision.broadphase.BroadphaseNativeType;

/**
 * BU_Simplex1to4 implements feature based and implicit simplex of up to 4 vertices
 * (tetrahedron, triangle, line, vertex).
 * 
 * @author jezek2
 */
public class BU_Simplex1to4 extends PolyhedralConvexShape {

	protected int numVertices = 0;
	protected Vector3f[] vertices = new Vector3f[4];

	public BU_Simplex1to4() {
	}

	public BU_Simplex1to4(Vector3f pt0) {
		addVertex(pt0);
	}

	public BU_Simplex1to4(Vector3f pt0, Vector3f pt1) {
		addVertex(pt0);
		addVertex(pt1);
	}

	public BU_Simplex1to4(Vector3f pt0, Vector3f pt1, Vector3f pt2) {
		addVertex(pt0);
		addVertex(pt1);
		addVertex(pt2);
	}

	public BU_Simplex1to4(Vector3f pt0, Vector3f pt1, Vector3f pt2, Vector3f pt3) {
		addVertex(pt0);
		addVertex(pt1);
		addVertex(pt2);
		addVertex(pt3);
	}
	
	public void reset() {
		numVertices = 0;
	}
	
	@Override
	public BroadphaseNativeType getShapeType() {
		return BroadphaseNativeType.TETRAHEDRAL_SHAPE_PROXYTYPE;
	}
	
	public void addVertex(Vector3f pt) {
		if (vertices[numVertices] == null) {
			vertices[numVertices] = new Vector3f();
		}
		
		vertices[numVertices++] = pt;

		recalcLocalAabb();
	}

	
	@Override
	public int getNumVertices() {
		return numVertices;
	}

	@Override
	public int getNumEdges() {
		// euler formula, F-E+V = 2, so E = F+V-2

		switch (numVertices) {
			case 0: return 0;
			case 1: return 0;
			case 2: return 1;
			case 3: return 3;
			case 4: return 6;
		}

		return 0;
	}

	@Override
	public void getEdge(int i, Vector3f pa, Vector3f pb) {
		switch (numVertices) {
			case 2:
				pa.set(vertices[0]);
				pb.set(vertices[1]);
				break;
			case 3:
				switch (i) {
					case 0:
						pa.set(vertices[0]);
						pb.set(vertices[1]);
						break;
					case 1:
						pa.set(vertices[1]);
						pb.set(vertices[2]);
						break;
					case 2:
						pa.set(vertices[2]);
						pb.set(vertices[0]);
						break;
				}
				break;
			case 4:
				switch (i) {
					case 0:
						pa.set(vertices[0]);
						pb.set(vertices[1]);
						break;
					case 1:
						pa.set(vertices[1]);
						pb.set(vertices[2]);
						break;
					case 2:
						pa.set(vertices[2]);
						pb.set(vertices[0]);
						break;
					case 3:
						pa.set(vertices[0]);
						pb.set(vertices[3]);
						break;
					case 4:
						pa.set(vertices[1]);
						pb.set(vertices[3]);
						break;
					case 5:
						pa.set(vertices[2]);
						pb.set(vertices[3]);
						break;
				}
		}
	}

	@Override
	public void getVertex(int i, Vector3f vtx) {
		vtx.set(vertices[i]);
	}

	@Override
	public int getNumPlanes() {
		switch (numVertices) {
			case 0: return 0;
			case 1: return 0;
			case 2: return 0;
			case 3: return 2;
			case 4: return 4;
		}
		return 0;
	}

	@Override
	public void getPlane(Vector3f planeNormal, Vector3f planeSupport, int i) {
	}
	
	public int getIndex(int i) {
		return 0;
	}

	@Override
	public boolean isInside(Vector3f pt, float tolerance) {
		return false;
	}

	@Override
	public String getName() {
		return "BU_Simplex1to4";
	}

}