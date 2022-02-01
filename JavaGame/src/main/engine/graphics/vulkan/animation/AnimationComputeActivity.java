package main.engine.graphics.vulkan.animation;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VkCommandBuffer;

import main.engine.EngineProperties;
import main.engine.Scene;
import main.engine.utility.ResourcePaths.Shaders;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.vulkan.*;
import main.engine.graphics.vulkan.Queue;
import main.engine.items.GameItem;

import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.vulkan.VK11.*;

public class AnimationComputeActivity {

    private static final int LOCAL_SIZE_X = 32;

    private final Device device;
    private final MemoryBarrier memoryBarrier;
    private final Queue.ComputeQueue computeQueue;
    // Key is the entity id
    private final Map<String, List<GameItemAnimationBuffer>> gameItemAnimationsBuffers;
    // Key is the model id
    private final Map<String, ModelDescriptorSets> modelDescriptorSetsMap;
    private final Scene scene;

    private CommandBuffer commandBuffer;
    private ComputePipeline computePipeline;
    private DescriptorPool descriptorPool;
    private DescriptorSetLayout[] descriptorSetLayouts;
    private Fence fence;
    private ShaderProgram shaderProgram;
    private DescriptorSetLayout.StorageDescriptorSetLayout storageDescriptorSetLayout;
    private DescriptorSetLayout.UniformDescriptorSetLayout uniformDescriptorSetLayout;

    public AnimationComputeActivity(CommandPool commandPool, PipelineCache pipelineCache, Scene scene) {
        this.scene = scene;
        device = pipelineCache.getDevice();
        computeQueue = new Queue.ComputeQueue(device, 0);
        createDescriptorPool();
        createDescriptorSets();
        createShaders();
        createPipeline(pipelineCache);
        createCommandBuffers(commandPool);
        modelDescriptorSetsMap = new HashMap<>();
        gameItemAnimationsBuffers = new HashMap<>();
        memoryBarrier = new MemoryBarrier(0, VK_ACCESS_SHADER_WRITE_BIT);
    }

    public void cleanup() {
        computePipeline.cleanup();
        shaderProgram.cleanup();
        commandBuffer.cleanup();
        descriptorPool.cleanup();
        storageDescriptorSetLayout.cleanup();
        uniformDescriptorSetLayout.cleanup();
        fence.cleanup();
        for (Map.Entry<String, List<GameItemAnimationBuffer>> entry : gameItemAnimationsBuffers.entrySet()) {
            entry.getValue().forEach(GameItemAnimationBuffer::cleanup);
        }
    }

    private void createCommandBuffers(CommandPool commandPool) {
        commandBuffer = new CommandBuffer(commandPool, true, false);
        fence = new Fence(device, true);
    }

    private void createDescriptorPool() {
        EngineProperties engineProperties = EngineProperties.getInstance();
        int maxStorageBuffers = engineProperties.getMaxStorageBuffers();
        int maxJointsMatricesLists = engineProperties.getMaxJointsMatricesLists();
        List<DescriptorPool.DescriptorTypeCount> descriptorTypeCounts = new ArrayList<>();
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(maxStorageBuffers, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(maxJointsMatricesLists, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER));
        descriptorPool = new DescriptorPool(device, descriptorTypeCounts);
    }

    private void createDescriptorSets() {
        storageDescriptorSetLayout = new DescriptorSetLayout.StorageDescriptorSetLayout(device, 0, VK_SHADER_STAGE_COMPUTE_BIT);
        uniformDescriptorSetLayout = new DescriptorSetLayout.UniformDescriptorSetLayout(device, 0, VK_SHADER_STAGE_COMPUTE_BIT);
        descriptorSetLayouts = new DescriptorSetLayout[]{
                storageDescriptorSetLayout,
                storageDescriptorSetLayout,
                storageDescriptorSetLayout,
                uniformDescriptorSetLayout,
        };
    }

    private void createPipeline(PipelineCache pipelineCache) {
        ComputePipeline.PipeLineCreationInfo pipeLineCreationInfo = new ComputePipeline.PipeLineCreationInfo(shaderProgram,
                descriptorSetLayouts);
        computePipeline = new ComputePipeline(pipelineCache, pipeLineCreationInfo);
    }

