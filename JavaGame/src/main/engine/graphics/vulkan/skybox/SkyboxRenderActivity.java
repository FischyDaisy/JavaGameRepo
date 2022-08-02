package main.engine.graphics.vulkan.skybox;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
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
	private final int materialSize;
    private final Scene scene;
    private final Window window;
    
    private DescriptorPool descriptorPool;
    private Map<String, TextureDescriptorSet> descriptorSetMap;
    private DescriptorSetLayout[] skyboxDescriptorSetLayouts;
    private DescriptorSetLayout.DynUniformDescriptorSetLayout materialDescriptorSetLayout;
    private VulkanBuffer materialsBuffer;
    private DescriptorSet.DynUniformDescriptorSet materialsDescriptorSet;
    private Pipeline pipeline;
    private DescriptorSet.UniformDescriptorSet projMatrixDescriptorSet;
    private VulkanBuffer projMatrixUniform;
    private ShaderProgram shaderProgram;
    private SwapChain swapChain;
    private DescriptorSetLayout.SamplerDescriptorSetLayout textureDescriptorSetLayout;
    private TextureSampler textureSampler;
    private DescriptorSetLayout.UniformDescriptorSetLayout uniformDescriptorSetLayout;
    private VulkanBuffer[] viewMatricesBuffer;
    private DescriptorSet.UniformDescriptorSet[] viewMatricesDescriptorSets;
	
	public SkyboxRenderActivity(SwapChain swapChain, PipelineCache pipelineCache, Scene scene, Window window, long vkRenderPass, int materialSize) {
		this.scene = scene;
		this.window = window;
		this.materialSize = materialSize;
		device = swapChain.getDevice();
		this.swapChain = swapChain;
		
		int numImages = swapChain.getNumImages();
		
		createShaders();
		createDescriptorPool();
		createDescriptorSets(numImages);
		createPipeline(pipelineCache, vkRenderPass);
		VulkanUtils.copyMatrixToBuffer(projMatrixUniform, window.getProjectionMatrix());
	}
	
	public void cleanup() {
		Logger.trace("Cleaning up SkyboxRenderActivity");
		pipeline.cleanup();
        materialsBuffer.cleanup();
        Arrays.stream(viewMatricesBuffer).forEach(VulkanBuffer::cleanup);
        projMatrixUniform.cleanup();
        textureSampler.cleanup();
        materialDescriptorSetLayout.cleanup();
        textureDescriptorSetLayout.cleanup();
        uniformDescriptorSetLayout.cleanup();
        descriptorPool.cleanup();
        shaderProgram.cleanup();
	}
	
	public void createDescriptorPool() {
		EngineProperties engineProperties = EngineProperties.INSTANCE;
        List<DescriptorPool.DescriptorTypeCount> descriptorTypeCounts = new ArrayList<>();
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(swapChain.getNumImages() + 1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(engineProperties.getMaxSkyboxMaterials() * 3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC));
        descriptorPool = new DescriptorPool(device, descriptorTypeCounts);
	}
	
	public void createDescriptorSets(int numImages) {
		uniformDescriptorSetLayout = new DescriptorSetLayout.UniformDescriptorSetLayout(device, 0, VK_SHADER_STAGE_VERTEX_BIT, 0);
        textureDescriptorSetLayout = new DescriptorSetLayout.SamplerDescriptorSetLayout(device, 0, VK_SHADER_STAGE_FRAGMENT_BIT, 0);
        materialDescriptorSetLayout = new DescriptorSetLayout.DynUniformDescriptorSetLayout(device, 0, VK_SHADER_STAGE_FRAGMENT_BIT, 0);
        skyboxDescriptorSetLayouts = new DescriptorSetLayout[]{
        		uniformDescriptorSetLayout,
                uniformDescriptorSetLayout,
                textureDescriptorSetLayout,
                textureDescriptorSetLayout,
                textureDescriptorSetLayout,
                materialDescriptorSetLayout
        };
        
        EngineProperties engineProperties = EngineProperties.INSTANCE;
        descriptorSetMap = new HashMap<>();
        textureSampler = new TextureSampler(device, 1);
        projMatrixUniform = new VulkanBuffer(device, GraphConstants.MAT4X4_SIZE_BYTES, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
        projMatrixDescriptorSet = new DescriptorSet.UniformDescriptorSet(descriptorPool, uniformDescriptorSetLayout, projMatrixUniform, 0);

        viewMatricesDescriptorSets = new DescriptorSet.UniformDescriptorSet[numImages];
        viewMatricesBuffer = new VulkanBuffer[numImages];
        materialsBuffer = new VulkanBuffer(device, (long) materialSize * engineProperties.getMaxSkyboxMaterials(), 
        		VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
        materialsDescriptorSet = new DescriptorSet.DynUniformDescriptorSet(descriptorPool, materialDescriptorSetLayout,
                materialsBuffer, 0, materialSize);
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
                true, true, GraphConstants.MAT4X4_SIZE_BYTES,
                new VertexBufferStructure(), skyboxDescriptorSetLayouts);
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
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_FRAGMENT_BIT, Shaders.Vulkan.SKYBOX_FRAGMENT_SPV),
                });
	}
	
	public void recordCommandBuffer(CommandBuffer commandBuffer, VulkanModel skyboxModel) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
            int width = swapChainExtent.width();
            int height = swapChainExtent.height();
            int idx = swapChain.getCurrentFrame();
            
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
            
            LongBuffer descriptorSets = stack.mallocLong(6)
            		.put(0, projMatrixDescriptorSet.getVkDescriptorSet())
                    .put(1, viewMatricesDescriptorSets[idx].getVkDescriptorSet())
                    .put(5, materialsDescriptorSet.getVkDescriptorSet());
            Matrix4f viewMatrix = scene.getCamera().getViewMatrix();
            float m30 = viewMatrix.m30();
            viewMatrix.m30(0);
            float m31 = viewMatrix.m31();
            viewMatrix.m31(0);
            float m32 = viewMatrix.m32();
            viewMatrix.m32(0);
            VulkanUtils.copyMatrixToBuffer(viewMatricesBuffer[idx], viewMatrix);
            
            LongBuffer offsets = stack.mallocLong(1);
            offsets.put(0, 0L);
            LongBuffer vertexBuffer = stack.mallocLong(1);
            IntBuffer dynDescrSetOffset = stack.callocInt(1);
            int materialCount = 0;
            
            int meshCount = 0;
            for (VulkanModel.VulkanMaterial material : skyboxModel.getVulkanMaterialList()) {
                int materialOffset = materialCount * materialSize;
                dynDescrSetOffset.put(0, materialOffset);
                TextureDescriptorSet textureDescriptorSet = descriptorSetMap.get(material.texture().getFileName());
                TextureDescriptorSet normalMapDescriptorSet = descriptorSetMap.get(material.normalMap().getFileName());
                TextureDescriptorSet metalRoughDescriptorSet = descriptorSetMap.get(material.metalRoughMap().getFileName());

                for (VulkanModel.VulkanMesh mesh : material.vulkanMeshList()) {
                	vertexBuffer.put(0, mesh.verticesBuffer().getBuffer());
                    vkCmdBindVertexBuffers(cmdHandle, 0, vertexBuffer, offsets);
                    vkCmdBindIndexBuffer(cmdHandle, mesh.indicesBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
                    
                	descriptorSets.put(2, textureDescriptorSet.getVkDescriptorSet());
                    descriptorSets.put(3, normalMapDescriptorSet.getVkDescriptorSet());
                    descriptorSets.put(4, metalRoughDescriptorSet.getVkDescriptorSet());
                	vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS,
                            pipeline.getVkPipelineLayout(), 0, descriptorSets, dynDescrSetOffset);

                    VulkanUtils.setMatrixAsPushConstant(pipeline, cmdHandle, scene.getSkyBox().buildModelMatrix());
                    vkCmdDrawIndexed(cmdHandle, mesh.numIndices(), 1, 0, 0, 0);
                    meshCount++;
                }
                materialCount++;
            }
            viewMatrix.m30(m30);
            viewMatrix.m31(m31);
            viewMatrix.m32(m32);
		}
	}
	
	public void registerModel(VulkanModel skyboxModel) {
        device.waitIdle();
        int materialCount = 0;
        for (VulkanModel.VulkanMaterial vulkanMaterial : skyboxModel.getVulkanMaterialList()) {
            int materialOffset = materialCount * materialSize;
            updateTextureDescriptorSet(vulkanMaterial.texture());
            updateTextureDescriptorSet(vulkanMaterial.normalMap());
            updateTextureDescriptorSet(vulkanMaterial.metalRoughMap());
            updateMaterialsBuffer(materialsBuffer, vulkanMaterial, materialOffset);
            materialCount++;
        }
    }
	
	public void resize(SwapChain swapChain) {
        VulkanUtils.copyMatrixToBuffer(projMatrixUniform, window.getProjectionMatrix());
        this.swapChain = swapChain;
    }
	
	private void updateMaterialsBuffer(VulkanBuffer vulkanBuffer, VulkanModel.VulkanMaterial material, int offset) {
        long mappedMemory = vulkanBuffer.map();
        ByteBuffer materialBuffer = MemoryUtil.memByteBuffer(mappedMemory, (int) vulkanBuffer.getRequestedSize());
        material.diffuseColor().get(offset, materialBuffer);
        materialBuffer.putFloat(offset + GraphConstants.FLOAT_SIZE_BYTES * 4, material.hasTexture() ? 1.0f : 0.0f);
        materialBuffer.putFloat(offset + GraphConstants.FLOAT_SIZE_BYTES * 5, material.hasNormalMap() ? 1.0f : 0.0f);
        materialBuffer.putFloat(offset + GraphConstants.FLOAT_SIZE_BYTES * 6, material.hasMetalRoughMap() ? 1.0f : 0.0f);
        materialBuffer.putFloat(offset + GraphConstants.FLOAT_SIZE_BYTES * 7, material.roughnessFactor());
        materialBuffer.putFloat(offset + GraphConstants.FLOAT_SIZE_BYTES * 8, material.metallicFactor());
        vulkanBuffer.unMap();
    }

    private void updateTextureDescriptorSet(VKTexture texture) {
        String textureFileName = texture.getFileName();
        TextureDescriptorSet textureDescriptorSet = descriptorSetMap.get(textureFileName);
        if (textureDescriptorSet == null) {
            textureDescriptorSet = new TextureDescriptorSet(descriptorPool, textureDescriptorSetLayout,
                    texture, textureSampler, 0);
            descriptorSetMap.put(textureFileName, textureDescriptorSet);
        }
    }
}
