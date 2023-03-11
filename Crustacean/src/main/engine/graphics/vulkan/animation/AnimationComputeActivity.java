package main.engine.graphics.vulkan.animation;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import main.engine.items.GameItemAnimation;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.tinylog.Logger;

import main.engine.EngineProperties;
import main.engine.utility.ResourcePaths.Shaders;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.vulkan.*;
import main.engine.graphics.vulkan.Queue;

import java.nio.*;
import java.util.*;

import static org.lwjgl.vulkan.VK11.*;

public class AnimationComputeActivity {

    private static final int LOCAL_SIZE_X = 32;
    private static final int PUSH_CONSTANTS_SIZE = GraphConstants.INT_SIZE_BYTES * 5;

    private final Device device;
    private final MemoryBarrier memoryBarrier;
    private final Queue.ComputeQueue computeQueue;

    private CommandBuffer commandBuffer;
    private ComputePipeline computePipeline;
    private DescriptorPool descriptorPool;
    private DescriptorSetLayout[] descriptorSetLayouts;
    private DescriptorSet.StorageDescriptorSet dstVerticesDescriptorSet;
    private Fence fence;
    private DescriptorSet.StorageDescriptorSet jointMatricesDescriptorSet;
    private ShaderProgram shaderProgram;
    private DescriptorSet.StorageDescriptorSet srcVerticesDescriptorSet;
    private DescriptorSetLayout.StorageDescriptorSetLayout storageDescriptorSetLayout;
    private DescriptorSet.StorageDescriptorSet weightsDescriptorSet;

    public AnimationComputeActivity(CommandPool commandPool, PipelineCache pipelineCache) {
        device = pipelineCache.getDevice();
        computeQueue = new Queue.ComputeQueue(device, 0);
        createDescriptorPool();
        createDescriptorSets();
        createShaders();
        createPipeline(pipelineCache);
        createCommandBuffers(commandPool);
        memoryBarrier = new MemoryBarrier(0, VK_ACCESS_SHADER_WRITE_BIT);
    }

    public void cleanup() {
    	Logger.trace("Cleaning up AnimationComputeActivity");
    	computeQueue.waitIdle();
        computePipeline.cleanup();
        shaderProgram.cleanup();
        commandBuffer.cleanup();
        descriptorPool.cleanup();
        storageDescriptorSetLayout.cleanup();
        fence.cleanup();
        memoryBarrier.cleanup();
    }

    private void createCommandBuffers(CommandPool commandPool) {
        commandBuffer = new CommandBuffer(commandPool, true, false);
        fence = new Fence(device, true);
    }