    private void createShaders() {
        EngineProperties engineProperties = EngineProperties.getInstance();
        if (engineProperties.isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.ANIMATION_COMPUTE_GLSL, Shaderc.shaderc_compute_shader);
        }
        shaderProgram = new ShaderProgram(device, new ShaderProgram.ShaderModuleData[]
                {
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_COMPUTE_BIT, Shaders.Vulkan.ANIMATION_COMPUTE_SPV),
                });
    }

    public Map<String, List<GameItemAnimationBuffer>> getGameItemAnimationsBuffers() {
        return gameItemAnimationsBuffers;
    }

    public void recordCommandBuffer(List<VulkanModel> vulkanModelList) {
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

            for (VulkanModel vulkanModel : vulkanModelList) {
                String modelId = vulkanModel.getModelId();
                List<GameItem> items = scene.getGameItemsByModelId(modelId);
                if (items == null || items.isEmpty() || !vulkanModel.hasAnimations()) {
                    continue;
                }

                ModelDescriptorSets modelDescriptorSets = modelDescriptorSetsMap.get(modelId);
                int meshCount = 0;
                for (VulkanModel.VulkanMaterial material : vulkanModel.getVulkanMaterialList()) {
                    for (VulkanModel.VulkanMesh mesh : material.vulkanMeshList()) {
                        MeshDescriptorSets meshDescriptorSets = modelDescriptorSets.meshesDescriptorSets.get(meshCount);
                        descriptorSets.put(0, meshDescriptorSets.srcDescriptorSet.getVkDescriptorSet());
                        descriptorSets.put(1, meshDescriptorSets.weightsDescriptorSet.getVkDescriptorSet());

                        for (GameItem item : items) {
                            List<GameItemAnimationBuffer> animationsBuffer = gameItemAnimationsBuffers.get(item.getId());
                            GameItemAnimationBuffer gameItemAnimationBuffer = animationsBuffer.get(meshCount);
                            descriptorSets.put(2, gameItemAnimationBuffer.descriptorSet().getVkDescriptorSet());

                            GameItem.GameItemAnimation itemAnimation = item.getGameItemAnimation();
                            if (!itemAnimation.isStarted() && mesh.animationRendered()) {
                                continue;
                            }
                            DescriptorSet jointMatricesDescriptorSet = modelDescriptorSets.jointMatricesBufferDescriptorSets.
                                    get(itemAnimation.getAnimationIdx()).get(itemAnimation.getCurrentFrame());
                            descriptorSets.put(3, jointMatricesDescriptorSet.getVkDescriptorSet());

                            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_COMPUTE,
                                    computePipeline.getVkPipelineLayout(), 0, descriptorSets, null);

                            vkCmdDispatch(cmdHandle, meshDescriptorSets.groupSize(), 1, 1);
                        }
                        mesh.setAnimationRendered(true);
                        meshCount++;
                    }
                }
            }
        }
        commandBuffer.endRecording();
    }

    public void registerGameItem(VulkanModel vulkanModel, GameItem item) {
        List<GameItemAnimationBuffer> bufferList = new ArrayList<>();
        gameItemAnimationsBuffers.put(item.getId(), bufferList);
        for (VulkanModel.VulkanMaterial material : vulkanModel.getVulkanMaterialList()) {
            for (VulkanModel.VulkanMesh mesh : material.vulkanMeshList()) {
                VulkanBuffer animationBuffer = new VulkanBuffer(device, mesh.verticesBuffer().getRequestedSize(),
                        VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
                DescriptorSet descriptorSet = new DescriptorSet.StorageDescriptorSet(descriptorPool,
                        storageDescriptorSetLayout, animationBuffer, 0);
                bufferList.add(new GameItemAnimationBuffer(animationBuffer, descriptorSet));
            }
        }
    }

    public void registerModels(List<VulkanModel> vulkanModels) {
        for (VulkanModel vulkanModel : vulkanModels) {
            if (!vulkanModel.hasAnimations()) {
                continue;
            }
            String modelId = vulkanModel.getModelId();
            List<List<DescriptorSet>> jointMatricesBufferDescriptorSets = new ArrayList<>();
            for (VulkanModel.VulkanAnimation animation : vulkanModel.getAnimationList()) {
                List<DescriptorSet> animationFrames = new ArrayList<>();
                for (VulkanBuffer jointsMatricesBuffer : animation.frameBufferList()) {
                    animationFrames.add(new DescriptorSet.UniformDescriptorSet(descriptorPool, uniformDescriptorSetLayout,
                            jointsMatricesBuffer, 0));
                }
                jointMatricesBufferDescriptorSets.add(animationFrames);
            }

            List<MeshDescriptorSets> meshDescriptorSetsList = new ArrayList<>();
            for (VulkanModel.VulkanMaterial material : vulkanModel.getVulkanMaterialList()) {
                for (VulkanModel.VulkanMesh mesh : material.vulkanMeshList()) {
                    int vertexSize = 14 * GraphConstants.FLOAT_SIZE_BYTES;
                    int groupSize = (int) Math.ceil((mesh.verticesBuffer().getRequestedSize() / vertexSize) / (float) LOCAL_SIZE_X);
                    MeshDescriptorSets meshDescriptorSets = new MeshDescriptorSets(
                            new DescriptorSet.StorageDescriptorSet(descriptorPool, storageDescriptorSetLayout, mesh.verticesBuffer(), 0),
                            groupSize,
                            new DescriptorSet.StorageDescriptorSet(descriptorPool, storageDescriptorSetLayout, mesh.weightsBuffer(), 0)
                    );
                    meshDescriptorSetsList.add(meshDescriptorSets);
                }
            }

            ModelDescriptorSets modelDescriptorSets = new ModelDescriptorSets(meshDescriptorSetsList, jointMatricesBufferDescriptorSets);
            modelDescriptorSetsMap.put(modelId, modelDescriptorSets);
        }
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

    public record GameItemAnimationBuffer(VulkanBuffer verticesBuffer, DescriptorSet descriptorSet) {
        public void cleanup() {
            verticesBuffer.cleanup();
        }
    }

    record MeshDescriptorSets(DescriptorSet srcDescriptorSet, int groupSize,
                              DescriptorSet weightsDescriptorSet) {
    }

    record ModelDescriptorSets(List<MeshDescriptorSets> meshesDescriptorSets,
                               List<List<DescriptorSet>> jointMatricesBufferDescriptorSets) {
    }
}