package main.engine.graphics.vulkan.skybox;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import main.engine.EngineProperties;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.*;
import main.engine.graphics.vulkan.Queue;
import main.engine.items.SkyBox;
import main.engine.utility.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkDrawIndexedIndirectCommand;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;

public class SkyboxBuffers {

    private final VulkanBuffer animJointMatricesBuffer;
    private final long jointMatricesBufferSize;
    private final VulkanBuffer animWeightsBuffer;
    private final long weightsBufferSize;
    private final VulkanBuffer indicesBuffer;
    private final long indexBufferSize;
    private final VulkanBuffer materialsBuffer;
    private final long materialBufferSize;
    private final VulkanBuffer verticesBuffer;
    private final long vertexBufferSize;
    private VulkanBuffer indirectBuffer;
    private VulkanBuffer[] instanceDataBuffers;
    private int numIndirectCommands;

    public SkyboxBuffers(Device device) {
        Logger.debug("Creating skybox buffers");
        EngineProperties engProps = EngineProperties.INSTANCE;
        vertexBufferSize = engProps.getMaxSkyboxVerticesBuffer();
        verticesBuffer = new VulkanBuffer(device, vertexBufferSize, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT |
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        indexBufferSize = engProps.getMaxSkyboxIndicesBuffer();
        indicesBuffer = new VulkanBuffer(device, indexBufferSize, VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        int maxMaterials = engProps.getMaxSkyboxMaterials();
        materialBufferSize = (long) maxMaterials * GraphConstants.VECTOR4F_SIZE_BYTES * 9;
        materialsBuffer = new VulkanBuffer(device, materialBufferSize, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        jointMatricesBufferSize = engProps.getMaxJointMatricesBuffer();
        animJointMatricesBuffer = new VulkanBuffer(device, jointMatricesBufferSize, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        weightsBufferSize = engProps.getMaxAnimWeightsBuffer();
        animWeightsBuffer = new VulkanBuffer(device, weightsBufferSize, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
    }

    public void cleanup() {
        Logger.debug("cleaning up skybox buffers");
        verticesBuffer.cleanup();
        indicesBuffer.cleanup();
        if (indirectBuffer != null) {
            indirectBuffer.cleanup();
        }
        materialsBuffer.cleanup();
        animJointMatricesBuffer.cleanup();
        animWeightsBuffer.cleanup();
        if (instanceDataBuffers != null) {
            Arrays.stream(instanceDataBuffers).forEach(VulkanBuffer::cleanup);
        }
    }

    public VulkanBuffer getAnimJointMatricesBuffer() {
        return animJointMatricesBuffer;
    }

    public VulkanBuffer getAnimWeightsBuffer() {
        return animWeightsBuffer;
    }

    public VulkanBuffer getIndicesBuffer() {
        return indicesBuffer;
    }

    public VulkanBuffer getIndirectBuffer() {
        return indirectBuffer;
    }

    public VulkanBuffer[] getInstanceDataBuffers() {
        return instanceDataBuffers;
    }

    public VulkanBuffer getMaterialsBuffer() {
        return materialsBuffer;
    }

    public int getNumIndirectCommands() {
        return numIndirectCommands;
    }

    public VulkanBuffer getVerticesBuffer() {
        return verticesBuffer;
    }

    public void loadSkybox(VulkanModel skyboxModel, CommandPool commandPool,
                           Queue queue, int numSwapChainImages) {
        numIndirectCommands = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Device device = commandPool.getDevice();
            CommandBuffer cmd = new CommandBuffer(commandPool, true, true);

            List<VkDrawIndexedIndirectCommand> indexedIndirectCommandList = new ArrayList<>();
            int firstInstance = 0;
            for (VulkanModel.VulkanMesh vulkanMesh : skyboxModel.vulkanMeshList) {
                VkDrawIndexedIndirectCommand indexedIndirectCommand = VkDrawIndexedIndirectCommand.calloc(stack);
                indexedIndirectCommand.indexCount(vulkanMesh.numIndices());
                indexedIndirectCommand.firstIndex(vulkanMesh.indicesOffset() / GraphConstants.INT_SIZE_BYTES);
                indexedIndirectCommand.instanceCount(1);
                indexedIndirectCommand.vertexOffset(vulkanMesh.verticesOffset() / VertexBufferStructure.SIZE_IN_BYTES);
                indexedIndirectCommand.firstInstance(firstInstance);
                indexedIndirectCommandList.add(indexedIndirectCommand);

                numIndirectCommands++;
                firstInstance++;
            }
            if (numIndirectCommands > 0) {
                cmd.beginRecording();

                StagingBuffer indirectStgBuffer = new StagingBuffer(device, (long) GlobalBuffers.IND_COMMAND_STRIDE * numIndirectCommands);
                if (indirectBuffer != null) {
                    indirectBuffer.cleanup();
                }
                indirectBuffer = new VulkanBuffer(device, indirectStgBuffer.getRequestedSize(),
                        VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                        VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
                ByteBuffer dataBuffer = indirectStgBuffer.getDataBuffer();
                VkDrawIndexedIndirectCommand.Buffer indCommandBuffer = new VkDrawIndexedIndirectCommand.Buffer(dataBuffer);

                indexedIndirectCommandList.forEach(indCommandBuffer::put);

                if (instanceDataBuffers != null) {
                    Arrays.stream(instanceDataBuffers).forEach(VulkanBuffer::cleanup);
                }
                instanceDataBuffers = new VulkanBuffer[numSwapChainImages];
                for (int i = 0; i < numSwapChainImages; i++) {
                    instanceDataBuffers[i] = new VulkanBuffer(device, (long) numIndirectCommands * (GraphConstants.MAT4X4_SIZE_BYTES + GraphConstants.INT_SIZE_BYTES),
                            VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
                }

                indirectStgBuffer.recordTransferCommand(cmd, indirectBuffer);

                cmd.endRecording();
                cmd.submitAndWait(device, queue);
                cmd.cleanup();
                indirectStgBuffer.cleanup();
            }
        }
    }

    public void loadSkyboxInstanceData(Dominion dominion, VulkanModel skyboxModel, int currentSwapChainIdx) {
        if (instanceDataBuffers == null) {
            return;
        }
        VulkanBuffer instanceBuffer = instanceDataBuffers[currentSwapChainIdx];
        if (instanceBuffer == null) {
            return;
        }
        long mappedMemory = instanceBuffer.map();
        ByteBuffer dataBuffer = MemoryUtil.memByteBuffer(mappedMemory, (int) instanceBuffer.getRequestedSize());
        instanceBuffer.map();
        int pos = 0;
        Results<Results.With1<SkyBox>> results = dominion.findEntitiesWith(SkyBox.class);
        Iterator<Results.With1<SkyBox>> itr = results.iterator();
        if (!itr.hasNext()) {
            return;
        }
        SkyBox skybox = itr.next().comp();
        for (VulkanModel.VulkanMesh vulkanMesh : skyboxModel.vulkanMeshList) {
            skybox.getModelMatrix().get(pos, dataBuffer);
            pos += GraphConstants.MAT4X4_SIZE_BYTES;
            dataBuffer.putInt(pos, vulkanMesh.globalMaterialIdx());
            pos += GraphConstants.INT_SIZE_BYTES;
        }
        instanceBuffer.unMap();
    }

    public VulkanModel loadSkyboxModel(ModelData skyboxModel, VKTextureCache textureCache, CommandPool
            commandPool, Queue queue) {
        List<VKTexture> textureList = new ArrayList<>();
        Device device = commandPool.getDevice();
        CommandBuffer cmd = new CommandBuffer(commandPool, true, true);

        StagingBuffer verticesStgBuffer = new StagingBuffer(device, verticesBuffer.getRequestedSize());
        StagingBuffer indicesStgBuffer = new StagingBuffer(device, indicesBuffer.getRequestedSize());
        StagingBuffer materialsStgBuffer = new StagingBuffer(device, materialsBuffer.getRequestedSize());
        StagingBuffer animJointMatricesStgBuffer = new StagingBuffer(device, animJointMatricesBuffer.getRequestedSize());
        StagingBuffer animWeightsStgBuffer = new StagingBuffer(device, animWeightsBuffer.getRequestedSize());

        cmd.beginRecording();

        // Load a default material
        long materialsOffset = 0;
        List<ModelData.Material> defaultMaterialList = Collections.singletonList(new ModelData.Material());
        materialsOffset = BufferUtils.loadMaterials(device, textureCache, materialsStgBuffer, defaultMaterialList, new ArrayList<>(), textureList, materialsOffset);

        VulkanModel vulkanModel = new VulkanModel(skyboxModel.getModelId());

        List<VulkanModel.VulkanMaterial> vulkanMaterialList = new ArrayList<>();
        BufferUtils.loadMaterials(device, textureCache, materialsStgBuffer,
                skyboxModel.getMaterialList(), vulkanMaterialList, textureList, materialsOffset);
        BufferUtils.loadMeshes(verticesStgBuffer, indicesStgBuffer, animWeightsStgBuffer, skyboxModel, vulkanModel, vulkanMaterialList,
                new BufferUtils.BufferOffsets(0L,0L, 0L));
        BufferUtils.loadAnimationData(skyboxModel, vulkanModel, animJointMatricesStgBuffer, 0L);

        // We need to ensure that at least we have one texture
        if (textureList.isEmpty()) {
            EngineProperties engineProperties = EngineProperties.INSTANCE;
            VKTexture defaultTexture = textureCache.get(device, engineProperties.getDefaultTexturePath(),
                    VK_FORMAT_R8G8B8A8_SRGB);
            textureList.add(defaultTexture);
        }

        materialsStgBuffer.recordTransferCommand(cmd, materialsBuffer);
        verticesStgBuffer.recordTransferCommand(cmd, verticesBuffer);
        indicesStgBuffer.recordTransferCommand(cmd, indicesBuffer);
        animJointMatricesStgBuffer.recordTransferCommand(cmd, animJointMatricesBuffer);
        animWeightsStgBuffer.recordTransferCommand(cmd, animWeightsBuffer);
        textureList.forEach(t -> t.recordTextureTransition(cmd));
        cmd.endRecording();

        cmd.submitAndWait(device, queue);
        cmd.cleanup();

        verticesStgBuffer.cleanup();
        indicesStgBuffer.cleanup();
        materialsStgBuffer.cleanup();
        animJointMatricesStgBuffer.cleanup();
        animWeightsStgBuffer.cleanup();
        textureList.forEach(VKTexture::cleanupStgBuffer);

        return vulkanModel;
    }
}
