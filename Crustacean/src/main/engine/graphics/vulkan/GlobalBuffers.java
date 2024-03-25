package main.engine.graphics.vulkan;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import main.engine.enginelayouts.Matrix4fLayout;
import main.engine.utility.BufferUtils;
import org.lwjgl.system.*;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

import main.engine.EngineProperties;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.animation.VulkanAnimModel;
import main.engine.items.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.vulkan.VK11.*;

public class GlobalBuffers {
	public static final int IND_COMMAND_STRIDE = VkDrawIndexedIndirectCommand.SIZEOF;
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
    private VulkanBuffer animIndirectBuffer;
    private VulkanBuffer[] animInstanceDataBuffers;
    private VulkanBuffer animVerticesBuffer;
    private VulkanBuffer indirectBuffer;
    private VulkanBuffer[] instanceDataBuffers;
    private Arena instanceDataBufferArena;
    private MemorySegment[] instanceDataSegments;
    private int numAnimIndirectCommands;
    private int numIndirectCommands;
    private long jointMatricesBufferOffset;
    private long weightsBufferOffset;
    private long indexBufferOffset;
    private long materialBufferOffset;
    private long vertexBufferOffset;
    private List<VulkanAnimModel> vulkanAnimModelList;
    
    public GlobalBuffers(Device device) {
        Logger.debug("Creating global buffers");
        EngineProperties engProps = EngineProperties.INSTANCE;
        vertexBufferSize = engProps.getMaxVerticesBuffer();
        vertexBufferOffset = 0;
        verticesBuffer = new VulkanBuffer(device, vertexBufferSize, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT |
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        indexBufferSize = engProps.getMaxIndicesBuffer();
        indexBufferOffset = 0;
        indicesBuffer = new VulkanBuffer(device, indexBufferSize, VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        int maxMaterials = engProps.getMaxMaterials();
        materialBufferSize = (long) maxMaterials * GraphConstants.VECTOR4F_SIZE_BYTES * 9;
        materialBufferOffset = 0;
        materialsBuffer = new VulkanBuffer(device, materialBufferSize, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        jointMatricesBufferSize = engProps.getMaxJointMatricesBuffer();
        jointMatricesBufferOffset = 0;
        animJointMatricesBuffer = new VulkanBuffer(device, jointMatricesBufferSize, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        weightsBufferSize = engProps.getMaxAnimWeightsBuffer();
        weightsBufferOffset = 0;
        animWeightsBuffer = new VulkanBuffer(device, weightsBufferSize, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        animVerticesBuffer = new VulkanBuffer(device, engProps.getMaxAnimVerticesBuffer(), VK_BUFFER_USAGE_VERTEX_BUFFER_BIT |
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        numIndirectCommands = 0;
        instanceDataBufferArena = Arena.ofConfined();
    }

    public void cleanup() {
        Logger.debug("Destroying global buffers");
        verticesBuffer.cleanup();
        indicesBuffer.cleanup();
        if (indirectBuffer != null) {
            indirectBuffer.cleanup();
        }
        if (animVerticesBuffer != null) {
            animVerticesBuffer.cleanup();
        }
        if (animIndirectBuffer != null) {
            animIndirectBuffer.cleanup();
        }
        materialsBuffer.cleanup();
        animJointMatricesBuffer.cleanup();
        animWeightsBuffer.cleanup();
        if (instanceDataBuffers != null) {
            Arrays.stream(instanceDataBuffers).forEach(VulkanBuffer::cleanup);
        }
        if (animInstanceDataBuffers != null) {
            Arrays.stream(animInstanceDataBuffers).forEach(VulkanBuffer::cleanup);
        }
        if (instanceDataBufferArena.isCloseableBy(Thread.currentThread())) {
            instanceDataBufferArena.close();
        }
    }

    public VulkanBuffer getAnimIndirectBuffer() {
        return animIndirectBuffer;
    }

    public VulkanBuffer[] getAnimInstanceDataBuffers() {
        return animInstanceDataBuffers;
    }

    public VulkanBuffer getAnimJointMatricesBuffer() {
        return animJointMatricesBuffer;
    }

    public VulkanBuffer getAnimVerticesBuffer() {
        return animVerticesBuffer;
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

    public int getNumAnimIndirectCommands() {
        return numAnimIndirectCommands;
    }

    public int getNumIndirectCommands() {
        return numIndirectCommands;
    }

    public VulkanBuffer getVerticesBuffer() {
        return verticesBuffer;
    }

    public List<VulkanAnimModel> getVulkanAnimModelList() {
        return vulkanAnimModelList;
    }

    private void loadAnimGameItems(Dominion dominion, CommandPool commandPool,
                                   Queue queue, int numSwapChainImages) {
    	vulkanAnimModelList = new ArrayList<>();
        numAnimIndirectCommands = 0;
        int animModelListOffset = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Device device = commandPool.getDevice();
            CommandBuffer cmd = new CommandBuffer(commandPool, true, true);

            int bufferOffset = 0;
            int firstInstance = 0;
            List<VkDrawIndexedIndirectCommand> indexedIndirectCommandList = new ArrayList<>();
            Results<Results.With2<GameItem, GameItemAnimation>> gameItems = dominion.findEntitiesWith(GameItem.class, GameItemAnimation.class);
            Results<Results.With1<VulkanModel>> modelResults = dominion.findEntitiesWith(VulkanModel.class);
            for (Results.With1<VulkanModel> modelComp : modelResults) {
                VulkanModel vulkanModel = modelComp.comp();
                for (Results.With2<GameItem, GameItemAnimation> itemComp : gameItems) {
                    if (!itemComp.comp1().modelId().equals(vulkanModel.modelId)) {
                        continue;
                    }
                    VulkanAnimModel vulkanAnimModel = new VulkanAnimModel(itemComp.comp1(), vulkanModel);
                    vulkanAnimModelList.add(vulkanAnimModel);
                    itemComp.comp2().setAnimModelIdx(animModelListOffset);
                    animModelListOffset++;
                    List<VulkanAnimModel.VulkanAnimMesh> vulkanAnimMeshList = vulkanAnimModel.vulkanAnimMeshList;
                    for (VulkanModel.VulkanMesh vulkanMesh : vulkanModel.vulkanMeshList) {
                        VkDrawIndexedIndirectCommand indexedIndirectCommand = VkDrawIndexedIndirectCommand.calloc(stack);
                        indexedIndirectCommand.indexCount(vulkanMesh.numIndices());
                        indexedIndirectCommand.firstIndex(vulkanMesh.indicesOffset() / GraphConstants.INT_SIZE_BYTES);
                        indexedIndirectCommand.instanceCount(1);
                        indexedIndirectCommand.vertexOffset(bufferOffset / VertexBufferStructure.SIZE_IN_BYTES);
                        indexedIndirectCommand.firstInstance(firstInstance);
                        indexedIndirectCommandList.add(indexedIndirectCommand);

                        vulkanAnimMeshList.add(new VulkanAnimModel.VulkanAnimMesh(bufferOffset, vulkanMesh));
                        bufferOffset += vulkanMesh.verticesSize();
                        firstInstance++;
                    }
                }
            }
            numAnimIndirectCommands = indexedIndirectCommandList.size();
            if (numAnimIndirectCommands > 0) {
                cmd.beginRecording();

                StagingBuffer indirectStgBuffer = new StagingBuffer(device, (long) IND_COMMAND_STRIDE * numAnimIndirectCommands);
                if (animIndirectBuffer != null) {
                    animIndirectBuffer.cleanup();
                }
                animIndirectBuffer = new VulkanBuffer(device, indirectStgBuffer.getRequestedSize(),
                        VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                        VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
                ByteBuffer dataBuffer = indirectStgBuffer.getDataBuffer();
                VkDrawIndexedIndirectCommand.Buffer indCommandBuffer = new VkDrawIndexedIndirectCommand.Buffer(dataBuffer);

                indexedIndirectCommandList.forEach(indCommandBuffer::put);

                if (animInstanceDataBuffers != null) {
                    Arrays.stream(animInstanceDataBuffers).forEach(VulkanBuffer::cleanup);
                }
                animInstanceDataBuffers = new VulkanBuffer[numSwapChainImages];
                for (int i = 0; i < numSwapChainImages; i++) {
                    animInstanceDataBuffers[i] = new VulkanBuffer(device,
                            (long) numAnimIndirectCommands * (GraphConstants.MAT4X4_SIZE_BYTES + GraphConstants.INT_SIZE_BYTES),
                            VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
                }

                indirectStgBuffer.recordTransferCommand(cmd, animIndirectBuffer);

                cmd.endRecording();
                cmd.submitAndWait(device, queue);
                cmd.cleanup();
                indirectStgBuffer.cleanup();
            }
        }
    }

    public void loadGameItems(Dominion dominion, CommandPool commandPool,
                             Queue queue, int numSwapChainImages) {
        loadStaticGameItems(dominion, commandPool, queue, numSwapChainImages);
        loadAnimGameItems(dominion, commandPool, queue, numSwapChainImages);
    }

    public void loadInstanceData(Dominion dominion, int currentSwapChainIdx) throws Throwable {
        Results<Results.With1<GameItem>> staticGameItems = dominion.findEntitiesWith(GameItem.class).without(GameItemAnimation.class);
        Results<Results.With1<GameItem>> animGameItems = dominion.findEntitiesWith(GameItem.class).withAlso(GameItemAnimation.class);
        Results<Results.With1<VulkanModel>> vulkanModels = dominion.findEntitiesWith(VulkanModel.class);
        loadInstanceData(staticGameItems, vulkanModels, instanceDataBuffers[currentSwapChainIdx]);
        loadInstanceData(animGameItems, vulkanModels, animInstanceDataBuffers[currentSwapChainIdx]);
    }

    private void loadInstanceData(Results<Results.With1<GameItem>> gameItems, Results<Results.With1<VulkanModel>> vulkanModels, VulkanBuffer instanceBuffer) throws Throwable {
        if (instanceBuffer == null) {
            return;
        }
        long mappedMemory = instanceBuffer.map();
        MemorySegment dataBuffer = MemorySegment.ofAddress(mappedMemory).reinterpret(instanceBuffer.getRequestedSize());
        instanceBuffer.map();
        int pos = 0;
        for (Results.With1<VulkanModel> modelComp : vulkanModels) {
            VulkanModel vulkanModel = modelComp.comp();
            for (VulkanModel.VulkanMesh vulkanMesh : vulkanModel.vulkanMeshList) {
                for (Results.With1<GameItem> itemComp : gameItems) {
                    GameItem gameItem = itemComp.comp();
                    if (!gameItem.modelId().equals(vulkanModel.modelId)) {
                        continue;
                    }
                    Matrix4fLayout.setMatrix(dataBuffer, pos, gameItem.getModelMatrix());
                    pos += GraphConstants.MAT4X4_SIZE_BYTES;
                    dataBuffer.set(ValueLayout.JAVA_INT, pos, vulkanMesh.globalMaterialIdx());
                    pos += GraphConstants.INT_SIZE_BYTES;
                }
            }
        }
        instanceBuffer.unMap();
    }

    public void loadModels(Dominion dominion, VKTextureCache textureCache, CommandPool
            commandPool, Queue queue) {
        resetModelBuffers();
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
        long materialsOffset;
        List<ModelData.Material> defaultMaterialList = Collections.singletonList(new ModelData.Material());
        materialsOffset = BufferUtils.loadMaterials(device, textureCache, materialsStgBuffer, defaultMaterialList, new ArrayList<>(), textureList, 0);

        long jointsOffset = 0;
        BufferUtils.BufferOffsets offsets = new BufferUtils.BufferOffsets(0L, 0L, 0L);
        Results<Results.With1<ModelData>> results = dominion.findEntitiesWith(ModelData.class);
        Iterator<Results.With1<ModelData>> itr = results.iterator();
        while (itr.hasNext()) {
            Results.With1<ModelData> result = itr.next();
            ModelData modelData = result.comp();
            VulkanModel vulkanModel = new VulkanModel(modelData.getModelId());
            dominion.createEntity(vulkanModel);

            List<VulkanModel.VulkanMaterial> vulkanMaterialList = new ArrayList<>();
            materialsOffset = BufferUtils.loadMaterials(device, textureCache, materialsStgBuffer,
                    modelData.getMaterialList(), vulkanMaterialList, textureList, materialsOffset);
            offsets = BufferUtils.loadMeshes(verticesStgBuffer, indicesStgBuffer, animWeightsStgBuffer, modelData, vulkanModel, vulkanMaterialList, offsets);
            jointsOffset = BufferUtils.loadAnimationData(modelData, vulkanModel, animJointMatricesStgBuffer, jointsOffset);
            dominion.deleteEntity(result.entity());
        }

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
    }

    private void loadStaticGameItems(Dominion dominion, CommandPool commandPool,
                                    Queue queue, int numSwapChainImages) {
        numIndirectCommands = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Device device = commandPool.getDevice();
            CommandBuffer cmd = new CommandBuffer(commandPool, true, true);

            List<VkDrawIndexedIndirectCommand> indexedIndirectCommandList = new ArrayList<>();
            int numInstances = 0;
            int firstInstance = 0;
            Results<Results.With1<GameItem>> gameItems = dominion.findEntitiesWith(GameItem.class).without(GameItemAnimation.class);
            Results<Results.With1<VulkanModel>> modelResults = dominion.findEntitiesWith(VulkanModel.class);
            for (Results.With1<VulkanModel> modelComp : modelResults) {
                VulkanModel vulkanModel = modelComp.comp();
                List<Results.With1<GameItem>> results = gameItems.stream()
                        .filter(r -> r.comp().modelId().equals(vulkanModel.modelId))
                        .toList();
                if (results.isEmpty() || vulkanModel.hasAnimations()) {
                    continue;
                }
                for (VulkanModel.VulkanMesh vulkanMesh : vulkanModel.vulkanMeshList) {
                    VkDrawIndexedIndirectCommand indexedIndirectCommand = VkDrawIndexedIndirectCommand.calloc(stack);
                    indexedIndirectCommand.indexCount(vulkanMesh.numIndices());
                    indexedIndirectCommand.firstIndex(vulkanMesh.indicesOffset() / GraphConstants.INT_SIZE_BYTES);
                    indexedIndirectCommand.instanceCount(results.size());
                    indexedIndirectCommand.vertexOffset(vulkanMesh.verticesOffset() / VertexBufferStructure.SIZE_IN_BYTES);
                    indexedIndirectCommand.firstInstance(firstInstance);
                    indexedIndirectCommandList.add(indexedIndirectCommand);

                    numIndirectCommands++;
                    firstInstance += results.size();
                    numInstances += results.size();
                }
            }
            if (numIndirectCommands > 0) {
                cmd.beginRecording();

                StagingBuffer indirectStgBuffer = new StagingBuffer(device, (long) IND_COMMAND_STRIDE * numIndirectCommands);
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
                    instanceDataBuffers[i] = new VulkanBuffer(device, (long) numInstances * (GraphConstants.MAT4X4_SIZE_BYTES + GraphConstants.INT_SIZE_BYTES),
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

    public void resetModelBuffers() {
        jointMatricesBufferOffset = 0;
        weightsBufferOffset = 0;
        materialBufferOffset = 0;
        indexBufferOffset = 0;
        vertexBufferOffset = 0;
    }
}