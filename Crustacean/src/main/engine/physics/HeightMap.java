package main.engine.physics;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import crab.newton.NewtonBody;
import crab.newton.NewtonCollision;
import crab.newton.NewtonMesh;
import crab.newton.NewtonWorld;
import main.engine.graphics.ModelData;
import main.engine.graphics.ModelData.Material;
import main.engine.graphics.ModelData.MeshData;
import main.engine.utility.ResourcePaths;
import main.engine.utility.Utils;
import java.lang.foreign.*;
import static org.lwjgl.stb.STBImage.*;

public class HeightMap {
	
	private static final int MAX_COLOR = 255 * 255 * 255;
	
	public static HeightMapData createHeightMap(NewtonWorld world, String modelId, int cellSize, float minY, float maxY, SegmentAllocator allocator) throws Exception {
		ByteBuffer buf = null;
        int width;
        int height;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            buf = stbi_load(ResourcePaths.Textures.HEIGHT_MAP, w, h, channels, 4);
            if (buf == null) {
                throw new Exception("Image file [" + ResourcePaths.Textures.HEIGHT_MAP  + "] not loaded: " + stbi_failure_reason());
            }

            width = w.get();
            height = h.get();
        }
        
        float[] elevationMap = new float[width * height];
        char[] attributeMap = new char[width * height];
        
        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
            	float currentHeight = getHeight(col, row, width, minY, maxY, buf);
            	elevationMap[(row * width) + col] = currentHeight;
            	positions.add((float) col * cellSize); // x
            	positions.add(currentHeight); //y
                positions.add((float) row * cellSize); //z
                
                textCoords.add((float) cellSize * (float) col / (float) width);
                textCoords.add((float) cellSize * (float) row / (float) height);
                
