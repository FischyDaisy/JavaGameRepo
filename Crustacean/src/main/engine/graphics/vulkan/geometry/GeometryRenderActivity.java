package main.engine.graphics.vulkan.geometry;

import main.engine.graphics.camera.Camera;
import org.lwjgl.system.*;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

import main.engine.EngineProperties;
import main.engine.Window;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.vulkan.*;
import main.engine.utility.ResourcePaths.Shaders;

import java.nio.*;
import java.util.*;

import static org.lwjgl.vulkan.VK11.*;

public class GeometryRenderActivity {

    private final Device device;
    private final MemoryBarrier memoryBarrier;
    private final GeometryFrameBuffer geometryFrameBuffer;
    private final GeometrySpecConstants geometrySpecConstants;
    private final PipelineCache pipelineCache;
    private final Window window;

    private DescriptorPool descriptorPool;
    private DescriptorSetLayout[] geometryDescriptorSetLayouts;
    private DescriptorSet.StorageDescriptorSet materialsDescriptorSet;
    private Pipeline pipeLine;
    private DescriptorSet.UniformDescriptorSet projMatrixDescriptorSet;
    private VulkanBuffer projMatrixUniform;
    private ShaderProgram shaderProgram;
    private DescriptorSetLayout.StorageDescriptorSetLayout storageDescriptorSetLayout;
    private SwapChain swapChain;
    private TextureDescriptorSet textureDescriptorSet;
    private DescriptorSetLayout.SamplerDescriptorSetLayout textureDescriptorSetLayout;
    private TextureSampler textureSampler;
    private DescriptorSetLayout.UniformDescriptorSetLayout uniformDescriptorSetLayout;
    private VulkanBuffer[] viewMatricesBuffer;
    private DescriptorSet.UniformDescriptorSet[] viewMatricesDescriptorSets;

