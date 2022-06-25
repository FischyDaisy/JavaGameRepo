package com.newton;

import com.newton.generated.*;

import jdk.incubator.foreign.*;

public class NewtonMesh {
	
	protected final MemoryAddress address;
	
	protected NewtonMesh(MemoryAddress address) {
		this.address = address;
	}
	
	/**
	 * Creates a new {@code NewtonMesh}
	 * @param world - {@code NewtonWorld} to allocate NewtonMesh
	 * @return new {@code NewtonMesh}
	 */
	public static NewtonMesh create(NewtonWorld world) {
		return new NewtonMesh(Newton_h.NewtonMeshCreate(world.address));
	}
	
	/**
	 * Create a new {@code NewtonMesh} from an existing mesh
	 * @param mesh - {@code NewtonMesh} to be copied
	 * @return new {@code NewtonMesh}
	 */
	public static NewtonMesh createFromMesh(NewtonMesh mesh) {
		return new NewtonMesh(Newton_h.NewtonMeshCreateFromMesh(mesh.address));
	}
	
	/**
	 * Creates a new {@code NewtonMesh} from a {@code NewtonCollision}
	 * @param collision - 
	 * @return
	 */
	public static NewtonMesh createFromCollision(NewtonCollision collision) {
		return new NewtonMesh(Newton_h.NewtonMeshCreateFromCollision(collision.address));
	}
	
	public static NewtonMesh createTetrahedraIsoSurface(NewtonMesh mesh) {
		return new NewtonMesh(Newton_h.NewtonMeshCreateTetrahedraIsoSurface(mesh.address));
	}
	
	public static NewtonMesh createConvexHull(NewtonWorld world, int pointCount, float[] vertexCloud, int strideInBytes, float tolerance, 
			SegmentAllocator allocator) {
		MemorySegment vertCloud = allocator.allocateArray(Newton_h.C_FLOAT, vertexCloud);
		return new NewtonMesh(Newton_h.NewtonMeshCreateConvexHull(world.address, pointCount, vertCloud, strideInBytes, tolerance));
	}
	
	public static NewtonMesh createVoronoiConvexDecomposition(NewtonWorld world, int pointCount, float[] vertexCloud, int strideInBytes, int materialID, float[] textureMatrix,
			SegmentAllocator allocator) {
		MemorySegment vertCloud = allocator.allocateArray(Newton_h.C_FLOAT, vertexCloud);
		MemorySegment textMatrix = allocator.allocateArray(Newton_h.C_FLOAT, textureMatrix);
		return new NewtonMesh(Newton_h.NewtonMeshCreateVoronoiConvexDecomposition(world.address, pointCount, vertCloud, strideInBytes, materialID, textMatrix));
	}
	
	public static NewtonMesh createFromSerialization(NewtonWorld world, NewtonDeserializeCallback deserializFunc, Addressable serializeHandle, ResourceScope scope) { 
		NativeSymbol func = NewtonDeserializeCallback.allocate(deserializFunc, scope);
		return new NewtonMesh(Newton_h.NewtonMeshCreateFromSerialization(world.address, func, serializeHandle));
	}
	
	public static NewtonMesh loadTetrahedraMesh(NewtonWorld world, String filename, SegmentAllocator allocator) {
		MemorySegment cString = allocator.allocateUtf8String(filename);
		return new NewtonMesh(Newton_h.NewtonMeshLoadTetrahedraMesh(world.address, cString));
	}
	
	public static void clearVertexFormat(MemorySegment meshVertexFormat) {
		Newton_h.NewtonMeshClearVertexFormat(meshVertexFormat);
	}
	
	public void destroy() {
		Newton_h.NewtonMeshDestroy(address);
	}
	
	public void serialize(NewtonSerializeCallback serializeFunction, Addressable serializeHandle, ResourceScope scope) {
		NativeSymbol serializeFunc = NewtonSerializeCallback.allocate(serializeFunction, scope);
		Newton_h.NewtonMeshSerialize(address, serializeFunc, serializeHandle);
	}
	