                if (col < width - 1 && row < height - 1) {
                    int leftTop = row * width + col;
                    int leftBottom = (row + 1) * width + col;
                    int rightBottom = (row + 1) * width + col + 1;
                    int rightTop = row * width + col + 1;

                    indices.add(rightTop);
                    indices.add(leftBottom);
                    indices.add(rightBottom);

                    indices.add(leftTop);
                    indices.add(leftBottom);
                    indices.add(rightTop);
                }
            }
        }
        
        float[] posArr = Utils.listFloatToArray(positions);
        int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();
        float[] textCoordsArr = Utils.listFloatToArray(textCoords);
        float[] normalsArr = calcNormals(posArr, width, height);
        float[] tangentArr = new float[posArr.length];
        float[] biTangnetArr = new float[posArr.length];

        NewtonCollision collision = world.createHeightField(width, height, 1, elevationMap, attributeMap, 1.0f, cellSize, cellSize, 0);
        
        for (int i = 0; i < indicesArr.length; i += 3) {
			int p0 = indicesArr[i], p1 = indicesArr[i + 1], p2 = indicesArr[i + 2];
			
			float vx, vy, vz, wx, wy, wz;
			vx = posArr[p1] - posArr[p0];
			vy = posArr[p1 + 1] - posArr[p0 + 1];
			vz = posArr[p1 + 2] - posArr[p0 + 2];
			
			wx = posArr[p2] - posArr[p0];
			wy = posArr[p2 + 1] - posArr[p0 + 1];
			wz = posArr[p2 + 2] - posArr[p0 + 2];
			
			float sx = textCoordsArr[p1] - textCoordsArr[p0], sy = textCoordsArr[p1 + 1] - textCoordsArr[p0 + 1];
			float tx = textCoordsArr[p2] - textCoordsArr[p0], ty = textCoordsArr[p2 + 1] - textCoordsArr[p0 + 1];
			float dirCorrection = (tx * sy - ty * sx) < 0.0f ? -1.0f : 1.0f;
			if (sx * ty == sy * tx) {
	            sx = 0.0f;
	            sy = 1.0f;
	            tx = 1.0f;
	            ty = 0.0f;
	        }
			
			float tangentX, tangentY, tangentZ, biTangentX, biTangentY, biTangentZ;
			tangentX = (wx * sy - vx * ty) * dirCorrection;
			tangentY = (wy * sy - vy * ty) * dirCorrection;
			tangentZ = (wz * sy - vz * ty) * dirCorrection;
			biTangentX = (- wx * sx + vx * tx) * dirCorrection;
			biTangentY = (- wy * sx + vy * tx) * dirCorrection;
			biTangentZ = (- wz * sx + vz * tx) * dirCorrection;
			
			Vector3f localTan = new Vector3f(), localBitan = new Vector3f(), 
					tangent = new Vector3f(tangentX, tangentY, tangentZ), biTangent = new Vector3f(biTangentX, biTangentY, biTangentZ), 
					normal = new Vector3f();
			for (int b = 0; b < 3; b++) {
				int p = indicesArr[i + b];
				normal.x = normalsArr[p];
				normal.y = normalsArr[p + 1];
				normal.z = normalsArr[p + 2];
				
				tangent.sub(normal.mul(tangent.mul(normal, localTan), localTan), localTan);
				biTangent.sub(normal.mul(biTangent.mul(normal, localBitan), localBitan), localBitan)
				.sub(localTan.mul(biTangent.mul(localTan, localBitan), localBitan));
				localTan.normalize();
				localBitan.normalize();
				
				boolean invalid_tangent = !localTan.isFinite();
				boolean invalid_bitangent = !localBitan.isFinite();
				if (invalid_tangent != invalid_bitangent) {
					if (invalid_tangent) {
						localTan.x = Float.intBitsToFloat(Float.floatToRawIntBits(normal.x) ^ Float.floatToRawIntBits(localBitan.x));
						localTan.y = Float.intBitsToFloat(Float.floatToRawIntBits(normal.y) ^ Float.floatToRawIntBits(localBitan.y));
						localTan.z = Float.intBitsToFloat(Float.floatToRawIntBits(normal.z) ^ Float.floatToRawIntBits(localBitan.z));
						localTan.normalize();
					} else {
						localBitan.x = Float.intBitsToFloat(Float.floatToRawIntBits(normal.x) ^ Float.floatToRawIntBits(localTan.x));
						localBitan.y = Float.intBitsToFloat(Float.floatToRawIntBits(normal.y) ^ Float.floatToRawIntBits(localTan.y));
						localBitan.z = Float.intBitsToFloat(Float.floatToRawIntBits(normal.z) ^ Float.floatToRawIntBits(localTan.z));
						localTan.normalize();
					}
				}
				
				tangentArr[p] = localTan.x;
				tangentArr[p + 1] = localTan.y;
				tangentArr[p + 2] = localTan.z;
				biTangnetArr[p] = localBitan.x;
				biTangnetArr[p + 1] = localBitan.y;
				biTangnetArr[p + 2] = localBitan.z;
			}
		}
        
        ModelData.Material grassMaterial = new ModelData.Material(ResourcePaths.Textures.GRASS);
        List<ModelData.Material> materialList = new ArrayList<>();
		materialList.add(grassMaterial);
		ModelData.MeshData heightMapMesh = new ModelData.MeshData(posArr, normalsArr, tangentArr, biTangnetArr, textCoordsArr, indicesArr, 0);
		List<ModelData.MeshData> meshDataList = new ArrayList<>();
		meshDataList.add(heightMapMesh);
		ModelData heightMapModel = new ModelData(modelId, meshDataList, materialList);
        
        return new HeightMapData(collision, heightMapModel);
	}
	
	private static float[] calcNormals(float[] posArr, int width, int height) {
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        Vector3f v4 = new Vector3f();
        Vector3f v12 = new Vector3f();
        Vector3f v23 = new Vector3f();
        Vector3f v34 = new Vector3f();
        Vector3f v41 = new Vector3f();
        List<Float> normals = new ArrayList<>();
        Vector3f normal = new Vector3f();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (row > 0 && row < height -1 && col > 0 && col < width -1) {
                    int i0 = row*width*3 + col*3;
                    v0.x = posArr[i0];
                    v0.y = posArr[i0 + 1];
                    v0.z = posArr[i0 + 2];

                    int i1 = row*width*3 + (col-1)*3;
                    v1.x = posArr[i1];
                    v1.y = posArr[i1 + 1];
                    v1.z = posArr[i1 + 2];
                    v1 = v1.sub(v0);

                    int i2 = (row+1)*width*3 + col*3;
                    v2.x = posArr[i2];
                    v2.y = posArr[i2 + 1];
                    v2.z = posArr[i2 + 2];
                    v2 = v2.sub(v0);

                    int i3 = (row)*width*3 + (col+1)*3;
                    v3.x = posArr[i3];
                    v3.y = posArr[i3 + 1];
                    v3.z = posArr[i3 + 2];
                    v3 = v3.sub(v0);

                    int i4 = (row-1)*width*3 + col*3;
                    v4.x = posArr[i4];
                    v4.y = posArr[i4 + 1];
                    v4.z = posArr[i4 + 2];
                    v4 = v4.sub(v0);

                    v1.cross(v2, v12);
                    v12.normalize();

                    v2.cross(v3, v23);
                    v23.normalize();

                    v3.cross(v4, v34);
                    v34.normalize();

                    v4.cross(v1, v41);
                    v41.normalize();

                    normal = v12.add(v23).add(v34).add(v41);
                    normal.normalize();
                } else {
                    normal.x = 0;
                    normal.y = 1;
                    normal.z = 0;
                }
                normal.normalize();
                normals.add(normal.x);
                normals.add(normal.y);
                normals.add(normal.z);
            }
        }
        return Utils.listFloatToArray(normals);
    }
	
	private static float getHeight(int x, int z, int width, float minY, float maxY, ByteBuffer buffer) {
        byte r = buffer.get(x * 4 + 0 + z * 4 * width);
        byte g = buffer.get(x * 4 + 1 + z * 4 * width);
        byte b = buffer.get(x * 4 + 2 + z * 4 * width);
        byte a = buffer.get(x * 4 + 3 + z * 4 * width);
        int argb = ((0xFF & a) << 24) | ((0xFF & r) << 16)
            | ((0xFF & g) << 8) | (0xFF & b);
        return minY + Math.abs(maxY - minY) * ((float) argb / (float) MAX_COLOR);
    }
	
	public record HeightMapData(NewtonCollision collision, ModelData modeldata) {}
}