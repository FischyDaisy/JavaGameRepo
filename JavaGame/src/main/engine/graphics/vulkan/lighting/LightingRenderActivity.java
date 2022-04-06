package main.engine.graphics.vulkan.lighting;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.*;

import main.engine.EngineProperties;
import main.engine.Scene;
import main.engine.Window;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.lights.Light;
import main.engine.graphics.shadows.CascadeShadow;
import main.engine.graphics.vulkan.*;
import main.engine.graphics.vulkan.Queue;
import main.engine.utility.ResourcePaths.Shaders;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.vulkan.VK11.*;

public class LightingRenderActivity {

	private final Vector4f auxVec;
    private final Device device;
    private final LightSpecConstants lightSpecConstants;
    private final LightingFrameBuffer lightingFrameBuffer;
    private final Scene scene;
    private final Window window;

    private AttachmentsDescriptorSet attachmentsDescriptorSet;
    private AttachmentsLayout attachmentsLayout;
    private CommandBuffer[] commandBuffers;
    private DescriptorPool descriptorPool;
    private DescriptorSetLayout[] descriptorSetLayouts;
    private Fence[] fences;
    private VulkanBuffer[] invMatricesBuffers;
    private DescriptorSet.UniformDescriptorSet[] invMatricesDescriptorSets;
    private VulkanBuffer[] lightsBuffers;
    private DescriptorSet.UniformDescriptorSet[] lightsDescriptorSets;
    private Pipeline pipeline;
    private ShaderProgram shaderProgram;
    private VulkanBuffer[] shadowsMatricesBuffers;
    private DescriptorSet.UniformDescriptorSet[] shadowsMatricesDescriptorSets;
    private SwapChain swapChain;
    private DescriptorSetLayout.UniformDescriptorSetLayout uniformDescriptorSetLayout;

    public LightingRenderActivity(SwapChain swapChain, CommandPool commandPool, PipelineCache pipelineCache,
                                  List<Attachment> attachments, Scene scene, Window window) {
        this.swapChain = swapChain;
        this.scene = scene;
        this.window = window;
        device = swapChain.getDevice();
        auxVec = new Vector4f();
        lightSpecConstants = new LightSpecConstants();

        lightingFrameBuffer = new LightingFrameBuffer(swapChain);
        int numImages = swapChain.getNumImages();
        createShaders();
        createDescriptorPool(attachments);
        createUniforms(numImages);
        createDescriptorSets(attachments, numImages);
        createPipeline(pipelineCache);
        createCommandBuffers(commandPool, numImages);
    }
    
    public CommandBuffer beginRecording(List<CascadeShadow> cascadeShadows) {
        int idx = swapChain.getCurrentFrame();

        Fence fence = fences[idx];
        CommandBuffer commandBuffer = commandBuffers[idx];

        fence.fenceWait();
        fence.reset();

        updateLights(scene.getSceneLight().getAmbientLight(), scene.getSceneLight().getLights(), scene.getCamera().getViewMatrix(), lightsBuffers[idx]);
        updateInvMatrices(invMatricesBuffers[idx]);
        updateCascadeShadowMatrices(cascadeShadows, shadowsMatricesBuffers[idx]);

        commandBuffer.reset();
        commandBuffer.beginRecording();

        return commandBuffer;
    }

    public void cleanup() {
    	uniformDescriptorSetLayout.cleanup();
        attachmentsDescriptorSet.cleanup();
        attachmentsLayout.cleanup();
        descriptorPool.cleanup();
        Arrays.stream(lightsBuffers).forEach(VulkanBuffer::cleanup);
        pipeline.cleanup();
        lightSpecConstants.cleanup();
        Arrays.stream(invMatricesBuffers).forEach(VulkanBuffer::cleanup);
        lightingFrameBuffer.cleanup();
        Arrays.stream(shadowsMatricesBuffers).forEach(VulkanBuffer::cleanup);
        shaderProgram.cleanup();
        Arrays.stream(commandBuffers).forEach(CommandBuffer::cleanup);
        Arrays.stream(fences).forEach(Fence::cleanup);
    }

    private void createCommandBuffers(CommandPool commandPool, int numImages) {
        commandBuffers = new CommandBuffer[numImages];
        fences = new Fence[numImages];

        for (int i = 0; i < numImages; i++) {
            commandBuffers[i] = new CommandBuffer(commandPool, true, false);
            fences[i] = new Fence(device, true);
        }
    }

    private void createDescriptorPool(List<Attachment> attachments) {
    	List<DescriptorPool.DescriptorTypeCount> descriptorTypeCounts = new ArrayList<>();
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(attachments.size(), VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(swapChain.getNumImages() * 3, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER));
        descriptorPool = new DescriptorPool(device, descriptorTypeCounts);
    }

