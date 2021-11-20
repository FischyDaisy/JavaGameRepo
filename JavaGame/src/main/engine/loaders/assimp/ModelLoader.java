package main.engine.loaders.assimp;

import org.joml.Vector4f;
import org.joml.primitives.AABBf;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.GraphConstants;
import main.engine.utility.Utils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.assimp.Assimp.*;

public class ModelLoader {

    private ModelLoader() {
        // Utility class
    }

    public static ModelData loadModel(String modelId, String modelPath, String texturesDir) {
        return loadModel(modelId, modelPath, texturesDir, aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices |
                aiProcess_Triangulate | aiProcess_FixInfacingNormals | aiProcess_CalcTangentSpace |
                aiProcess_PreTransformVertices);
    }

    public static ModelData loadModel(String modelId, String modelPath, String texturesDir, int flags) {
        //LOGGER.debug("Loading model data [{}]", modelPath);
        if (!new File(modelPath).exists()) {
            throw new RuntimeException("Model path does not exist [" + modelPath + "]");
        }
        if (!new File(texturesDir).exists()) {
            throw new RuntimeException("Textures path does not exist [" + texturesDir + "]");
        }

        AIScene aiScene = aiImportFile(modelPath, flags);
        if (aiScene == null) {
            throw new RuntimeException("Error loading model [modelPath: " + modelPath + ", texturesDir:" + texturesDir + "]");
        }

        int numMaterials = aiScene.mNumMaterials();
        List<ModelData.Material> materialList = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiScene.mMaterials().get(i));
            ModelData.Material material = processMaterial(aiMaterial, texturesDir);
            materialList.add(material);
        }

        int numMeshes = aiScene.mNumMeshes();
        PointerBuffer aiMeshes = aiScene.mMeshes();
        List<ModelData.MeshData> meshDataList = new ArrayList<>();
        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            ModelData.MeshData meshData = processMesh(aiMesh);
            meshDataList.add(meshData);
        }

        ModelData modelData = new ModelData(modelId, meshDataList, materialList);

        aiReleaseImport(aiScene);
        //LOGGER.debug("Loaded model [{}]", modelPath);
        return modelData;
    }

    protected static List<Integer> processIndices(AIMesh aiMesh) {
        List<Integer> indices = new ArrayList<Integer>();
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }
        return indices;
    }

    private static ModelData.Material processMaterial(AIMaterial aiMaterial, String texturesDir) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            AIColor4D color = AIColor4D.calloc(stack);
            
            Vector4f ambient = ModelData.Material.DEFAULT_COLOR;
            int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0,
                    color);
            if (result == aiReturn_SUCCESS) {
                ambient = new Vector4f(color.r(), color.g(), color.b(), color.a());
            }

            Vector4f diffuse = ModelData.Material.DEFAULT_COLOR;
            result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0,
                    color);
            if (result == aiReturn_SUCCESS) {
                diffuse = new Vector4f(color.r(), color.g(), color.b(), color.a());
            }
            
            Vector4f specular = ModelData.Material.DEFAULT_COLOR;
            result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0,
                    color);
            if (result == aiReturn_SUCCESS) {
                specular = new Vector4f(color.r(), color.g(), color.b(), color.a());
            }
            
            Vector4f reflectance = ModelData.Material.DEFAULT_COLOR;
            result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_REFLECTIVE, aiTextureType_NONE, 0, 
            		color);
            if (result == aiReturn_SUCCESS) {
                reflectance = new Vector4f(color.r(), color.g(), color.b(), color.a());
            }
            AIString aiTexturePath = AIString.calloc(stack);
            aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, aiTexturePath, (IntBuffer) null,
                    null, null, null, null, null);
            String texturePath = aiTexturePath.dataString();
            if (texturePath != null && texturePath.length() > 0) {
                texturePath = texturesDir + File.separator + new File(texturePath).getName();
                diffuse = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
            }

            return new ModelData.Material(texturePath, ambient,  diffuse, specular, reflectance);
        }
    }

    private static ModelData.MeshData processMesh(AIMesh aiMesh) {
        List<Float> vertices = processVertices(aiMesh);
        List<Float> textCoords = processTextCoords(aiMesh);
        List<Float> normals = processNormals(aiMesh);
        List<Integer> indices = processIndices(aiMesh);

        // Texture coordinates may not have been populated. We need at least the empty slots
        if (textCoords.isEmpty()) {
            int numElements = (vertices.size() / 3) * 2;
            for (int i = 0; i < numElements; i++) {
                textCoords.add(0.0f);
            }
        }

        int materialIdx = aiMesh.mMaterialIndex();
        return new ModelData.MeshData(Utils.listFloatToArray(vertices), Utils.listFloatToArray(textCoords), Utils.listFloatToArray(normals), 
        		Utils.listIntToArray(indices), materialIdx);
    }

    private static List<Float> processTextCoords(AIMesh aiMesh) {
        List<Float> textCoords = new ArrayList<Float>();
        AIVector3D.Buffer aiTextCoords = aiMesh.mTextureCoords(0);
        int numTextCoords = aiTextCoords != null ? aiTextCoords.remaining() : 0;
        for (int i = 0; i < numTextCoords; i++) {
            AIVector3D textCoord = aiTextCoords.get();
            textCoords.add(textCoord.x());
            textCoords.add(1 - textCoord.y());
        }
        return textCoords;
    }

    private static List<Float> processVertices(AIMesh aiMesh) {
        List<Float> vertices = new ArrayList<Float>();
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();
            vertices.add(aiVertex.x());
            vertices.add(aiVertex.y());
            vertices.add(aiVertex.z());
        }
        return vertices;
    }
    
    private static List<Float> processNormals(AIMesh aiMesh) {
    	List<Float> normals = new ArrayList<Float>();
    	AIVector3D.Buffer aiNormals = aiMesh.mNormals();
    	while (aiNormals != null && aiNormals.remaining() > 0) {
    		AIVector3D aiNormal = aiNormals.get();
    		normals.add(aiNormal.x());
    		normals.add(aiNormal.y());
    		normals.add(aiNormal.z());
    	}
    	return normals;
    }
    
    private static AABBf processAABB(AIMesh aiMesh) {
    	AIAABB aiAABB = aiMesh.mAABB();
    	AIVector3D min = aiAABB.mMin();
    	AIVector3D max = aiAABB.mMax();
    	AABBf aabb = new AABBf(min.x(), min.y(), min.z(),
    			max.x(), max.y(), max.z());
    	return aabb;
    	
    }
}