    public GeometryRenderActivity(SwapChain swapChain, PipelineCache pipelineCache, Window window, GlobalBuffers globalBuffers) {
        this.swapChain = swapChain;
        this.pipelineCache = pipelineCache;
        this.window = window;
        device = swapChain.getDevice();
        geometrySpecConstants = new GeometrySpecConstants();
        
        geometryFrameBuffer = new GeometryFrameBuffer(swapChain);
        int numImages = swapChain.getNumImages();
        createShaders();
        createDescriptorPool();
        createDescriptorSets(numImages, globalBuffers);
        createPipeline();
        VulkanUtils.copyMatrixToBuffer(projMatrixUniform, window.getProjectionMatrix());
        memoryBarrier = new MemoryBarrier(VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
    }

    public void cleanup() {
    	Logger.trace("Cleaning up GeometryRenderActivity");
        pipeLine.cleanup();
        Arrays.stream(viewMatricesBuffer).forEach(VulkanBuffer::cleanup);
        projMatrixUniform.cleanup();
        textureSampler.cleanup();
        textureDescriptorSetLayout.cleanup();
        uniformDescriptorSetLayout.cleanup();
        storageDescriptorSetLayout.cleanup();
        descriptorPool.cleanup();
        shaderProgram.cleanup();
        geometryFrameBuffer.cleanup();
        memoryBarrier.cleanup();
    }

    private void createDescriptorPool() {
        EngineProperties engineProps = EngineProperties.INSTANCE;
        List<DescriptorPool.DescriptorTypeCount> descriptorTypeCounts = new ArrayList<>();
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(swapChain.getNumImages() + 1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(engineProps.getMaxMaterials() * 3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(1, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER));
        descriptorPool = new DescriptorPool(device, descriptorTypeCounts);
    }

    private void createDescriptorSets(int numImages, GlobalBuffers globalBuffers) {
    	EngineProperties engineProps = EngineProperties.INSTANCE;
        uniformDescriptorSetLayout = new DescriptorSetLayout.UniformDescriptorSetLayout(device, 0, VK_SHADER_STAGE_VERTEX_BIT, 0);
        textureDescriptorSetLayout = new DescriptorSetLayout.SamplerDescriptorSetLayout(device, engineProps.getMaxTextures(), 0, VK_SHADER_STAGE_FRAGMENT_BIT, 0);
        storageDescriptorSetLayout = new DescriptorSetLayout.StorageDescriptorSetLayout(device, 0, VK_SHADER_STAGE_FRAGMENT_BIT, 0);
        geometryDescriptorSetLayouts = new DescriptorSetLayout[]{
        		uniformDescriptorSetLayout,
                uniformDescriptorSetLayout,
                storageDescriptorSetLayout,
                textureDescriptorSetLayout
        };
        
        textureSampler = new TextureSampler(device, 1);
        projMatrixUniform = new VulkanBuffer(device, GraphConstants.MAT4X4_SIZE_BYTES, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
        projMatrixDescriptorSet = new DescriptorSet.UniformDescriptorSet(descriptorPool, uniformDescriptorSetLayout, projMatrixUniform, 0);
        materialsDescriptorSet = new DescriptorSet.StorageDescriptorSet(descriptorPool, storageDescriptorSetLayout,
                globalBuffers.getMaterialsBuffer(), 0);

        viewMatricesDescriptorSets = new DescriptorSet.UniformDescriptorSet[numImages];
        viewMatricesBuffer = new VulkanBuffer[numImages];
        for (int i = 0; i < numImages; i++) {
            viewMatricesBuffer[i] = new VulkanBuffer(device, GraphConstants.MAT4X4_SIZE_BYTES, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
            viewMatricesDescriptorSets[i] = new DescriptorSet.UniformDescriptorSet(descriptorPool, uniformDescriptorSetLayout,
                    viewMatricesBuffer[i], 0);
        }
    }

    private void createPipeline() {
        Pipeline.PipeLineCreationInfo pipeLineCreationInfo = new Pipeline.PipeLineCreationInfo(
                geometryFrameBuffer.getRenderPass().getVkRenderPass(), shaderProgram, GeometryAttachments.NUMBER_COLOR_ATTACHMENTS,
                true, true, 0,
                new InstancedVertexBufferStructure(), geometryDescriptorSetLayouts);
        pipeLine = new Pipeline(pipelineCache, pipeLineCreationInfo);
        pipeLineCreationInfo.cleanup();
    }

    private void createShaders() {
        EngineProperties engineProperties = EngineProperties.INSTANCE;
        if (engineProperties.isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.GEOMETRY_VERTEX_GLSL, Shaderc.shaderc_glsl_vertex_shader);
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.GEOMETRY_FRAGMENT_GLSL, Shaderc.shaderc_glsl_fragment_shader);
        }
        shaderProgram = new ShaderProgram(device, new ShaderProgram.ShaderModuleData[]
                {
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_VERTEX_BIT, Shaders.Vulkan.GEOMETRY_VERTEX_SPV),
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_FRAGMENT_BIT, Shaders.Vulkan.GEOMETRY_FRAGMENT_SPV,
                        		geometrySpecConstants.getSpecInfo()),
                });
    }
    
    public void endRenderPass(CommandBuffer commandBuffer) {
    	vkCmdEndRenderPass(commandBuffer.getVkCommandBuffer());
    }

    public List<Attachment> getAttachments() {
        return geometryFrameBuffer.geometryAttachments().getAttachments();
    }
    
    public GeometryFrameBuffer getFrameBuffer() {
    	return geometryFrameBuffer;
    }
    
    public void loadModels(VKTextureCache textureCache) {
        device.waitIdle();
        // Size of the descriptor is setup in the layout, we need to fill up the texture list
        // up to the number defined in the layout (reusing last texture)
        List<VKTexture> textureCacheList = textureCache.getAsList();
        int textureCacheSize = textureCacheList.size();
        List<VKTexture> textureList = new ArrayList<>(textureCacheList);
        EngineProperties engineProperties = EngineProperties.INSTANCE;
        int maxTextures = engineProperties.getMaxTextures();
        for (int i = 0; i < maxTextures - textureCacheSize; i++) {
            textureList.add(textureCacheList.get(textureCacheSize - 1));
        }
        if (textureDescriptorSet == null) {
            textureDescriptorSet = new TextureDescriptorSet(descriptorPool, textureDescriptorSetLayout, textureList,
                    textureSampler, 0);
        } else {
            textureDescriptorSet.update(device, textureList, textureSampler, 0);
        }
    }

    public void recordCommandBuffer(CommandBuffer commandBuffer, GlobalBuffers globalBuffers, int idx) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
            int width = swapChainExtent.width();
            int height = swapChainExtent.height();

