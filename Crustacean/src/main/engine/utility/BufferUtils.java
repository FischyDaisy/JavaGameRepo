package main.engine.utility;

import main.engine.enginelayouts.MaterialLayout;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.*;
import org.joml.Matrix4f;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;

public final class BufferUtils {
    public static long loadAnimationData(ModelData modelData, VulkanModel vulkanModel, StagingBuffer animJointMatricesStgBuffer, long offset) {
        List<ModelData.Animation> animationsList = modelData.getAnimationsList();
        if (!modelData.hasAnimations()) {
            return offset;
        }
        ByteBuffer dataBuffer = animJointMatricesStgBuffer.getDataBuffer();
        MemorySegment jointData = animJointMatricesStgBuffer.getDataSegment();
        float[] matArr = new float[16];
        for (ModelData.Animation animation : animationsList) {
            VulkanModel.VulkanAnimationData vulkanAnimationData = new VulkanModel.VulkanAnimationData();
            vulkanModel.addVulkanAnimationData(vulkanAnimationData);
            List<ModelData.AnimatedFrame> frameList = animation.frames();
            for (ModelData.AnimatedFrame frame : frameList) {
                vulkanAnimationData.addVulkanAnimationFrame(new VulkanModel.VulkanAnimationFrame((int) offset));
                Matrix4f[] matrices = frame.jointMatrices();
                for (Matrix4f matrix : matrices) {
                    matrix.get(matArr);
                    MemorySegment.copy(matArr, 0, jointData, ValueLayout.JAVA_FLOAT, offset, matArr.length);
                    offset += GraphConstants.MAT4X4_SIZE_BYTES;
                }
            }
        }
        return offset;
    }

    public static long loadMaterials(Device device, VKTextureCache textureCache, StagingBuffer materialsStgBuffer,
                  List<ModelData.Material> materialList, List<VulkanModel.VulkanMaterial> vulkanMaterialList, List<VKTexture> textureList, long materialOffset) {
        MemorySegment dataBuffer = materialsStgBuffer.getDataSegment();
        for (ModelData.Material material : materialList) {

            VKTexture texture = textureCache.get(device, material.texturePath(), VK_FORMAT_R8G8B8A8_SRGB);
            if (texture != null) {
                textureList.add(texture);
            }
            int textureIdx = textureCache.getPosition(material.texturePath());

            texture = textureCache.get(device, material.normalMapPath(), VK_FORMAT_R8G8B8A8_UNORM);
            if (texture != null) {
                textureList.add(texture);
            }
            int normalMapIdx = textureCache.getPosition(material.normalMapPath());

            texture = textureCache.get(device, material.metalRoughMap(), VK_FORMAT_R8G8B8A8_UNORM);
            if (texture != null) {
                textureList.add(texture);
            }
            int metalRoughMapIdx = textureCache.getPosition(material.metalRoughMap());

            vulkanMaterialList.add(new VulkanModel.VulkanMaterial((int) (materialOffset / MaterialLayout.LAYOUT.byteSize())));
            MaterialLayout.setDiffuseColor(dataBuffer, materialOffset, material.diffuseColor());
            MaterialLayout.setTextureIdx(dataBuffer, materialOffset, textureIdx);
            MaterialLayout.setNormalMapIdx(dataBuffer, materialOffset, normalMapIdx);
            MaterialLayout.setMetalRoughMapIdx(dataBuffer, materialOffset, metalRoughMapIdx);
            MaterialLayout.setRoughnessFactor(dataBuffer, materialOffset, material.roughnessFactor());
            MaterialLayout.setMetallicFactor(dataBuffer, materialOffset, material.metallicFactor());
            materialOffset += MaterialLayout.LAYOUT.byteSize();
        }

        return materialOffset;
    }

