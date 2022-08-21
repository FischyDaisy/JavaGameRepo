package main.engine.graphics.vulkan.skybox;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexedIndirect;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;
import org.tinylog.Logger;

import main.engine.*;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.vulkan.*;
import main.engine.graphics.vulkan.animation.AnimationComputeActivity;
import main.engine.graphics.vulkan.geometry.*;
import main.engine.items.GameItem;
import main.engine.items.SkyBox;
import main.engine.scene.Scene;
import main.engine.utility.ResourcePaths.Shaders;

public class SkyboxRenderActivity {
	
	private final Device device;
	private final SkyboxSpecConstants skyboxSpecConstants;
    private final Scene scene;
    private final Window window;
    
    private DescriptorPool descriptorPool;
    private DescriptorSetLayout[] skyboxDescriptorSetLayouts;
    private DescriptorSet.StorageDescriptorSet materialsDescriptorSet;
    private Pipeline pipeline;
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
	
	public SkyboxRenderActivity(SwapChain swapChain, PipelineCache pipelineCache, Scene scene, Window window, long vkRenderPass, GlobalBuffers globalBuffers) {
		this.scene = scene;
		this.window = window;
		device = swapChain.getDevice();
		this.swapChain = swapChain;
		skyboxSpecConstants = new SkyboxSpecConstants();
		
		int numImages = swapChain.getNumImages();
		
		createShaders();
		createDescriptorPool();
		createDescriptorSets(numImages, globalBuffers);
		createPipeline(pipelineCache, vkRenderPass);
		VulkanUtils.copyMatrixToBuffer(projMatrixUniform, window.getProjectionMatrix());
	}
	
	public void cleanup() {
		Logger.trace("Cleaning up SkyboxRenderActivity");
		pipeline.cleanup();
        Arrays.stream(viewMatricesBuffer).forEach(VulkanBuffer::cleanup);
        projMatrixUniform.cleanup();
        textureSampler.cleanup();
        textureDescriptorSetLayout.cleanup();
        uniformDescriptorSetLayout.cleanup();
        storageDescriptorSetLayout.cleanup();
        descriptorPool.cleanup();
        shaderProgram.cleanup();
	}
	
