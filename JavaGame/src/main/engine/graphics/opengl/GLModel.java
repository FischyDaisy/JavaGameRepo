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
import main.engine.graphics.TextureCache;
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
	
	public static List<GLModel> transformModels(List<ModelData> modelDataList, TextureCache textureCache) throws Exception {
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
			GLMaterial defaultGLMaterial, TextureCache textureCache) throws Exception {
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
	
	public static GLMaterial transformMaterial(ModelData.Material material, TextureCache textureCache) throws Exception {
		GLTexture texture = textureCache.getTexture(material.texturePath(), material.cols(), material.rows());
		return new GLMaterial(material.ambientColor(), material.diffuseColor(), material.specularColor(),
				texture, null, material.reflectance().x(), new ArrayList<GLMesh>());
	}
	
	public record GLMaterial(Vector4f ambientColor, Vector4f diffuseColor, Vector4f specularColor, GLTexture texture, 
			GLTexture normalMap, float reflectance, List<GLMesh> glMeshList) {
		public static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		public GLMaterial(List<GLMesh> glMeshList) {
			this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, null, null, 0f, glMeshList);
		}
		
		public GLMaterial(Vector4f color, float reflectance, List<GLMesh> glMeshList) {
			this(color, color, color, null, null, reflectance, glMeshList);
		}
		
		public GLMaterial(GLTexture texture, List<GLMesh> glMeshList) {
			this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, texture, null, 0f, glMeshList);
		}
		
		public GLMaterial(GLTexture texture, GLTexture normalMap, List<GLMesh> glMeshList) {
			this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, texture, normalMap, 0f, glMeshList);
		}
		
		public GLMaterial(GLTexture texture, float reflectance, List<GLMesh> glMeshList) {
			this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, texture, null, reflectance, glMeshList);
		}
		
		public GLMaterial(GLTexture texture, GLTexture normalMap, float reflectance, List<GLMesh> glMeshList) {
			this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, texture, normalMap, reflectance, glMeshList);
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
