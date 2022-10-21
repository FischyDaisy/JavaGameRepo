package main.engine.graphics.vulkan.shadows;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

import main.engine.EngineProperties;
import main.engine.Window;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.shadows.CascadeShadow;
import main.engine.graphics.vulkan.*;
import main.engine.graphics.vulkan.animation.AnimationComputeActivity;
import main.engine.graphics.vulkan.geometry.GeometryAttachments;
import main.engine.items.GameItem;
import main.engine.scene.Scene;
import main.engine.utility.ResourcePaths.Shaders;

import java.nio.*;
import java.util.*;

import static org.lwjgl.vulkan.VK11.*;

public class ShadowRenderActivity {

    private final Device device;
    private final Scene scene;
    private final Window window;
    private final ShadowsFrameBuffer shadowsFrameBuffer;

    private List<CascadeShadow> cascadeShadows;
    private DescriptorPool descriptorPool;
    private DescriptorSetLayout[] descriptorSetLayouts;
    private Pipeline pipeLine;
    private DescriptorSet.UniformDescriptorSet[] projMatrixDescriptorSet;
    private ShaderProgram shaderProgram;
    private VulkanBuffer[] shadowsUniforms;
    private SwapChain swapChain;
    private DescriptorSetLayout.UniformDescriptorSetLayout uniformDescriptorSetLayout;

    public ShadowRenderActivity(SwapChain swapChain, PipelineCache pipelineCache, Scene scene, Window window) {
        this.swapChain = swapChain;
        this.scene = scene;
        this.window = window;
        device = swapChain.getDevice();
        int numImages = swapChain.getNumImages();
        shadowsFrameBuffer = new ShadowsFrameBuffer(device);
        createShaders();
        createDescriptorPool(numImages);
        createDescriptorSets(numImages);
        createPipeline(pipelineCache);
        createShadowCascades();
    }

    public void cleanup() {
    	Logger.trace("Cleaning up ShadowRenderActivity");
        pipeLine.cleanup();
        Arrays.stream(shadowsUniforms).forEach(VulkanBuffer::cleanup);
        uniformDescriptorSetLayout.cleanup();
        descriptorPool.cleanup();
        shaderProgram.cleanup();
        shadowsFrameBuffer.cleanup();
    }

