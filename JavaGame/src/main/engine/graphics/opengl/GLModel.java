package main.engine.graphics.opengl;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
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
import java.util.function.Consumer;

import org.joml.Vector4f;
import org.joml.primitives.AABBf;
import org.lwjgl.system.MemoryUtil;

import main.engine.graphics.IModel;
import main.engine.graphics.Material;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.VKTexture;
import main.engine.items.GameItem;

public class GLModel implements IModel {
	
	protected final String modelId;
	protected final List<GLMaterial> glMaterialList;
    
    private float boundingRadius;
	
	public GLModel(String modelId) {
		this.modelId = modelId;
		glMaterialList = new ArrayList<GLMaterial>();
	}
	
	public String getModelId() {
        return modelId;
    }
	
	public List<GLModel.GLMaterial> getGLMaterialList() {
        return glMaterialList;
    }
	
	public void setGLMaterial(GLMaterial material) {
		for (int i = 0; i < glMaterialList.size(); i++) {
			glMaterialList.set(i, material);
		}
	}
	
	public void setGLMaterial(List<GLMaterial> materials) throws Exception {
		if (materials.size() != glMaterialList.size()) {
			throw new RuntimeException("List sizes don't match");
		}
		for (int i = 0; i < glMaterialList.size(); i++) {
			glMaterialList.set(i, materials.get(i));
		}
	}
	
	public float getBoundingRadius() {
		return boundingRadius;
	}
	
	public void setBoundingRadius(float radius) {
		this.boundingRadius = radius;
	}
	
	public void render() {
		for (GLMaterial glMaterial : glMaterialList) {
			render(glMaterial);
		}
	}
	
	public void render(GLMaterial material) {
		for (GLMesh glMesh : material.glMeshList()) {
			glMesh.render(material);
		}
	}
	
	public void renderList(List<GameItem> gameItems, Consumer<GameItem> consumer) {
		for (GLMaterial glMaterial : glMaterialList) {
			renderList(gameItems, glMaterial, consumer);
		}
	}
	
	public void renderList(List<GameItem> gameItems, GLMaterial material, Consumer<GameItem> consumer) {
		for (GLMesh glMesh : material.glMeshList()) {
			glMesh.renderList(gameItems, consumer, material);
		}
	}
	
	@Override
	public void cleanup() {
		glMaterialList.forEach(GLMaterial::cleanup);
	}
	
	public static List<GLModel> transformModels(List<ModelData> modelDataList, GLTextureCache textureCache) throws Exception {
		List<GLModel> glModelList = new ArrayList<GLModel>();
		
		for (ModelData modelData : modelDataList) {
			GLModel glModel = new GLModel(modelData.getModelId());
			glModelList.add(glModel);
			
			GLMaterial defaultGLMaterial = null;
			for (ModelData.Material material : modelData.getMaterialList()) {
				GLMaterial glMaterial = transformMaterial(material, textureCache);
				glModel.glMaterialList.add(glMaterial);
			}
			
			transformModel(modelData, glModel, defaultGLMaterial, textureCache);
		}
		
		return glModelList;
	}
	