    private void createDescriptorSets(List<Attachment> attachments, int numImages) {
    	attachmentsLayout = new AttachmentsLayout(device, attachments.size());
        uniformDescriptorSetLayout = new DescriptorSetLayout.UniformDescriptorSetLayout(device, 0, VK_SHADER_STAGE_FRAGMENT_BIT);
        descriptorSetLayouts = new DescriptorSetLayout[]{
                attachmentsLayout,
                uniformDescriptorSetLayout,
                uniformDescriptorSetLayout,
                uniformDescriptorSetLayout
        };
        
        attachmentsDescriptorSet = new AttachmentsDescriptorSet(descriptorPool, attachmentsLayout,
                attachments, 0);

        lightsDescriptorSets = new DescriptorSet.UniformDescriptorSet[numImages];
        invMatricesDescriptorSets = new DescriptorSet.UniformDescriptorSet[numImages];
        shadowsMatricesDescriptorSets = new DescriptorSet.UniformDescriptorSet[numImages];
        for (int i = 0; i < numImages; i++) {
            lightsDescriptorSets[i] = new DescriptorSet.UniformDescriptorSet(descriptorPool, uniformDescriptorSetLayout,
                    lightsBuffers[i], 0);
            invMatricesDescriptorSets[i] = new DescriptorSet.UniformDescriptorSet(descriptorPool, uniformDescriptorSetLayout,
                    invMatricesBuffers[i], 0);
            shadowsMatricesDescriptorSets[i] = new DescriptorSet.UniformDescriptorSet(descriptorPool, uniformDescriptorSetLayout,
                    shadowsMatricesBuffers[i], 0);
        }
    }

    private void createPipeline(PipelineCache pipelineCache) {
        Pipeline.PipeLineCreationInfo pipeLineCreationInfo = new Pipeline.PipeLineCreationInfo(
                lightingFrameBuffer.getLightingRenderPass().getVkRenderPass(), shaderProgram, 1, false, false, 0,
                new EmptyVertexBufferStructure(), descriptorSetLayouts);
        pipeline = new Pipeline(pipelineCache, pipeLineCreationInfo);
        pipeLineCreationInfo.cleanup();
    }

