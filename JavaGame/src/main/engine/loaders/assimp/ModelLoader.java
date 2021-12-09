package main.engine.loaders.assimp;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.joml.primitives.AABBf;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import main.engine.graphics.GraphConstants;
import main.engine.graphics.ModelData;
import main.engine.graphics.animation.AnimGameItem;
import main.engine.graphics.animation.AnimatedFrame;
import main.engine.graphics.animation.Animation;
import main.engine.utility.Utils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;

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
        List<ModelData.Material> materialList = new ArrayList<ModelData.Material>();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiScene.mMaterials().get(i));
            ModelData.Material material = processMaterial(aiMaterial, texturesDir);
            materialList.add(material);
        }

        int numMeshes = aiScene.mNumMeshes();
        PointerBuffer aiMeshes = aiScene.mMeshes();
        List<ModelData.MeshData> meshDataList = new ArrayList<ModelData.MeshData>();
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
    
    public static ModelData loadAnimModel(String modelId, String modelPath, String texturesDir) throws Exception {
        return loadAnimModel(modelId, modelPath, texturesDir,
                aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
                        | aiProcess_FixInfacingNormals | aiProcess_LimitBoneWeights);
    }
    
    public static ModelData loadAnimModel(String modelId, String modelPath, String texturesDir, int flags)
            throws Exception {
        AIScene aiScene = aiImportFile(modelPath, flags);
        if (aiScene == null) {
            throw new Exception("Error loading model");
        }

        int numMaterials = aiScene.mNumMaterials();
        List<ModelData.Material> materialList = new ArrayList<ModelData.Material>();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiScene.mMaterials().get(i));
            ModelData.Material material = processMaterial(aiMaterial, texturesDir);
            materialList.add(material);
        }

        List<Bone> boneList = new ArrayList<>();
        int numMeshes = aiScene.mNumMeshes();
        PointerBuffer aiMeshes = aiScene.mMeshes();
        List<ModelData.MeshData> meshDataList = new ArrayList<ModelData.MeshData>();
        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            ModelData.MeshData meshData = processAnimMesh(aiMesh, boneList);
            meshDataList.add(meshData);
        }
        
        ModelData modelData = new ModelData(modelId, meshDataList, materialList);

        Node rootNode = buildNodesTree(aiScene.mRootNode(), null);
        Matrix4f globalInverseTransformation = toMatrix(aiScene.mRootNode().mTransformation()).invert();
        Map<String, Animation> animations = processAnimations(aiScene, boneList, rootNode,
                globalInverseTransformation);
        modelData.setAnimations(animations);
        
        aiReleaseImport(aiScene);

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

            return new ModelData.Material(texturePath, ambient,  diffuse, specular, reflectance, 1, 1);
        }
    }

    private static ModelData.MeshData processMesh(AIMesh aiMesh) {
        List<Float> vertices = processVertices(aiMesh);
        List<Float> textCoords = processTextCoords(aiMesh);
        List<Float> normals = processNormals(aiMesh);
        List<Integer> indices = processIndices(aiMesh);
        AABBf aabb = processAABB(aiMesh);

        // Texture coordinates may not have been populated. We need at least the empty slots
        if (textCoords.isEmpty()) {
            int numElements = (vertices.size() / 3) * 2;
            for (int i = 0; i < numElements; i++) {
                textCoords.add(0.0f);
            }
        }

        int materialIdx = aiMesh.mMaterialIndex();
        return new ModelData.MeshData(Utils.listFloatToArray(vertices), Utils.listFloatToArray(textCoords), Utils.listFloatToArray(normals), 
        		Utils.listIntToArray(indices), materialIdx, aabb);
    }
    
    private static List<Integer> processBones(AIMesh aiMesh, List<Bone> boneList, List<Float> weights) {
    	Map<Integer, List<VertexWeight>> weightSet = new HashMap<Integer, List<VertexWeight>>();
    	List<Integer> boneIds = new ArrayList<Integer>();
    	int numBones = aiMesh.mNumBones();
    	PointerBuffer aiBones = aiMesh.mBones();
    	for (int i = 0; i < numBones; i++) {
    		AIBone aiBone = AIBone.create(aiBones.get(i));
    		int id = boneList.size();
    		Bone bone = new Bone(id, aiBone.mName().dataString(), toMatrix(aiBone.mOffsetMatrix()));
    		boneList.add(bone);
    		int numWeights = aiBone.mNumWeights();
    		AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
    		for (int j = 0; j < numWeights; j++) {
    			AIVertexWeight aiWeight = aiWeights.get(j);
    			VertexWeight vw = new VertexWeight(bone.getBoneId(), aiWeight.mVertexId(),
    					aiWeight.mWeight());
    			List<VertexWeight> vertexWeightList = weightSet.get(vw.getVertexId());
    			if (vertexWeightList == null) {
    				vertexWeightList = new ArrayList<>();
    				weightSet.put(vw.getVertexId(), vertexWeightList);
    			}
    			vertexWeightList.add(vw);
    		}
    	}
    	
    	int numVertices = aiMesh.mNumVertices();
    	for (int i = 0; i < numVertices; i++) {
    		List<VertexWeight> vertexWeightList = weightSet.get(i);
    		int size = vertexWeightList != null ? vertexWeightList.size() : 0;
    		for (int j = 0; j < ModelData.MeshData.MAX_WEIGHTS; j++) {
    			if (j < size) {
    				VertexWeight vw = vertexWeightList.get(j);
    				weights.add(vw.getWeight());
    				boneIds.add(vw.getBoneId());
    				} else {
    					weights.add(0.0f);
    					boneIds.add(0);
    				}
    			}
    	}
    	return boneIds;
    }
    
    private static final ModelData.MeshData processAnimMesh(AIMesh aiMesh, List<Bone> boneList) {
    	List<Float> vertices = processVertices(aiMesh);
        List<Float> textCoords = processTextCoords(aiMesh);
        List<Float> normals = processNormals(aiMesh);
        List<Integer> indices = processIndices(aiMesh);
        List<Float> weights = new ArrayList<Float>();
        List<Integer> boneIds = processBones(aiMesh, boneList, weights);
        AABBf aabb = processAABB(aiMesh);
        
        // Texture coordinates may not have been populated. We need at least the empty slots
        if (textCoords.isEmpty()) {
            int numElements = (vertices.size() / 3) * 2;
            for (int i = 0; i < numElements; i++) {
                textCoords.add(0.0f);
            }
        }
        
        int materialIdx = aiMesh.mMaterialIndex();
        return new ModelData.MeshData(Utils.listFloatToArray(vertices), Utils.listFloatToArray(textCoords), Utils.listFloatToArray(normals), 
        		Utils.listIntToArray(indices), Utils.listIntToArray(boneIds), Utils.listFloatToArray(weights), materialIdx, aabb);
    }

    private static final List<Float> processTextCoords(AIMesh aiMesh) {
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

    private static final List<Float> processVertices(AIMesh aiMesh) {
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
    
    private static final List<Float> processNormals(AIMesh aiMesh) {
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
    
    private static final AABBf processAABB(AIMesh aiMesh) {
    	AIAABB aiAABB = aiMesh.mAABB();
    	AIVector3D min = aiAABB.mMin();
    	AIVector3D max = aiAABB.mMax();
    	AABBf aabb = new AABBf(min.x(), min.y(), min.z(),
    			max.x(), max.y(), max.z());
    	return aabb;
    	
    }
    
    private static Node buildNodesTree(AINode aiNode, Node parentNode) {
        String nodeName = aiNode.mName().dataString();
        Node node = new Node(nodeName, parentNode, toMatrix(aiNode.mTransformation()));

        int numChildren = aiNode.mNumChildren();
        PointerBuffer aiChildren = aiNode.mChildren();
        for (int i = 0; i < numChildren; i++) {
            AINode aiChildNode = AINode.create(aiChildren.get(i));
            Node childNode = buildNodesTree(aiChildNode, node);
            node.addChild(childNode);
        }
        return node;
    }

    private static Map<String, Animation> processAnimations(AIScene aiScene, List<Bone> boneList,
                                                            Node rootNode, Matrix4f globalInverseTransformation) {
        Map<String, Animation> animations = new HashMap<>();

        // Process all animations
        int numAnimations = aiScene.mNumAnimations();
        PointerBuffer aiAnimations = aiScene.mAnimations();
        for (int i = 0; i < numAnimations; i++) {
            AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));
            int maxFrames = calcAnimationMaxFrames(aiAnimation);

            List<AnimatedFrame> frames = new ArrayList<>();
            Animation animation = new Animation(aiAnimation.mName().dataString(), frames, aiAnimation.mDuration());
            animations.put(animation.getName(), animation);

            for (int j = 0; j < maxFrames; j++) {
                AnimatedFrame animatedFrame = new AnimatedFrame();
                buildFrameMatrices(aiAnimation, boneList, animatedFrame, j, rootNode,
                        rootNode.getNodeTransformation(), globalInverseTransformation);
                frames.add(animatedFrame);
            }
        }
        return animations;
    }

    private static void buildFrameMatrices(AIAnimation aiAnimation, List<Bone> boneList, AnimatedFrame animatedFrame, int frame,
                                           Node node, Matrix4f parentTransformation, Matrix4f globalInverseTransform) {
        String nodeName = node.getName();
        AINodeAnim aiNodeAnim = findAIAnimNode(aiAnimation, nodeName);
        Matrix4f nodeTransform = node.getNodeTransformation();
        if (aiNodeAnim != null) {
            nodeTransform = buildNodeTransformationMatrix(aiNodeAnim, frame);
        }
        Matrix4f nodeGlobalTransform = new Matrix4f(parentTransformation).mul(nodeTransform);

        List<Bone> affectedBones = boneList.stream().filter( b -> b.getBoneName().equals(nodeName)).collect(Collectors.toList());
        for (Bone bone: affectedBones) {
            Matrix4f boneTransform = new Matrix4f(globalInverseTransform).mul(nodeGlobalTransform).
                    mul(bone.getOffsetMatrix());
            animatedFrame.setMatrix(bone.getBoneId(), boneTransform);
        }

        for (Node childNode : node.getChildren()) {
            buildFrameMatrices(aiAnimation, boneList, animatedFrame, frame, childNode, nodeGlobalTransform,
                    globalInverseTransform);
        }
    }
    
    private static Matrix4f buildNodeTransformationMatrix(AINodeAnim aiNodeAnim, int frame) {
        AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
        AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
        AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

        AIVectorKey aiVecKey;
        AIVector3D vec;

        Matrix4f nodeTransform = new Matrix4f();
        int numPositions = aiNodeAnim.mNumPositionKeys();
        if (numPositions > 0) {
            aiVecKey = positionKeys.get(Math.min(numPositions - 1, frame));
            vec = aiVecKey.mValue();
            nodeTransform.translate(vec.x(), vec.y(), vec.z());
        }
        int numRotations = aiNodeAnim.mNumRotationKeys();
        if (numRotations > 0) {
            AIQuatKey quatKey = rotationKeys.get(Math.min(numRotations - 1, frame));
            AIQuaternion aiQuat = quatKey.mValue();
            Quaternionf quat = new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
            nodeTransform.rotate(quat);
        }
        int numScalingKeys = aiNodeAnim.mNumScalingKeys();
        if (numScalingKeys > 0) {
            aiVecKey = scalingKeys.get(Math.min(numScalingKeys - 1, frame));
            vec = aiVecKey.mValue();
            nodeTransform.scale(vec.x(), vec.y(), vec.z());
        }

        return nodeTransform;
    }

    private static AINodeAnim findAIAnimNode(AIAnimation aiAnimation, String nodeName) {
        AINodeAnim result = null;
        int numAnimNodes = aiAnimation.mNumChannels();
        PointerBuffer aiChannels = aiAnimation.mChannels();
        for (int i=0; i<numAnimNodes; i++) {
            AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
            if ( nodeName.equals(aiNodeAnim.mNodeName().dataString())) {
                result = aiNodeAnim;
                break;
            }
        }
        return result;
    }

    private static int calcAnimationMaxFrames(AIAnimation aiAnimation) {
        int maxFrames = 0;
        int numNodeAnims = aiAnimation.mNumChannels();
        PointerBuffer aiChannels = aiAnimation.mChannels();
        for (int i=0; i<numNodeAnims; i++) {
            AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
            int numFrames = Math.max(Math.max(aiNodeAnim.mNumPositionKeys(), aiNodeAnim.mNumScalingKeys()),
                    aiNodeAnim.mNumRotationKeys());
            maxFrames = Math.max(maxFrames, numFrames);
        }

        return maxFrames;
    }
    
    private static final Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4) {
        Matrix4f result = new Matrix4f();
        result.m00(aiMatrix4x4.a1());
        result.m10(aiMatrix4x4.a2());
        result.m20(aiMatrix4x4.a3());
        result.m30(aiMatrix4x4.a4());
        result.m01(aiMatrix4x4.b1());
        result.m11(aiMatrix4x4.b2());
        result.m21(aiMatrix4x4.b3());
        result.m31(aiMatrix4x4.b4());
        result.m02(aiMatrix4x4.c1());
        result.m12(aiMatrix4x4.c2());
        result.m22(aiMatrix4x4.c3());
        result.m32(aiMatrix4x4.c4());
        result.m03(aiMatrix4x4.d1());
        result.m13(aiMatrix4x4.d2());
        result.m23(aiMatrix4x4.d3());
        result.m33(aiMatrix4x4.d4());

        return result;
    }
}