	public void createDescriptorPool() {
		EngineProperties engineProperties = EngineProperties.INSTANCE;
        List<DescriptorPool.DescriptorTypeCount> descriptorTypeCounts = new ArrayList<>();
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(swapChain.getNumImages() + 1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(engineProperties.getMaxSkyboxMaterials() * 3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(1, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER));
        descriptorPool = new DescriptorPool(device, descriptorTypeCounts);
	}
	
	public void createDescriptorSets(int numImages, GlobalBuffers globalBuffers) {
		EngineProperties engineProps = EngineProperties.INSTANCE;
		uniformDescriptorSetLayout = new DescriptorSetLayout.UniformDescriptorSetLayout(device, 0, VK_SHADER_STAGE_VERTEX_BIT, 0);
        textureDescriptorSetLayout = new DescriptorSetLayout.SamplerDescriptorSetLayout(device, engineProps.getMaxSkyboxTextures(), 0, VK_SHADER_STAGE_FRAGMENT_BIT, 0);
        storageDescriptorSetLayout = new DescriptorSetLayout.StorageDescriptorSetLayout(device, 0, VK_SHADER_STAGE_FRAGMENT_BIT, 0);
        skyboxDescriptorSetLayouts = new DescriptorSetLayout[]{
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
	
	public void createPipeline(PipelineCache pipelineCache, long vkRenderPass) {
		Pipeline.PipeLineCreationInfo pipeLineCreationInfo = new Pipeline.PipeLineCreationInfo(
				vkRenderPass, shaderProgram, GeometryAttachments.NUMBER_COLOR_ATTACHMENTS,
                true, true, 0,
                new InstancedVertexBufferStructure(), skyboxDescriptorSetLayouts);
        pipeline = new Pipeline(pipelineCache, pipeLineCreationInfo);
        pipeLineCreationInfo.cleanup();
	}
	
	public void createShaders() {
		EngineProperties engineProperties = EngineProperties.INSTANCE;
		if (engineProperties.isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.SKYBOX_VERTEX_GLSL, Shaderc.shaderc_glsl_vertex_shader);
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.SKYBOX_FRAGMENT_GLSL, Shaderc.shaderc_glsl_fragment_shader);
        }
		shaderProgram = new ShaderProgram(device, new ShaderProgram.ShaderModuleData[]
                {
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_VERTEX_BIT, Shaders.Vulkan.SKYBOX_VERTEX_SPV),
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_FRAGMENT_BIT, Shaders.Vulkan.SKYBOX_FRAGMENT_SPV,
                        		skyboxSpecConstants.getSpecInfo()),
                });
	}
	
	public void loadModel(List<VKTexture> textureCacheList) {
        device.waitIdle();
        // Size of the descriptor is setup in the layout, we need to fill up the texture list
        // up to the number defined in the layout (reusing last texture)
        int textureCacheSize = textureCacheList.size();
        List<VKTexture> textureList = new ArrayList<>(textureCacheList);
        EngineProperties engineProperties = EngineProperties.INSTANCE;
        int maxTextures = engineProperties.getMaxSkyboxTextures();
        for (int i = 0; i < maxTextures - textureCacheSize; i++) {
            textureList.add(textureCacheList.get(textureCacheSize - 1));
        }
        textureDescriptorSet = new TextureDescriptorSet(descriptorPool, textureDescriptorSetLayout, textureList,
                textureSampler, 0);
    }
	
	public void recordCommandBuffer(CommandBuffer commandBuffer, GlobalBuffers globalBuffers, int idx) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
            int width = swapChainExtent.width();
            int height = swapChainExtent.height();
            
            VkCommandBuffer cmdHandle = commandBuffer.getVkCommandBuffer();
            
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
            		.put(0, projMatrixDescriptorSet.getVkDescriptorSet())
                    .put(1, viewMatricesDescriptorSets[idx].getVkDescriptorSet())
                    .put(2, materialsDescriptorSet.getVkDescriptorSet())
                    .put(3, textureDescriptorSet.getVkDescriptorSet());
            
            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS,
            		pipeline.getVkPipelineLayout(), 0, descriptorSets, null);
            
            LongBuffer vertexBuffer = stack.mallocLong(1);
            LongBuffer instanceBuffer = stack.mallocLong(1);
            LongBuffer offsets = stack.mallocLong(1).put(0, 0L);
            
            if (globalBuffers.getNumSkyboxIndirectCommands() > 0) {
            	vertexBuffer.put(0, globalBuffers.getSkyboxVerticesBuffer().getBuffer());
            	instanceBuffer.put(0, globalBuffers.getSkyboxInstanceDataBuffers()[idx].getBuffer());
            	
            	vkCmdBindVertexBuffers(cmdHandle, 0, vertexBuffer, offsets);
                vkCmdBindVertexBuffers(cmdHandle, 1, instanceBuffer, offsets);
                vkCmdBindIndexBuffer(cmdHandle, globalBuffers.getSkyboxIndicesBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
                VulkanBuffer skyboxIndirectBuffer = globalBuffers.getSkyboxIndirectBuffer();
                vkCmdDrawIndexedIndirect(cmdHandle, skyboxIndirectBuffer.getBuffer(), 0, globalBuffers.getNumSkyboxIndirectCommands(),
                        GlobalBuffers.IND_COMMAND_STRIDE);
            }
		}
	}
	
	public void render() {
        int idx = swapChain.getCurrentFrame();
        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();
        float m30 = viewMatrix.m30();
        viewMatrix.m30(0);
        float m31 = viewMatrix.m31();
        viewMatrix.m31(0);
        float m32 = viewMatrix.m32();
        viewMatrix.m32(0);
        VulkanUtils.copyMatrixToBuffer(viewMatricesBuffer[idx], viewMatrix);
        viewMatrix.m30(m30);
        viewMatrix.m31(m31);
        viewMatrix.m32(m32);
    }
	
	public void resize(SwapChain swapChain) {
        VulkanUtils.copyMatrixToBuffer(projMatrixUniform, window.getProjectionMatrix());
        this.swapChain = swapChain;
    }
}