	public void flipWinding() {
		Newton_h.NewtonMeshFlipWinding(address);
	}
	
	public void applyTransform(float[] transform, SegmentAllocator allocator) {
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, transform);
		Newton_h.NewtonMeshApplyTransform(address, matrix);
	}
	
	public float[] calculateOOBB(SegmentAllocator allocator) {
		MemorySegment oobb = allocator.allocateArray(Newton_h.C_FLOAT, Newton.MAT4F_VEC3F);
		Newton_h.NewtonMeshCalculateOOBB(address, 
				oobb.asSlice(0L, Newton_h.C_FLOAT.byteSize() * 16), 
				oobb.asSlice(64L, Newton_h.C_FLOAT.byteSize()), 
				oobb.asSlice(70L, Newton_h.C_FLOAT.byteSize()), 
				oobb.asSlice(74L, Newton_h.C_FLOAT.byteSize()));
		return oobb.toArray(Newton_h.C_FLOAT);
	}
	
	public void calculateVertexNormals(float angleInRadians) {
		Newton_h.NewtonMeshCalculateVertexNormals(address, angleInRadians);
	}
	
	public void applySphericalMapping(int material, float[] alignMatrix, SegmentAllocator allocator) {
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, alignMatrix);
		Newton_h.NewtonMeshApplySphericalMapping(address, material, matrix);
	}
	
	public void applyCylindricalMapping(int cylinderMaterial, int capMaterial, float[] alignMatrix, SegmentAllocator allocator) {
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, alignMatrix);
		Newton_h.NewtonMeshApplyCylindricalMapping(address, cylinderMaterial, capMaterial, matrix);
	}
	
	public void applyBoxMapping(int frontMaterial, int sideMaterial, int topMaterial, float[] alignMatrix, SegmentAllocator allocator) {
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, alignMatrix);
		Newton_h.NewtonMeshApplyBoxMapping(address, frontMaterial, sideMaterial, topMaterial, matrix);
	}
	
	public void applyAngleBasedMapping(int material, NewtonReportProgress reportPrograssCallback, Addressable reportPrgressUserData, float[] alignMatrix, ResourceScope scope) {
		NativeSymbol callback = NewtonReportProgress.allocate(reportPrograssCallback, scope);
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment matrix = allocator.allocateArray(Newton_h.C_FLOAT, alignMatrix);
		Newton_h.NewtonMeshApplyAngleBasedMapping(address, material, callback, reportPrgressUserData, matrix);
	}
	
	public void createTetrahedraLinearBlendSkinWeightsChannel(NewtonMesh skinMesh) {
		Newton_h.NewtonCreateTetrahedraLinearBlendSkinWeightsChannel(address, skinMesh.address);
	}
	
	public void optimize() {
		Newton_h.NewtonMeshOptimize(address);
	}
	
	public void optimizePoints() {
		Newton_h.NewtonMeshOptimizePoints(address);
	}
	
	public void optimizeVertex() {
		Newton_h.NewtonMeshOptimizeVertex(address);
	}
	
	public boolean isOpenMesh() {
		return Newton_h.NewtonMeshIsOpenMesh(address) == 1;
	}
	
	public void fixTJoints() {
		Newton_h.NewtonMeshFixTJoints(address);
	}
	
	public void polygonize() {
		Newton_h.NewtonMeshPolygonize(address);
	}
	
	public void triangulate() {
		Newton_h.NewtonMeshTriangulate(address);
	}
	
	public NewtonMesh convexMeshIntersection(NewtonMesh convexMesh) {
		return new NewtonMesh(Newton_h.NewtonMeshConvexMeshIntersection(address, convexMesh.address));
	}
	
	public void beginBuild() {
		Newton_h.NewtonMeshBeginBuild(address);
	}
	
	public void beginFace() {
		Newton_h.NewtonMeshBeginFace(address);
	}
	
	public void addPoint(double x, double y, double z) {
		Newton_h.NewtonMeshAddPoint(address, x, y, z);
	}
	
	public void addLayer(int layerIndex) {
		Newton_h.NewtonMeshAddLayer(address, layerIndex);
	}
	
	public void addMaterial(int materialIndex) {
		Newton_h.NewtonMeshAddMaterial(address, materialIndex);
	}
	
	public void addNormal(float x, float y, float z) {
		Newton_h.NewtonMeshAddNormal(address, x, y, z);
	}
	
	public void addBiNormal(float x, float y, float z) {
		Newton_h.NewtonMeshAddBinormal(address, x, y, z);
	}
	
	public void addUV0(float u, float v) {
		Newton_h.NewtonMeshAddUV0(address, u, v);
	}
	
	public void addUV1(float u, float v) {
		Newton_h.NewtonMeshAddUV1(address, u, v);
	}
	
	public void addVertexColor(float r, float g, float b, float a) {
		Newton_h.NewtonMeshAddVertexColor(address, r, g, b, a);
	}
	
	public void endFace() {
		Newton_h.NewtonMeshEndFace(address);
	}
	
	public void endBuild() {
		Newton_h.NewtonMeshEndBuild(address);
	}
	
	public void buildFromVertexListIndexList(MemorySegment meshVertexFormat) {
		Newton_h.NewtonMeshBuildFromVertexListIndexList(address, meshVertexFormat);
	}
	
	public int getPointCount() {
		return Newton_h.NewtonMeshGetPointCount(address);
	}
	
	public int[] getIndexToVertexMap(int count, ResourceScope scope) {
		MemoryAddress indexPtr = Newton_h.NewtonMeshGetIndexToVertexMap(address);
		return MemorySegment.ofAddress(indexPtr, Newton_h.C_INT.byteSize() * count, scope).toArray(Newton_h.C_INT);
	}
	
	public double[] getVertexDoubleChannel(int vertexStrideInBytes, int vertexCount, SegmentAllocator allocator) {
		MemorySegment vertexBuffer = allocator.allocateArray(Newton_h.C_DOUBLE, new double[vertexCount * 3]);
		Newton_h.NewtonMeshGetVertexDoubleChannel(address, vertexStrideInBytes, vertexBuffer);
		return vertexBuffer.toArray(Newton_h.C_DOUBLE);
	}
	
	public float[] getVertexChannel(int vertexStrideInBytes, int vertexCount, SegmentAllocator allocator) {
		MemorySegment vertexBuffer = allocator.allocateArray(Newton_h.C_FLOAT, new float[vertexCount * 3]);
		Newton_h.NewtonMeshGetVertexChannel(address, vertexStrideInBytes, vertexBuffer);
		return vertexBuffer.toArray(Newton_h.C_FLOAT);
	}
	
	public float[] getNormalChannel(int vertexStrideInBytes, int vertexCount, SegmentAllocator allocator) {
		MemorySegment normalBuffer = allocator.allocateArray(Newton_h.C_FLOAT, new float[vertexCount * 3]);
		Newton_h.NewtonMeshGetNormalChannel(address, vertexStrideInBytes, normalBuffer);
		return normalBuffer.toArray(Newton_h.C_FLOAT);
	}
	
	public float[] getBiNormalChannel(int vertexStrideInBytes, int vertexCount, SegmentAllocator allocator) {
		MemorySegment biNormalBuffer = allocator.allocateArray(Newton_h.C_FLOAT, new float[vertexCount * 3]);
		Newton_h.NewtonMeshGetBinormalChannel(address, vertexStrideInBytes, biNormalBuffer);
		return biNormalBuffer.toArray(Newton_h.C_FLOAT);
	}
	
	public float[] getUV0Channel(int vertexStrideInBytes, int vertexCount, SegmentAllocator allocator) {
		MemorySegment uvBuffer = allocator.allocateArray(Newton_h.C_FLOAT, new float[vertexCount * 2]);
		Newton_h.NewtonMeshGetUV0Channel(address, vertexStrideInBytes, uvBuffer);
		return uvBuffer.toArray(Newton_h.C_FLOAT);
	}
	
	public float[] getUV1Channel(int vertexStrideInBytes, int vertexCount, SegmentAllocator allocator) {
		MemorySegment uvBuffer = allocator.allocateArray(Newton_h.C_FLOAT, new float[vertexCount * 2]);
		Newton_h.NewtonMeshGetUV1Channel(address, vertexStrideInBytes, uvBuffer);
		return uvBuffer.toArray(Newton_h.C_FLOAT);
	}
	
	public float[] getVertexColorChannel(int vertexStrideInBytes, int vertexCount, SegmentAllocator allocator) {
		MemorySegment colorBuffer = allocator.allocateArray(Newton_h.C_FLOAT, new float[vertexCount * 4]);
		Newton_h.NewtonMeshGetVertexColorChannel(address, vertexStrideInBytes, colorBuffer);
		return colorBuffer.toArray(Newton_h.C_FLOAT);
	}
	
	public boolean hasNormalChannel() {
		return Newton_h.NewtonMeshHasNormalChannel(address) == 1;
	}
	
	public boolean hasBiNormalChannel() {
		return Newton_h.NewtonMeshHasBinormalChannel(address) == 1;
	}
	
	public boolean hasUV0Channel() {
		return Newton_h.NewtonMeshHasUV0Channel(address) == 1;
	}
	
	public boolean hasUV1Channel() {
		return Newton_h.NewtonMeshHasUV1Channel(address) == 1;
	}
	
	public boolean hasVertexColorChannel() {
		return Newton_h.NewtonMeshHasVertexColorChannel(address) == 1;
	}
	
	public MeshHandle beginHandle() {
		return new MeshHandle(Newton_h.NewtonMeshBeginHandle(address));
	}
	
	public void endHandle(MeshHandle handle) {
		Newton_h.NewtonMeshEndHandle(address, handle.address);
	}
	
	public int firstMaterial(MeshHandle handle) {
		return Newton_h.NewtonMeshFirstMaterial(address, handle.address);
	}
	
	public int nextMaterial(MeshHandle handle, int materialID) {
		return Newton_h.NewtonMeshNextMaterial(address, handle.address, materialID);
	}
	
	public int materialGetMaterial(MeshHandle handle, int materialID) {
		return Newton_h.NewtonMeshMaterialGetMaterial(address, handle.address, materialID);
	}
	
	public int materialGetIndexCount(MeshHandle handle, int materialID) {
		return Newton_h.NewtonMeshMaterialGetIndexCount(address, handle.address, materialID);
	}
	
	public int[] materialGetIndexStream(MeshHandle handle, int materialID, int indexCount, SegmentAllocator allocator) {
		MemorySegment indexStream = allocator.allocateArray(Newton_h.C_INT, new int[indexCount]);
		Newton_h.NewtonMeshMaterialGetIndexStream(address, handle.address, materialID, indexStream);
		return indexStream.toArray(Newton_h.C_INT);
	}
	
	public short[] materialGetIndexStreamShort(MeshHandle handle, int materialID, int indexCount, SegmentAllocator allocator) {
		MemorySegment indexStream = allocator.allocateArray(Newton_h.C_SHORT, new short[indexCount]);
		Newton_h.NewtonMeshMaterialGetIndexStreamShort(address, handle.address, materialID, indexStream);
		return indexStream.toArray(Newton_h.C_SHORT);
	}
	
	public static class MeshHandle {
		protected final MemoryAddress address;
		
		private MeshHandle(MemoryAddress address) {
			this.address = address;
		}
	}
}