    private void createShaders() {
        EngineProperties engineProperties = EngineProperties.getInstance();
        if (engineProperties.isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.LIGHTING_VERTEX_GLSL, Shaderc.shaderc_glsl_vertex_shader);
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.LIGHTING_FRAGMENT_GLSL, Shaderc.shaderc_glsl_fragment_shader);
        }
        shaderProgram = new ShaderProgram(device, new ShaderProgram.ShaderModuleData[]
                {
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_VERTEX_BIT, Shaders.Vulkan.LIGHTING_VERTEX_SPV),
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_FRAGMENT_BIT, Shaders.Vulkan.LIGHTING_FRAGMENT_SPV,
                                lightSpecConstants.getSpecInfo()),
                });
    }
    
    private void createUniforms(int numImages) {
    	lightsBuffers = new VulkanBuffer[numImages];
        invMatricesBuffers = new VulkanBuffer[numImages];
        shadowsMatricesBuffers = new VulkanBuffer[numImages];
        for (int i = 0; i < numImages; i++) {
            lightsBuffers[i] = new VulkanBuffer(device, (long)
                    GraphConstants.INT_SIZE_BYTES * 4 + GraphConstants.VECTOR4F_SIZE_BYTES * 2 * GraphConstants.MAX_LIGHTS +
                    GraphConstants.VECTOR4F_SIZE_BYTES, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);

            invMatricesBuffers[i] = new VulkanBuffer(device, (long)
                    GraphConstants.MAT4X4_SIZE_BYTES * 2, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);

            shadowsMatricesBuffers[i] = new VulkanBuffer(device, (long)
                    (GraphConstants.MAT4X4_SIZE_BYTES + GraphConstants.VECTOR4F_SIZE_BYTES) * GraphConstants.SHADOW_MAP_CASCADE_COUNT,
                    VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
        }
    }
    
    public void endRecording(CommandBuffer commandBuffer) {
        vkCmdEndRenderPass(commandBuffer.getVkCommandBuffer());
        commandBuffer.endRecording();
    }

    public LightingFrameBuffer getLightingFrameBuffer() {
        return lightingFrameBuffer;
    }

    public void recordCommandBuffer(CommandBuffer commandBuffer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int idx = swapChain.getCurrentFrame();
            VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
            int width = swapChainExtent.width();
            int height = swapChainExtent.height();

            FrameBuffer frameBuffer = lightingFrameBuffer.getFrameBuffers()[idx];

            commandBuffer.reset();
            VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
            clearValues.apply(0, v -> v.color().float32(0, 0.0f).float32(1, 0.0f).float32(2, 0.0f).float32(3, 1));

            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset().set(0, 0);
            renderArea.extent().set(width, height);

            VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                    .renderPass(lightingFrameBuffer.getLightingRenderPass().getVkRenderPass())
                    .pClearValues(clearValues)
                    .framebuffer(frameBuffer.getVkFrameBuffer())
                    .renderArea(renderArea);

            commandBuffer.beginRecording();
            VkCommandBuffer cmdHandle = commandBuffer.getVkCommandBuffer();
            vkCmdBeginRenderPass(cmdHandle, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

            vkCmdBindPipeline(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.getVkPipeline());

            VkViewport.Buffer viewport = VkViewport.calloc(1, stack)
                    .x(0)
                    .y(height)
                    .height(-height)
                    .width(width)
                    .minDepth(0.0f)
                    .maxDepth(1.0f);
            vkCmdSetViewport(cmdHandle, 0, viewport);

            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack)
                    .extent(it -> it
                            .width(width)
                            .height(height))
                    .offset(it -> it
                            .x(0)
                            .y(0));
            vkCmdSetScissor(cmdHandle, 0, scissor);

            LongBuffer descriptorSets = stack.mallocLong(4)
                    .put(0, attachmentsDescriptorSet.getVkDescriptorSet())
                    .put(1, lightsDescriptorSets[idx].getVkDescriptorSet())
                    .put(2, invMatricesDescriptorSets[idx].getVkDescriptorSet())
                    .put(3, shadowsMatricesDescriptorSets[idx].getVkDescriptorSet());
            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS,
                    pipeline.getVkPipelineLayout(), 0, descriptorSets, null);

            vkCmdDraw(cmdHandle, 3, 1, 0, 0);
        }
    }

    public void resize(SwapChain swapChain, List<Attachment> attachments) {
    	this.swapChain = swapChain;
        attachmentsDescriptorSet.update(attachments);
        lightingFrameBuffer.resize(swapChain);
    }

    public void submit(Queue queue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int idx = swapChain.getCurrentFrame();
            CommandBuffer commandBuffer = commandBuffers[idx];
            Fence currentFence = fences[idx];
            SwapChain.SyncSemaphores syncSemaphores = swapChain.getSyncSemaphoresList()[idx];
            queue.submit(stack.pointers(commandBuffer.getVkCommandBuffer()),
                    stack.longs(syncSemaphores.geometryCompleteSemaphore().getVkSemaphore()),
                    stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT),
                    stack.longs(syncSemaphores.renderCompleteSemaphore().getVkSemaphore()),
                    currentFence);
        }
    }
    
    private void updateCascadeShadowMatrices(List<CascadeShadow> cascadeShadows, VulkanBuffer shadowsUniformBuffer) {
        long mappedMemory = shadowsUniformBuffer.map();
        ByteBuffer buffer = MemoryUtil.memByteBuffer(mappedMemory, (int) shadowsUniformBuffer.getRequestedSize());
        int offset = 0;
        for (CascadeShadow cascadeShadow : cascadeShadows) {
            cascadeShadow.getProjViewMatrix().get(offset, buffer);
            buffer.putFloat(offset + GraphConstants.MAT4X4_SIZE_BYTES, cascadeShadow.getSplitDistance());
            offset += GraphConstants.MAT4X4_SIZE_BYTES + GraphConstants.VECTOR4F_SIZE_BYTES;
        }
        shadowsUniformBuffer.unMap();
    }

    private void updateInvMatrices(VulkanBuffer invMatricesBuffer) {
        Matrix4f invProj = new Matrix4f(window.getProjectionMatrix()).invert();
        Matrix4f invView = new Matrix4f(scene.getCamera().getViewMatrix()).invert();
        VulkanUtils.copyMatrixToBuffer(invMatricesBuffer, invProj, 0);
        VulkanUtils.copyMatrixToBuffer(invMatricesBuffer, invView, GraphConstants.MAT4X4_SIZE_BYTES);
    }

    private void updateLights(Vector4f ambientLight, Light[] lights, Matrix4f viewMatrix,
                              VulkanBuffer lightsBuffer) {
        long mappedMemory = lightsBuffer.map();
        ByteBuffer uniformBuffer = MemoryUtil.memByteBuffer(mappedMemory, (int) lightsBuffer.getRequestedSize());

        ambientLight.get(0, uniformBuffer);
        int offset = GraphConstants.VECTOR4F_SIZE_BYTES;
        int numLights = lights != null ? lights.length : 0;
        uniformBuffer.putInt(offset, numLights);
        offset += GraphConstants.VECTOR4F_SIZE_BYTES;
        for (int i = 0; i < numLights; i++) {
            Light light = lights[i];
            auxVec.set(light.getPosition());
            auxVec.mul(viewMatrix);
            auxVec.w = light.getPosition().w;
            auxVec.get(offset, uniformBuffer);
            offset += GraphConstants.VECTOR4F_SIZE_BYTES;
            light.getColor().get(offset, uniformBuffer);
            offset += GraphConstants.VECTOR4F_SIZE_BYTES;
        }

        lightsBuffer.unMap();
    }
}