    private void createDescriptorPool() {
        List<DescriptorPool.DescriptorTypeCount> descriptorTypeCounts = new ArrayList<>();
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(4, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER));
        descriptorPool = new DescriptorPool(device, descriptorTypeCounts);
    }

    private void createDescriptorSets() {
        storageDescriptorSetLayout = new DescriptorSetLayout.StorageDescriptorSetLayout(device, 0, VK_SHADER_STAGE_COMPUTE_BIT, 0);
        descriptorSetLayouts = new DescriptorSetLayout[]{
        		storageDescriptorSetLayout,
                storageDescriptorSetLayout,
                storageDescriptorSetLayout,
                storageDescriptorSetLayout,
        };
    }

    private void createPipeline(PipelineCache pipelineCache) {
        ComputePipeline.PipeLineCreationInfo pipeLineCreationInfo = new ComputePipeline.PipeLineCreationInfo(shaderProgram,
                descriptorSetLayouts, PUSH_CONSTANTS_SIZE);
        computePipeline = new ComputePipeline(pipelineCache, pipeLineCreationInfo);
    }

    private void createShaders() {
        EngineProperties engineProperties = EngineProperties.INSTANCE;
        if (engineProperties.isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.ANIMATION_COMPUTE_GLSL, Shaderc.shaderc_compute_shader);
        }
        shaderProgram = new ShaderProgram(device, new ShaderProgram.ShaderModuleData[]
                {
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_COMPUTE_BIT, Shaders.Vulkan.ANIMATION_COMPUTE_SPV),
                });
    }
    
    public void onAnimatedGameItemsLoaded(GlobalBuffers globalBuffers) {
        if (srcVerticesDescriptorSet == null) {
            srcVerticesDescriptorSet = new DescriptorSet.StorageDescriptorSet(descriptorPool,
                    storageDescriptorSetLayout, globalBuffers.getVerticesBuffer(), 0);
            weightsDescriptorSet = new DescriptorSet.StorageDescriptorSet(descriptorPool,
                    storageDescriptorSetLayout, globalBuffers.getAnimWeightsBuffer(), 0);
            dstVerticesDescriptorSet = new DescriptorSet.StorageDescriptorSet(descriptorPool,
                    storageDescriptorSetLayout, globalBuffers.getAnimVerticesBuffer(), 0);
            jointMatricesDescriptorSet = new DescriptorSet.StorageDescriptorSet(descriptorPool,
                    storageDescriptorSetLayout, globalBuffers.getAnimJointMatricesBuffer(), 0);
        } else {
            srcVerticesDescriptorSet.update(device, globalBuffers.getVerticesBuffer(), 0);
            weightsDescriptorSet.update(device, globalBuffers.getAnimWeightsBuffer(), 0);
            dstVerticesDescriptorSet.update(device, globalBuffers.getAnimVerticesBuffer(), 0);
            jointMatricesDescriptorSet.update(device, globalBuffers.getAnimJointMatricesBuffer(), 0);
        }
    }

    public void recordCommandBuffer(GlobalBuffers globalBuffers, Dominion dominion) {
        fence.fenceWait();
        fence.reset();

        commandBuffer.reset();
        commandBuffer.beginRecording();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBuffer cmdHandle = commandBuffer.getVkCommandBuffer();

            vkCmdPipelineBarrier(cmdHandle, VK_PIPELINE_STAGE_VERTEX_INPUT_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
                    0, memoryBarrier.getVkMemoryBarrier(), null, null);

            vkCmdBindPipeline(cmdHandle, VK_PIPELINE_BIND_POINT_COMPUTE, computePipeline.getVkPipeline());

            LongBuffer descriptorSets = stack.mallocLong(4);

            descriptorSets.put(srcVerticesDescriptorSet.getVkDescriptorSet());
            descriptorSets.put(weightsDescriptorSet.getVkDescriptorSet());
            descriptorSets.put(dstVerticesDescriptorSet.getVkDescriptorSet());
            descriptorSets.put(jointMatricesDescriptorSet.getVkDescriptorSet());
            descriptorSets.flip();
            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_COMPUTE,
                    computePipeline.getVkPipelineLayout(), 0, descriptorSets, null);
            
            List<VulkanAnimModel> vulkanAnimModelList = globalBuffers.getVulkanAnimModelList();
            Results<Results.With1<GameItemAnimation>> results = dominion.findEntitiesWith(GameItemAnimation.class);
            for (Iterator<Results.With1<GameItemAnimation>> itr = results.iterator(); itr.hasNext();) {
                Results.With1<GameItemAnimation> animGameItem = itr.next();
                GameItemAnimation gameItemAnimation = animGameItem.comp();
                VulkanAnimModel vulkanAnimModel = vulkanAnimModelList.get(gameItemAnimation.getAnimModelIdx());
                if (!gameItemAnimation.isStarted() && gameItemAnimation.isLoaded()) {
                    continue;
                }
                gameItemAnimation.setLoaded(true);
                VulkanModel vulkanModel = vulkanAnimModel.vulkanModel;
                int animationIdx = gameItemAnimation.getAnimationIdx();
                int currentFrame = gameItemAnimation.getCurrentFrame();
                int jointMatricesOffset = vulkanModel.vulkanAnimationDataList.get(animationIdx).getVulkanAnimationFrameList().get(currentFrame).jointMatricesOffset();

                for (VulkanAnimModel.VulkanAnimMesh vulkanAnimMesh : vulkanAnimModel.vulkanAnimMeshList) {
                    VulkanModel.VulkanMesh mesh = vulkanAnimMesh.vulkanMesh();

                    int groupSize = (int) Math.ceil((mesh.verticesSize() / (float) InstancedVertexBufferStructure.SIZE_IN_BYTES) / LOCAL_SIZE_X);

                    // Push constants
                    ByteBuffer pushConstantBuffer = stack.malloc(PUSH_CONSTANTS_SIZE);
                    pushConstantBuffer.putInt(mesh.verticesOffset() / GraphConstants.FLOAT_SIZE_BYTES);
                    pushConstantBuffer.putInt(mesh.verticesSize() / GraphConstants.FLOAT_SIZE_BYTES);
                    pushConstantBuffer.putInt(mesh.weightsOffset() / GraphConstants.FLOAT_SIZE_BYTES);
                    pushConstantBuffer.putInt(jointMatricesOffset / GraphConstants.MAT4X4_SIZE_BYTES);
                    pushConstantBuffer.putInt(vulkanAnimMesh.meshOffset() / GraphConstants.FLOAT_SIZE_BYTES);
                    pushConstantBuffer.flip();
                    vkCmdPushConstants(cmdHandle, computePipeline.getVkPipelineLayout(),
                            VK_SHADER_STAGE_COMPUTE_BIT, 0, pushConstantBuffer);

                    vkCmdDispatch(cmdHandle, groupSize, 1, 1);
                }
            }
        }
        commandBuffer.endRecording();
    }

    public void submit() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            computeQueue.submit(stack.pointers(commandBuffer.getVkCommandBuffer()),
                    null,
                    null,
                    null,
                    fence);
        }
    }
}