    public static BufferOffsets loadMeshes(StagingBuffer verticesStgBuffer, StagingBuffer indicesStgBuffer, StagingBuffer animWeightsStgBuffer,
                            ModelData modelData, VulkanModel vulkanModel, List<VulkanModel.VulkanMaterial> vulkanMaterialList, BufferOffsets offsets) {
        MemorySegment verticesData = verticesStgBuffer.getDataSegment();
        MemorySegment indicesData = indicesStgBuffer.getDataSegment();
        List<ModelData.MeshData> meshDataList = modelData.getMeshDataList();
        int meshCount = 0;
        long vertexOffset = offsets.vertexOffset, indexOffset = offsets.indexOffset, weightsOffset = offsets.weightsOffset;
        for (ModelData.MeshData meshData : meshDataList) {
            float[] positions = meshData.positions();
            float[] normals = meshData.normals();
            float[] tangents = meshData.tangents();
            float[] biTangents = meshData.biTangents();
            float[] textCoords = meshData.textCoords();
            if (textCoords == null || textCoords.length == 0) {
                textCoords = new float[(positions.length / 3) * 2];
            }
            int[] indices = meshData.indices();

            int numElements = positions.length + normals.length + tangents.length + biTangents.length + textCoords.length;
            int verticesSize = numElements * GraphConstants.FLOAT_SIZE_BYTES;

            int localMaterialIdx = meshData.materialIdx();
            int globalMaterialIdx = 0;
            if (localMaterialIdx >= 0 && localMaterialIdx < vulkanMaterialList.size()) {
                globalMaterialIdx = vulkanMaterialList.get(localMaterialIdx).globalMaterialIdx();
            }
            vulkanModel.addVulkanMesh(new VulkanModel.VulkanMesh(verticesSize, indices.length,
                    (int) vertexOffset, (int) indexOffset, globalMaterialIdx, (int) weightsOffset));

            VarHandle floatHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_FLOAT);
            int rows = positions.length / 3;
            for (int row = 0; row < rows; row++) {
                int startPos = row * 3;
                int startTextCoord = row * 2;
                MemorySegment.copy(positions, startPos, verticesData, ValueLayout.JAVA_FLOAT, vertexOffset, 3);
                vertexOffset += 3 * ValueLayout.JAVA_FLOAT.byteSize();
                MemorySegment.copy(normals, startPos, verticesData, ValueLayout.JAVA_FLOAT, vertexOffset, 3);
                vertexOffset += 3 * ValueLayout.JAVA_FLOAT.byteSize();
                MemorySegment.copy(tangents, startPos, verticesData, ValueLayout.JAVA_FLOAT, vertexOffset, 3);
                vertexOffset += 3 * ValueLayout.JAVA_FLOAT.byteSize();
                MemorySegment.copy(biTangents, startPos, verticesData, ValueLayout.JAVA_FLOAT, vertexOffset, 3);
                vertexOffset += 3 * ValueLayout.JAVA_FLOAT.byteSize();
                MemorySegment.copy(textCoords, startTextCoord, verticesData, ValueLayout.JAVA_FLOAT, vertexOffset, 2);
                vertexOffset += 2 * ValueLayout.JAVA_FLOAT.byteSize();
            }

            MemorySegment.copy(indices, 0, indicesData, ValueLayout.JAVA_INT, indexOffset, indices.length);
            indexOffset += indices.length * ValueLayout.JAVA_INT.byteSize();

            weightsOffset = BufferUtils.loadWeightsBuffer(modelData, animWeightsStgBuffer, meshCount, weightsOffset);
            meshCount++;
        }
        return new BufferOffsets(vertexOffset, indexOffset, weightsOffset);
    }

    public static long loadWeightsBuffer(ModelData modelData, StagingBuffer animWeightsBuffer, int meshCount, long offset) {
        List<ModelData.AnimMeshData> animMeshDataList = modelData.getAnimMeshDataList();
        if (animMeshDataList == null || animMeshDataList.isEmpty()) {
            return offset;
        }

        ModelData.AnimMeshData animMeshData = animMeshDataList.get(meshCount);
        float[] weights = animMeshData.weights();
        float[] boneIds = animMeshData.boneIds();

        MemorySegment weightData = animWeightsBuffer.getDataSegment();

        int rows = weights.length / 4;
        for (int row = 0; row < rows; row++) {
            int startPos = row * 4;
            MemorySegment.copy(weights, startPos, weightData, ValueLayout.JAVA_FLOAT, offset, 4);
            offset += 4 * ValueLayout.JAVA_FLOAT.byteSize();
            MemorySegment.copy(boneIds, startPos, weightData, ValueLayout.JAVA_FLOAT, offset, 4);
            offset += 4 * ValueLayout.JAVA_FLOAT.byteSize();
        }
        return offset;
    }

    public record BufferOffsets(long vertexOffset, long indexOffset, long weightsOffset) {}
}
