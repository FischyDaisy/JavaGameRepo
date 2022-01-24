package main.engine.graphics.opengl;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import main.engine.graphics.GraphConstants;
import main.engine.graphics.ModelData;
import main.engine.graphics.Transformation;
import main.engine.graphics.opengl.GLModel.GLMaterial;
import main.engine.items.GameItem;

public class InstancedGLModel extends GLModel {
	
	public static final int INSTANCE_SIZE_BYTES = GraphConstants.MAT4X4_SIZE_BYTES + GraphConstants.FLOAT_SIZE_BYTES * 2 + GraphConstants.FLOAT_SIZE_BYTES;
	public static final int INSTANCE_SIZE_FLOATS = GraphConstants.MAT4X4_SIZE_FLOATS + 3;

	public InstancedGLModel(String modelId) {
		super(modelId);
	}
	
	public void renderListInstanced(List<GameItem> gameItems, Transformation transformation, Matrix4f viewMatrix, GLMaterial material) {
		for (GLMesh mesh : material.glMeshList()) {
			((InstancedGLMesh) mesh).renderListInstanced(gameItems, transformation, viewMatrix, material);
		}
	}
	
	public void renderListInstanced(List<GameItem> gameItems, boolean billBoard, Transformation transformation, Matrix4f viewMatrix, GLMaterial material) {
		for (GLMesh mesh : material.glMeshList()) {
			((InstancedGLMesh) mesh).renderListInstanced(gameItems, billBoard, transformation, viewMatrix, material);
		}
	}
	
	public static List<InstancedGLModel> transformModels(List<ModelData> modelDataList, GLTextureCache textureCache, int numInstances) throws Exception {
		List<InstancedGLModel> glModelList = new ArrayList<InstancedGLModel>();
		
		for (ModelData modelData : modelDataList) {
			InstancedGLModel glModel = new InstancedGLModel(modelData.getModelId());
			glModelList.add(glModel);
			
			GLMaterial defaultGLMaterial = null;
			for (ModelData.Material material : modelData.getMaterialList()) {
				GLMaterial glMaterial = transformMaterial(material, textureCache);
				glModel.glMaterialList.add(glMaterial);
			}
			
			transformInstancedModel(modelData, glModel, defaultGLMaterial, textureCache, numInstances);
		}
		
		return glModelList;
	}
	
