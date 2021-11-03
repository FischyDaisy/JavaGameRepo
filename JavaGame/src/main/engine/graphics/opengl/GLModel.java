package main.engine.graphics.opengl;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import main.engine.graphics.Material;
import main.engine.graphics.ModelData;

public class GLModel {
	
	private final String modelId;
	private final List<GLModel.GLMesh> glMeshList;

    private Material material;
    
    private float boundingRadius;
	
	public GLModel(String modelId) {
		this.modelId = modelId;
		glMeshList = new ArrayList<GLModel.GLMesh>();
	}
	
	public String getModelId() {
        return modelId;
    }
	
	public List<GLModel.GLMesh> getGLMeshList() {
        return glMeshList;
    }
	
	public Material getMaterial() {
		return material;
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public float getBoundingRadius() {
		return boundingRadius;
	}
	
	public void setBoundingRadius(float radius) {
		this.boundingRadius = radius;
	}
	
	public static List<GLModel> transformModels(List<ModelData> modelDataList) {
		List<GLModel> glModelList = new ArrayList<GLModel>();
		
		for (ModelData modelData : modelDataList) {
			GLModel glModel = transformModel(modelData);
			glModelList.add(glModel);
		}
		
		return glModelList;
	}
	
	public static GLModel transformModel(ModelData modelData) {
		GLModel model = new GLModel(modelData.getModelId());
		for (ModelData.MeshData meshData : modelData.getMeshDataList()) {
			FloatBuffer posBuffer = null;
	        FloatBuffer textCoordsBuffer = null;
	        FloatBuffer vecNormalsBuffer = null;
	        FloatBuffer weightsBuffer = null;
	        IntBuffer jointIndicesBuffer = null;
	        IntBuffer indicesBuffer = null;
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

	            glBindBuffer(GL_ARRAY_BUFFER, 0);
	            glBindVertexArray(0);
	            
	            GLModel.GLMesh mesh = new GLModel.GLMesh(vaoId, vboIdList, vertexCount);
	            model.getGLMeshList().add(mesh);
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
		return model;
	}
	
	public record GLMesh(int vaoId, List<Integer> vboIdList, int vertexCount) {
		public void cleanup() {
			glDisableVertexAttribArray(0);

	        // Delete the VBOs
	        glBindBuffer(GL_ARRAY_BUFFER, 0);
	        for (int vboId : vboIdList) {
	            glDeleteBuffers(vboId);
	        }
	        
	        // Delete the VAO
	        glBindVertexArray(0);
	        glDeleteVertexArrays(vaoId);
		}
	}
}