	protected static void transformModel(ModelData modelData, GLModel glModel, 
			GLMaterial defaultGLMaterial, GLTextureCache textureCache) throws Exception {
		for (ModelData.MeshData meshData : modelData.getMeshDataList()) {
			FloatBuffer posBuffer = null;
	        FloatBuffer textCoordsBuffer = null;
	        FloatBuffer normalsBuffer = null;
	        FloatBuffer tangentsBuffer = null;
	        FloatBuffer biTangentsBuffer = null;
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

	            // Normals VBO
	            vboId = glGenBuffers();
	            vboIdList.add(vboId);
	            normalsBuffer = MemoryUtil.memAllocFloat(meshData.normals().length);
	            normalsBuffer.put(meshData.normals()).flip();
	            glBindBuffer(GL_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
	            glEnableVertexAttribArray(1);
	            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
	            
	            // Tangents VBO
	            vboId = glGenBuffers();
	            vboIdList.add(vboId);
	            tangentsBuffer = MemoryUtil.memAllocFloat(meshData.tangents().length);
	            tangentsBuffer.put(meshData.tangents()).flip();
	            glBindBuffer(GL_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ARRAY_BUFFER, tangentsBuffer, GL_STATIC_DRAW);
	            glEnableVertexAttribArray(2);
	            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
	            
	            // BiTangents VBO
	            vboId = glGenBuffers();
	            vboIdList.add(vboId);
	            biTangentsBuffer = MemoryUtil.memAllocFloat(meshData.biTangents().length);
	            biTangentsBuffer.put(meshData.biTangents()).flip();
	            glBindBuffer(GL_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ARRAY_BUFFER, biTangentsBuffer, GL_STATIC_DRAW);
	            glEnableVertexAttribArray(3);
	            glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

	            // Texture Coords VBO
	            vboId = glGenBuffers();
	            vboIdList.add(vboId);
	            textCoordsBuffer = MemoryUtil.memAllocFloat(meshData.textCoords().length);
	            textCoordsBuffer.put(meshData.textCoords()).flip();
	            glBindBuffer(GL_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
	            glEnableVertexAttribArray(4);
	            glVertexAttribPointer(4, 2, GL_FLOAT, false, 0, 0);

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
	            if (normalsBuffer != null) {
	                MemoryUtil.memFree(normalsBuffer);
	            }
	            if (tangentsBuffer != null) {
	                MemoryUtil.memFree(tangentsBuffer);
	            }
	            if (biTangentsBuffer != null) {
	                MemoryUtil.memFree(biTangentsBuffer);
	            }
	            if (indicesBuffer != null) {
	                MemoryUtil.memFree(indicesBuffer);
	            }
	        }
		}
	}
	
	public static GLMaterial transformMaterial(ModelData.Material material, GLTextureCache textureCache) throws Exception {
		GLTexture texture = textureCache.get(material.texturePath(), material.cols(), material.rows());
		boolean hasTexture = material.texturePath() != null && material.texturePath().trim().length() > 0;
		GLTexture normalMapTexture = textureCache.get(material.normalMapPath(), material.cols(), material.rows());
		boolean hasNormalMapTexture = material.normalMapPath() != null && material.normalMapPath().trim().length() > 0;
		GLTexture metalRoughTexture = textureCache.get(material.metalRoughMap(), material.cols(), material.rows());
		boolean hasMetalRoughTexture = material.metalRoughMap() != null && material.metalRoughMap().trim().length() > 0;
		
		return new GLMaterial(material.diffuseColor(), texture, hasTexture, normalMapTexture,
				hasNormalMapTexture, metalRoughTexture, hasMetalRoughTexture, material.metallicFactor(), material.roughnessFactor(), new ArrayList<GLMesh>());
	}
	
	public record GLMaterial(Vector4f diffuseColor, GLTexture texture, boolean hasTexture, GLTexture normalMap,
            boolean hasNormalMap, GLTexture metalRoughMap, boolean hasMetalRoughMap,
            float metallicFactor, float roughnessFactor, List<GLMesh> glMeshList) {
		public static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		public boolean isTransparent() {
			return texture != null && texture.hasTransparencies();
        }
		
		public boolean isTextured() {
			return texture != null;
		}
		
		public boolean hasNormalMap() {
			return normalMap != null;
		}
		
		public void cleanup() {
			glMeshList.forEach(GLMesh::cleanup);
		}
	}
	
	public static class GLMesh {
		
		protected final int vaoId;
		protected final List<Integer> vboIdList;
		protected final int vertexCount;
		protected final AABBf boundingBox;
		
		public GLMesh(int vaoId, List<Integer> vboIdList, int vertexCount) {
			this.vaoId = vaoId;
			this.vboIdList = vboIdList;
			this.vertexCount = vertexCount;
			boundingBox = new AABBf();
		}
		
		public int vaoId() {
			return vaoId;
		}
		
		public List<Integer> vboIdList() {
			return vboIdList;
		}
		
		public int vertexCount() {
			return vertexCount;
		}
		
		public AABBf getBoundingBox() {
			return boundingBox;
		}
		
		public void setBoundingBox(AABBf aabb) {
			boundingBox.set(aabb);
		}
		
		protected void initRender(GLMaterial material) {
	        GLTexture texture = material.texture();
	        if (texture != null) {
	            // Activate first texture bank
	            glActiveTexture(GL_TEXTURE0);
	            // Bind the texture
	            glBindTexture(GL_TEXTURE_2D, texture.getId());
	        }
	        GLTexture normalMap = material.normalMap();
	        if (normalMap != null) {
	            // Activate first texture bank
	            glActiveTexture(GL_TEXTURE1);
	            // Bind the texture
	            glBindTexture(GL_TEXTURE_2D, normalMap.getId());
	        }

	        // Draw the mesh
	        glBindVertexArray(vaoId);
		}
		
		protected void endRender() {
	        // Restore state
	        glBindVertexArray(0);

	        glBindTexture(GL_TEXTURE_2D, 0);
	    }
		
		public void render(GLMaterial material) {
	    	initRender(material);

	        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

	        endRender();
	    }
		
		public void renderList(List<GameItem> gameItems, Consumer<GameItem> consumer, GLMaterial material) {
	        initRender(material);

	        for (GameItem gameItem : gameItems) {
	            // Set up data required by GameItem
	            consumer.accept(gameItem);
	            // Render this game item
	            glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
	        }

	        endRender();
	    }
		 
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