    private void createDescriptorPool(int numImages) {
        List<DescriptorPool.DescriptorTypeCount> descriptorTypeCounts = new ArrayList<>();
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(numImages, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER));
        descriptorPool = new DescriptorPool(device, descriptorTypeCounts);
    }

    private void createDescriptorSets(int numImages) {
        uniformDescriptorSetLayout = new DescriptorSetLayout.UniformDescriptorSetLayout(device, 0, VK_SHADER_STAGE_GEOMETRY_BIT, 0);
        descriptorSetLayouts = new DescriptorSetLayout[]{
                uniformDescriptorSetLayout,
        };

        projMatrixDescriptorSet = new DescriptorSet.UniformDescriptorSet[numImages];
        shadowsUniforms = new VulkanBuffer[numImages];
        for (int i = 0; i < numImages; i++) {
            shadowsUniforms[i] = new VulkanBuffer(device, (long)
                    GraphConstants.MAT4X4_SIZE_BYTES * GraphConstants.SHADOW_MAP_CASCADE_COUNT,
                    VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
            projMatrixDescriptorSet[i] = new DescriptorSet.UniformDescriptorSet(descriptorPool, uniformDescriptorSetLayout,
                    shadowsUniforms[i], 0);
        }
    }

    private void createPipeline(PipelineCache pipelineCache) {
        Pipeline.PipeLineCreationInfo pipeLineCreationInfo = new Pipeline.PipeLineCreationInfo(
                shadowsFrameBuffer.getRenderPass().getVkRenderPass(), shaderProgram,
                GeometryAttachments.NUMBER_COLOR_ATTACHMENTS, true, true, 0,
                new InstancedVertexBufferStructure(), descriptorSetLayouts);
        pipeLine = new Pipeline(pipelineCache, pipeLineCreationInfo);
    }

    private void createShaders() {
        EngineProperties engineProperties = EngineProperties.INSTANCE;
        if (engineProperties.isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.SHADOW_VERTEX_GLSL, Shaderc.shaderc_glsl_vertex_shader);
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.SHADOW_GEOMETRY_GLSL, Shaderc.shaderc_glsl_geometry_shader);
        }
        shaderProgram = new ShaderProgram(device, new ShaderProgram.ShaderModuleData[]
                {
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_VERTEX_BIT, Shaders.Vulkan.SHADOW_VERTEX_SPV),
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_GEOMETRY_BIT, Shaders.Vulkan.SHADOW_GEOMETRY_SPV),
                });
    }

    private void createShadowCascades() {
        cascadeShadows = new ArrayList<>();
        for (int i = 0; i < GraphConstants.SHADOW_MAP_CASCADE_COUNT; i++) {
            CascadeShadow cascadeShadow = new CascadeShadow();
            cascadeShadows.add(cascadeShadow);
        }
    }

    public Attachment getDepthAttachment() {
        return shadowsFrameBuffer.getDepthAttachment();
    }

    public List<CascadeShadow> getShadowCascades() {
        return cascadeShadows;
    }

    public void recordCommandBuffer(CommandBuffer commandBuffer, GlobalBuffers globalBuffers, int idx) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
        	VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
            clearValues.apply(0, v -> v.depthStencil().depth(1.0f));

            EngineProperties engineProperties = EngineProperties.INSTANCE;
            int shadowMapSize = engineProperties.getShadowMapSize();
            int width = shadowMapSize;
            int height = shadowMapSize;
            
            VkCommandBuffer cmdHandle = commandBuffer.getVkCommandBuffer();
            
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
            
            FrameBuffer frameBuffer = shadowsFrameBuffer.getFrameBuffer();

            VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                    .renderPass(shadowsFrameBuffer.getRenderPass().getVkRenderPass())
                    .pClearValues(clearValues)
                    .renderArea(a -> a.extent().set(width, height))
                    .framebuffer(frameBuffer.getVkFrameBuffer());
            
            vkCmdBeginRenderPass(cmdHandle, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

            vkCmdBindPipeline(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeLine.getVkPipeline());

            LongBuffer descriptorSets = stack.mallocLong(1)
                    .put(0, projMatrixDescriptorSet[idx].getVkDescriptorSet());
            
            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS,
                    pipeLine.getVkPipelineLayout(), 0, descriptorSets, null);
            
            LongBuffer vertexBuffer = stack.mallocLong(1);
            LongBuffer instanceBuffer = stack.mallocLong(1);
            LongBuffer offsets = stack.mallocLong(1).put(0, 0L);
            
            // Draw commands for non animated models
            if (globalBuffers.getNumIndirectCommands() > 0) {
                vertexBuffer.put(0, globalBuffers.getVerticesBuffer().getBuffer());
                instanceBuffer.put(0, globalBuffers.getInstanceDataBuffers()[idx].getBuffer());

                vkCmdBindVertexBuffers(cmdHandle, 0, vertexBuffer, offsets);
                vkCmdBindVertexBuffers(cmdHandle, 1, instanceBuffer, offsets);
                vkCmdBindIndexBuffer(cmdHandle, globalBuffers.getIndicesBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);

                VulkanBuffer indirectBuffer = globalBuffers.getIndirectBuffer();
                vkCmdDrawIndexedIndirect(cmdHandle, indirectBuffer.getBuffer(), 0, globalBuffers.getNumIndirectCommands(),
                        GlobalBuffers.IND_COMMAND_STRIDE);
            }
            
            if (globalBuffers.getNumAnimIndirectCommands() > 0) {
                // Draw commands for  animated models
                vertexBuffer.put(0, globalBuffers.getAnimVerticesBuffer().getBuffer());
                instanceBuffer.put(0, globalBuffers.getAnimInstanceDataBuffers()[idx].getBuffer());

                vkCmdBindVertexBuffers(cmdHandle, 0, vertexBuffer, offsets);
                vkCmdBindVertexBuffers(cmdHandle, 1, instanceBuffer, offsets);
                vkCmdBindIndexBuffer(cmdHandle, globalBuffers.getIndicesBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
                VulkanBuffer animIndirectBuffer = globalBuffers.getAnimIndirectBuffer();
                vkCmdDrawIndexedIndirect(cmdHandle, animIndirectBuffer.getBuffer(), 0, globalBuffers.getNumAnimIndirectCommands(),
                        GlobalBuffers.IND_COMMAND_STRIDE);
            }

            vkCmdEndRenderPass(cmdHandle);
        }
    }
    
    public void render() {
        if (scene.getSceneLight().isLightChanged() || scene.getCamera().hasMoved()) {
            CascadeShadow.updateCascadeShadows(cascadeShadows, scene, window);
        }

        int idx = swapChain.getCurrentFrame();
        int offset = 0;
        for (CascadeShadow cascadeShadow : cascadeShadows) {
            VulkanUtils.copyMatrixToBuffer(shadowsUniforms[idx], cascadeShadow.getProjViewMatrix(), offset);
            offset += GraphConstants.MAT4X4_SIZE_BYTES;
        }
    }

    public void resize(SwapChain swapChain) {
        this.swapChain = swapChain;
        CascadeShadow.updateCascadeShadows(cascadeShadows, scene, window);
    }
}