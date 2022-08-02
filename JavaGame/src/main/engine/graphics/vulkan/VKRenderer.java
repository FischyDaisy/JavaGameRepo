package main.engine.graphics.vulkan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.lwjgl.nuklear.NkContext;

import main.engine.EngineProperties;
import main.engine.Window;
import main.engine.graphics.IHud;
import main.engine.graphics.ModelData;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.hud.Calculator;
import main.engine.graphics.hud.Demo;
import main.engine.graphics.hud.NKHudElement;
import main.engine.graphics.vulkan.animation.AnimationComputeActivity;
import main.engine.graphics.vulkan.geometry.GeometryRenderActivity;
import main.engine.graphics.vulkan.lighting.LightingRenderActivity;
import main.engine.graphics.vulkan.nuklear.NuklearRenderActivity;
import main.engine.graphics.vulkan.shadows.ShadowRenderActivity;
import main.engine.graphics.vulkan.skybox.SkyboxRenderActivity;
import main.engine.items.GameItem;
import main.engine.scene.Scene;

public class VKRenderer {
	
	private static final EngineProperties engProps = EngineProperties.INSTANCE;
	
	private final AnimationComputeActivity animationComputeActivity;
	private final CommandPool commandPool;
    private final Device device;
    private final GeometryRenderActivity geometryRenderActivity;
    private final LightingRenderActivity lightingRenderActivity;
    private final ShadowRenderActivity shadowRenderActivity;
    private final SkyboxRenderActivity skyboxRenderActivity;
    private final NuklearRenderActivity nuklearRenderActivity;
    private final Queue.GraphicsQueue graphQueue;
    private final Instance instance;
    private final PhysicalDevice physicalDevice;
    private final PipelineCache pipelineCache;
    private final Queue.PresentQueue presentQueue;
    private final Surface surface;
    private final VKTextureCache textureCache;
    private final List<VulkanModel> vulkanModels;
    
    private SwapChain swapChain;
    private VulkanModel skybox;
	
	public VKRenderer(Window window, Scene scene) throws Exception {
		instance = new Instance(engProps.isValidate());
        physicalDevice = PhysicalDevice.createPhysicalDevice(instance, engProps.getPhysDeviceName());
        device = new Device(instance, physicalDevice);
        surface = new Surface(physicalDevice, window.getWindowHandle());
        graphQueue = new Queue.GraphicsQueue(device, 0);
        presentQueue = new Queue.PresentQueue(device, surface, 0);
        swapChain = new SwapChain(device, surface, window, engProps.getRequestedImages(),
                engProps.isvSync());
        commandPool = new CommandPool(device, graphQueue.getQueueFamilyIndex());
        pipelineCache = new PipelineCache(device);
        geometryRenderActivity = new GeometryRenderActivity(swapChain, commandPool, pipelineCache, scene, window);
        shadowRenderActivity = new ShadowRenderActivity(swapChain, pipelineCache, scene, window);
        skyboxRenderActivity = new SkyboxRenderActivity(swapChain, pipelineCache, scene, window, 
				geometryRenderActivity.getFrameBuffer().getRenderPass().getVkRenderPass(), geometryRenderActivity.getMaterialSize());
        List<Attachment> attachments = new ArrayList<>(geometryRenderActivity.getAttachments());
        attachments.add(shadowRenderActivity.getDepthAttachment());
        lightingRenderActivity = new LightingRenderActivity(swapChain, commandPool, pipelineCache, attachments, scene, window);
        animationComputeActivity = new AnimationComputeActivity(commandPool, pipelineCache, scene);
        nuklearRenderActivity = new NuklearRenderActivity(swapChain, commandPool, graphQueue, pipelineCache,
                lightingRenderActivity.getLightingFrameBuffer().getLightingRenderPass().getVkRenderPass(), window);
        vulkanModels = new ArrayList<>();
        textureCache = VKTextureCache.INSTANCE;
	}

	public void render(Window window, Scene scene) {
		if (window.getWidth() <= 0 && window.getHeight() <= 0) {
            return;
        }
        if (window.isResized() || swapChain.acquireNextImage()) {
            window.setResized(false);
            resize(window);
            window.updateProjectionMatrix();
            swapChain.acquireNextImage();
        }
        
        animationComputeActivity.recordCommandBuffer(vulkanModels);
        animationComputeActivity.submit();

        CommandBuffer commandBuffer = geometryRenderActivity.beginRecording();
        geometryRenderActivity.recordCommandBuffer(commandBuffer, vulkanModels, animationComputeActivity.getGameItemAnimationsBuffers());
        skyboxRenderActivity.recordCommandBuffer(commandBuffer, skybox);
        geometryRenderActivity.endRenderPass(commandBuffer);
        shadowRenderActivity.recordCommandBuffer(commandBuffer, vulkanModels, animationComputeActivity.getGameItemAnimationsBuffers());
        geometryRenderActivity.endRecording(commandBuffer);
        geometryRenderActivity.submit(graphQueue);
        commandBuffer = lightingRenderActivity.beginRecording(shadowRenderActivity.getShadowCascades());
        lightingRenderActivity.recordCommandBuffer(commandBuffer);
        nuklearRenderActivity.recordCommandBuffer(commandBuffer);
        lightingRenderActivity.endRecording(commandBuffer);
        lightingRenderActivity.submit(graphQueue);

        if (swapChain.presentImage(graphQueue)) {
            window.setResized(true);
        }
	}