	private static void transformInstancedModel(ModelData modelData, InstancedGLModel glModel, 
			GLMaterial defaultGLMaterial, GLTextureCache textureCache, int numInstances) throws Exception{
		for (ModelData.MeshData meshData : modelData.getMeshDataList()) {
			FloatBuffer posBuffer = null;
	        FloatBuffer textCoordsBuffer = null;
	        FloatBuffer vecNormalsBuffer = null;
	        FloatBuffer weightsBuffer = null;
	        IntBuffer jointIndicesBuffer = null;
	        IntBuffer indicesBuffer = null;
	        FloatBuffer instanceDataBuffer = null;
	        try {
	        	int vertexCount = meshData.indices().length;
	            List<Integer> vboIdList = new ArrayList<>();

	            int vaoId = glGenVertexArrays();
	            glBindVertexArray(vaoId);

	            // Position VBO
	            int vboId = glGenBuffers();
	            vboIdList.add(vboId);
	            posBuffer = MemoryUtil.memAllocFloat(meshData.positions().length);
	            posBuffer.put(meshData.positions()).flip();
	            glBindBuffer(GL_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
	            glEnableVertexAttribArray(0);
	            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

	            // Texture coordinates VBO
	            vboId = glGenBuffers();
	            vboIdList.add(vboId);
	            textCoordsBuffer = MemoryUtil.memAllocFloat(meshData.textCoords().length);
	            textCoordsBuffer.put(meshData.textCoords()).flip();
	            glBindBuffer(GL_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
	            glEnableVertexAttribArray(1);
	            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
	            
	            // Vertex normals VBO
	            vboId = glGenBuffers();
	            vboIdList.add(vboId);
	            vecNormalsBuffer = MemoryUtil.memAllocFloat(meshData.normals().length);
	            if (vecNormalsBuffer.capacity() > 0) {
	                vecNormalsBuffer.put(meshData.normals()).flip();
	            } else {
	                // Create empty structure
	                vecNormalsBuffer = MemoryUtil.memAllocFloat(meshData.positions().length);
	            }
	            glBindBuffer(GL_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
	            glEnableVertexAttribArray(2);
	            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
	            
	            // Weights
	            vboId = glGenBuffers();
	            vboIdList.add(vboId);
	            weightsBuffer = MemoryUtil.memAllocFloat(meshData.weights().length);
	            weightsBuffer.put(meshData.weights()).flip();
	            glBindBuffer(GL_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
	            glEnableVertexAttribArray(3);
	            glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

	            // Joint indices
	            vboId = glGenBuffers();
	            vboIdList.add(vboId);
	            jointIndicesBuffer = MemoryUtil.memAllocInt(meshData.jointIndices().length);
	            jointIndicesBuffer.put(meshData.jointIndices()).flip();
	            glBindBuffer(GL_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ARRAY_BUFFER, jointIndicesBuffer, GL_STATIC_DRAW);
	            glEnableVertexAttribArray(4);
	            glVertexAttribPointer(4, 4, GL_FLOAT, false, 0, 0);

	            // Index VBO
	            vboId = glGenBuffers();
	            vboIdList.add(vboId);
	            indicesBuffer = MemoryUtil.memAllocInt(meshData.indices().length);
	            indicesBuffer.put(meshData.indices()).flip();
	            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

	            // Instance Data
	            
	            // Model View Matrix
	            int instanceDataVBO = glGenBuffers();
	            vboIdList.add(instanceDataVBO);
	            instanceDataBuffer = MemoryUtil.memAllocFloat(numInstances * InstancedGLModel.INSTANCE_SIZE_FLOATS);
	            glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
	            int start = 5;
	            int strideStart = 0;
	            for (int i = 0; i < 4; i++) {
	                glVertexAttribPointer(start, 4, GL_FLOAT, false, InstancedGLModel.INSTANCE_SIZE_BYTES, strideStart);
	                glVertexAttribDivisor(start, 1);
	                glEnableVertexAttribArray(start);
	                start++;
	                strideStart += GraphConstants.VECTOR4F_SIZE_BYTES;
	            }

	            // Texture offsets
	            glVertexAttribPointer(start, 2, GL_FLOAT, false, InstancedGLModel.INSTANCE_SIZE_BYTES, strideStart);
	            glVertexAttribDivisor(start, 1);
	            glEnableVertexAttribArray(start);
	            strideStart += GraphConstants.FLOAT_SIZE_BYTES * 2;
	            start++;
	            
	            // Selected
	            glVertexAttribPointer(start, 1, GL_FLOAT, false, InstancedGLModel.INSTANCE_SIZE_BYTES, strideStart);
	            glVertexAttribDivisor(start, 1);
	            glEnableVertexAttribArray(start);
	            start++;

	            glBindBuffer(GL_ARRAY_BUFFER, 0);
	            glBindVertexArray(0);
	            
	            InstancedGLMesh mesh = new InstancedGLMesh(vaoId, vboIdList, vertexCount, numInstances, instanceDataVBO, instanceDataBuffer);
	            mesh.setBoundingBox(meshData.boundingBox());
	            
	            GLMaterial glMaterial;
	            int materialIdx = meshData.materialIdx();
	            if(materialIdx >= 0 && materialIdx < glModel.glMaterialList.size()) {
	            	glMaterial = glModel.glMaterialList.get(materialIdx);
	            } else {
	            	if (defaultGLMaterial == null) {
	            		defaultGLMaterial = transformMaterial(new ModelData.Material(), textureCache);
	            	}
	            	glMaterial = defaultGLMaterial;
	            }
	            glMaterial.glMeshList().add(mesh);
	        } finally {
	            if (posBuffer != null) {
	                MemoryUtil.memFree(posBuffer);
	            }
	            if (textCoordsBuffer != null) {
	                MemoryUtil.memFree(textCoordsBuffer);
	            }
	            if (vecNormalsBuffer != null) {
	                MemoryUtil.memFree(vecNormalsBuffer);
	            }
	            if (weightsBuffer != null) {
	                MemoryUtil.memFree(weightsBuffer);
	            }
	            if (jointIndicesBuffer != null) {
	                MemoryUtil.memFree(jointIndicesBuffer);
	            }
	            if (indicesBuffer != null) {
	                MemoryUtil.memFree(indicesBuffer);
	            }
	        }
		}
	}
	
	public static class InstancedGLMesh extends GLMesh {
		
		private final int numInstances;
	    private final int instanceDataVBO;
	    private FloatBuffer instanceDataBuffer;
	    
		public InstancedGLMesh(int vaoId, List<Integer> vboIdList, int vertexCount,
				int numInstances, int instanceDataVBO, FloatBuffer instanceDataBuffer) {
			super(vaoId, vboIdList, vertexCount);
			this.numInstances = numInstances;
			this.instanceDataVBO = instanceDataVBO;
			this.instanceDataBuffer = instanceDataBuffer;
		}
		
		public int numInstances() {
			return numInstances;
		}
		
		public int instanceDataVBO() {
			return instanceDataVBO;
		}
		
		public FloatBuffer instanceDataBuffer() {
			return instanceDataBuffer;
		}
		
		public void renderListInstanced(List<GameItem> gameItems, Transformation transformation, Matrix4f viewMatrix, GLMaterial material) {
	        renderListInstanced(gameItems, false, transformation, viewMatrix, material);
	    }

	    public void renderListInstanced(List<GameItem> gameItems, boolean billBoard, Transformation transformation, Matrix4f viewMatrix, GLMaterial material) {
	    	initRender(material);

	        int chunkSize = numInstances;
	        int length = gameItems.size();
	        for (int i = 0; i < length; i += chunkSize) {
	            int end = Math.min(length, i + chunkSize);
	            List<GameItem> subList = gameItems.subList(i, end);
	            renderChunkInstanced(subList, billBoard, transformation, viewMatrix, material);
	        }

	        endRender();
	    }

	    private void renderChunkInstanced(List<GameItem> gameItems, boolean billBoard, Transformation transformation, Matrix4f viewMatrix, GLMaterial material) {
	    	this.instanceDataBuffer.clear();

	        int i = 0;

	        GLTexture text = material.texture();
	        for (GameItem gameItem : gameItems) {
	            Matrix4f modelMatrix = gameItem.buildModelMatrix();
	            if (viewMatrix != null && billBoard) {
	                viewMatrix.transpose3x3(modelMatrix);
	            }
	            modelMatrix.get(InstancedGLModel.INSTANCE_SIZE_FLOATS * i, instanceDataBuffer);
	            if (text != null) {
	                int col = gameItem.getTextPos() % text.getNumCols();
	                int row = gameItem.getTextPos() / text.getNumCols();
	                float textXOffset = (float) col / text.getNumCols();
	                float textYOffset = (float) row / text.getNumRows();
	                int buffPos = InstancedGLModel.INSTANCE_SIZE_FLOATS * i + GraphConstants.MAT4X4_SIZE_FLOATS;
	                this.instanceDataBuffer.put(buffPos, textXOffset);
	                this.instanceDataBuffer.put(buffPos + 1, textYOffset);
	            }
	            
	            // Selected data
	            int buffPos = InstancedGLModel.INSTANCE_SIZE_FLOATS * i + GraphConstants.MAT4X4_SIZE_FLOATS + 2;
	            this.instanceDataBuffer.put(buffPos, gameItem.isSelected() ? 1 : 0);

	            i++;
	        }

	        glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
	        glBufferData(GL_ARRAY_BUFFER, instanceDataBuffer, GL_DYNAMIC_DRAW);

	        glDrawElementsInstanced(
	                GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0, gameItems.size());

	        glBindBuffer(GL_ARRAY_BUFFER, 0);
	    }
		
		@Override
		public void cleanup() {
			super.cleanup();
			if (this.instanceDataBuffer != null) {
	            MemoryUtil.memFree(this.instanceDataBuffer);
	            this.instanceDataBuffer = null;
	        }
		}
	}
}