            FrameBuffer frameBuffer = geometryFrameBuffer.getFrameBuffer();
            List<Attachment> attachments = geometryFrameBuffer.geometryAttachments().getAttachments();
            VkClearValue.Buffer clearValues = VkClearValue.calloc(attachments.size(), stack);
            for (Attachment attachment : attachments) {
                if (attachment.isDepthAttachment()) {
                    clearValues.apply(v -> v.depthStencil().depth(1.0f));
                } else {
                    clearValues.apply(v -> v.color().float32(0, 0.0f).float32(1, 0.0f).float32(2, 0.0f).float32(3, 1));
                }
            }
            clearValues.flip();

            VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                    .renderPass(geometryFrameBuffer.getRenderPass().getVkRenderPass())
                    .pClearValues(clearValues)
                    .renderArea(a -> a.extent().set(width, height))
                    .framebuffer(frameBuffer.getVkFrameBuffer());

            VkCommandBuffer cmdHandle = commandBuffer.getVkCommandBuffer();
            
            vkCmdPipelineBarrier(cmdHandle, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_VERTEX_INPUT_BIT,
                    0, memoryBarrier.getVkMemoryBarrier(), null, null);
            
            vkCmdBeginRenderPass(cmdHandle, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

            vkCmdBindPipeline(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeLine.getVkPipeline());

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
            		.put(0, projMatrixDescriptorSet.getVkDescriptorSet())
                    .put(1, viewMatricesDescriptorSets[idx].getVkDescriptorSet())
                    .put(2, materialsDescriptorSet.getVkDescriptorSet())
                    .put(3, textureDescriptorSet.getVkDescriptorSet());

            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS,
                    pipeLine.getVkPipelineLayout(), 0, descriptorSets, null);
            
            LongBuffer vertexBuffer = stack.mallocLong(1);
            LongBuffer instanceBuffer = stack.mallocLong(1);
            LongBuffer offsets = stack.mallocLong(1).put(0, 0L);
            
            // Draw commands for non animated entities
            if (globalBuffers.getNumIndirectCommands() > 0) {
                vertexBuffer.put(0, globalBuffers.getVerticesBuffer().getBuffer());
                instanceBuffer.put(0, globalBuffers.getInstanceDataBuffers()[idx].getBuffer());

                vkCmdBindVertexBuffers(cmdHandle, 0, vertexBuffer, offsets);
                vkCmdBindVertexBuffers(cmdHandle, 1, instanceBuffer, offsets);
                vkCmdBindIndexBuffer(cmdHandle, globalBuffers.getIndicesBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
                VulkanBuffer staticIndirectBuffer = globalBuffers.getIndirectBuffer();
                vkCmdDrawIndexedIndirect(cmdHandle, staticIndirectBuffer.getBuffer(), 0, globalBuffers.getNumIndirectCommands(),
                        GlobalBuffers.IND_COMMAND_STRIDE);
            }

            // Draw commands for animated entities
            if (globalBuffers.getNumAnimIndirectCommands() > 0) {
                vertexBuffer.put(0, globalBuffers.getAnimVerticesBuffer().getBuffer());
                instanceBuffer.put(0, globalBuffers.getAnimInstanceDataBuffers()[idx].getBuffer());

                vkCmdBindVertexBuffers(cmdHandle, 0, vertexBuffer, offsets);
                vkCmdBindVertexBuffers(cmdHandle, 1, instanceBuffer, offsets);
                vkCmdBindIndexBuffer(cmdHandle, globalBuffers.getIndicesBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
                VulkanBuffer animIndirectBuffer = globalBuffers.getAnimIndirectBuffer();
                vkCmdDrawIndexedIndirect(cmdHandle, animIndirectBuffer.getBuffer(), 0, globalBuffers.getNumAnimIndirectCommands(),
                        GlobalBuffers.IND_COMMAND_STRIDE);
            }
        }
    }
    
    public void render(Camera camera) {
        int idx = swapChain.getCurrentFrame();
        VulkanUtils.copyMatrixToBuffer(viewMatricesBuffer[idx], camera.getViewMatrix());
    }

    public void resize(SwapChain swapChain) {
        VulkanUtils.copyMatrixToBuffer(projMatrixUniform, window.getProjectionMatrix());
        this.swapChain = swapChain;
        geometryFrameBuffer.resize(swapChain);
    }
}