	public void cleanup() {
		presentQueue.waitIdle();
        graphQueue.waitIdle();
        device.waitIdle();
        textureCache.cleanup();
        vulkanModels.forEach(VulkanModel::cleanup);
        pipelineCache.cleanup();
        nuklearRenderActivity.cleanup();
        lightingRenderActivity.cleanup();
        animationComputeActivity.cleanup();
        shadowRenderActivity.cleanup();
        skyboxRenderActivity.cleanup();
        geometryRenderActivity.cleanup();
        commandPool.cleanup();
        swapChain.cleanup();
        surface.cleanup();
        device.cleanup();
        physicalDevice.cleanup();
        instance.cleanup();
	}
	
	public void inputNuklear(Window window) {
		nuklearRenderActivity.input(window);
	}
	
	public void loadSkyBox(ModelData skyboxModelData, Window window, Scene scene) throws Exception {
		if (skybox != null) {
			skybox.cleanup();
		}
		skybox = VulkanModel.transformModel(skyboxModelData, textureCache, commandPool, graphQueue);
		skyboxRenderActivity.registerModel(skybox);
	}
	
	public void loadParticles(List<ModelData> modelDataList, int maxParticles) throws Exception {
	}
	
	public void loadAnimation(GameItem item) {
        String modelId = item.getModelId();
        Optional<VulkanModel> optModel = vulkanModels.stream().filter(m -> m.getModelId().equals(modelId)).findFirst();
        if (optModel.isEmpty()) {
            throw new RuntimeException("Could not find model [" + modelId + "]");
        }
        VulkanModel vulkanModel = optModel.get();
        if (!vulkanModel.hasAnimations()) {
            throw new RuntimeException("Model [" + modelId + "] does not define animations");
        }

        animationComputeActivity.registerGameItem(vulkanModel, item);
    }
	
	public void loadModels(List<ModelData> modelDataList) throws Exception {
		vulkanModels.addAll(VulkanModel.transformModels(modelDataList, textureCache, commandPool, graphQueue));
		
		// Reorder materials inside models
		/*
        vulkanModels.forEach(m -> {
            Collections.sort(m.getVulkanMaterialList(), (a, b) -> Boolean.compare(a.isTransparent(), b.isTransparent()));
        });

        // Reorder models
        Collections.sort(vulkanModels, (a, b) -> {
            boolean aHasTransparentMt = a.getVulkanMaterialList().stream().filter(m -> m.isTransparent()).findAny().isPresent();
            boolean bHasTransparentMt = b.getVulkanMaterialList().stream().filter(m -> m.isTransparent()).findAny().isPresent();

            return Boolean.compare(aHasTransparentMt, bHasTransparentMt);
        });*/
		
        geometryRenderActivity.registerModels(vulkanModels);
        animationComputeActivity.registerModels(vulkanModels);
    }
	
	public void clearAndLoadModels(List<ModelData> modelDataList) throws Exception {
		vulkanModels.clear();
		loadModels(modelDataList);
	}
	
	public NKHudElement[] getNuklearElements() {
		return nuklearRenderActivity.getElements();
	}
	
	public void setNulkearElements(NKHudElement[] elements) {
		nuklearRenderActivity.setElements(elements);
	}
	
	public NkContext getNuklearContext() {
		return nuklearRenderActivity.getContext();
	}
	
	private void resize(Window window) {
        device.waitIdle();
        graphQueue.waitIdle();

        swapChain.cleanup();

        swapChain = new SwapChain(device, surface, window, engProps.getRequestedImages(),
                engProps.isvSync());
        geometryRenderActivity.resize(swapChain);
        skyboxRenderActivity.resize(swapChain);
        shadowRenderActivity.resize(swapChain);
        List<Attachment> attachments = new ArrayList<>(geometryRenderActivity.getAttachments());
        attachments.add(shadowRenderActivity.getDepthAttachment());
        lightingRenderActivity.resize(swapChain, attachments);
        nuklearRenderActivity.resize(swapChain);
    }
}
