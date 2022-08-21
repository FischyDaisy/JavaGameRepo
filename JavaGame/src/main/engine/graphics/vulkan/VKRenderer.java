package main.engine.graphics.vulkan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

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
import main.engine.items.SkyBox;
import main.engine.scene.Scene;

import static org.lwjgl.vulkan.VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;

public class VKRenderer {
	
	private static final EngineProperties engProps = EngineProperties.INSTANCE;
	
	private final AnimationComputeActivity animationComputeActivity;
	private final CommandPool commandPool;
    private final Device device;
    private final GlobalBuffers globalBuffers;
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
    
    private CommandBuffer[] commandBuffers;
    private long gameItemsLoadedTimeStamp;
    private Fence[] fences;
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
        globalBuffers = new GlobalBuffers(device);
        geometryRenderActivity = new GeometryRenderActivity(swapChain, pipelineCache, scene, window, globalBuffers);
        shadowRenderActivity = new ShadowRenderActivity(swapChain, pipelineCache, scene, window);
        skyboxRenderActivity = new SkyboxRenderActivity(swapChain, pipelineCache, scene, window, 
				geometryRenderActivity.getFrameBuffer().getRenderPass().getVkRenderPass(), globalBuffers);
        List<Attachment> attachments = new ArrayList<>(geometryRenderActivity.getAttachments());
        attachments.add(shadowRenderActivity.getDepthAttachment());
        lightingRenderActivity = new LightingRenderActivity(swapChain, commandPool, pipelineCache, attachments, scene, window);
        animationComputeActivity = new AnimationComputeActivity(commandPool, pipelineCache, scene);
        nuklearRenderActivity = new NuklearRenderActivity(swapChain, commandPool, graphQueue, pipelineCache,
                lightingRenderActivity.getLightingFrameBuffer().getLightingRenderPass().getVkRenderPass(), window);
        vulkanModels = new ArrayList<>();
        textureCache = VKTextureCache.INSTANCE;
        gameItemsLoadedTimeStamp = 0;
        createCommandBuffers();
	}
	
	private CommandBuffer acquireCurrentCommandBuffer() {
        int idx = swapChain.getCurrentFrame();

        Fence fence = fences[idx];
        CommandBuffer commandBuffer = commandBuffers[idx];

        fence.fenceWait();
        fence.reset();

        return commandBuffer;
    }

	public void cleanup() {
		presentQueue.waitIdle();
        graphQueue.waitIdle();
        device.waitIdle();
        textureCache.cleanup();
        pipelineCache.cleanup();
        nuklearRenderActivity.cleanup();
        lightingRenderActivity.cleanup();
        animationComputeActivity.cleanup();
        shadowRenderActivity.cleanup();
        skyboxRenderActivity.cleanup();
        geometryRenderActivity.cleanup();
        Arrays.stream(commandBuffers).forEach(CommandBuffer::cleanup);
        Arrays.stream(fences).forEach(Fence::cleanup);
        globalBuffers.cleanup();
        commandPool.cleanup();
        swapChain.cleanup();
        surface.cleanup();
        device.cleanup();
        physicalDevice.cleanup();
        instance.cleanup();
	}
	
	private void createCommandBuffers() {
        int numImages = swapChain.getNumImages();
        commandBuffers = new CommandBuffer[numImages];
        fences = new Fence[numImages];

        for (int i = 0; i < numImages; i++) {
            commandBuffers[i] = new CommandBuffer(commandPool, true, false);
            fences[i] = new Fence(device, true);
        }
    }
	
	public void inputNuklear(Window window) {
		nuklearRenderActivity.input(window);
	}
	
	public void loadSkyBox(ModelData skyboxModelData, Scene scene) throws Exception {
		Logger.debug("Loading Skybox model");
		skybox = globalBuffers.loadSkyboxModel(skyboxModelData, textureCache, commandPool, graphQueue);
		Logger.debug("Loaded Skybox model");
		
		List<VKTexture> textureCacheList = textureCache.getAsList();
		SkyBox skybox = scene.getSkyBox();
		textureCacheList.removeIf(t -> !skybox.isSkyboxTexture(t));
		skyboxRenderActivity.loadModel(textureCacheList);
	}
	
	public void loadParticles(List<ModelData> modelDataList, int maxParticles) throws Exception {
	}
	
	public void loadModels(List<ModelData> modelDataList, Scene scene) throws Exception {
		Logger.debug("Loading {} model(s)", modelDataList.size());
		vulkanModels.addAll(globalBuffers.loadModels(modelDataList, textureCache, commandPool, graphQueue));
		Logger.debug("Loaded {} model(s)", modelDataList.size());
		
		if (skybox == null) {
			List<VKTexture> textureCacheList = textureCache.getAsList();
			geometryRenderActivity.loadModels(textureCacheList);
		} else {
			List<VKTexture> textureCacheList = textureCache.getAsList();
			SkyBox skybox = scene.getSkyBox();
			textureCacheList.removeIf(t -> skybox.isSkyboxTexture(t));
			geometryRenderActivity.loadModels(textureCacheList);
		}
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
	
	private void recordCommands() { //this causes errors
        int idx = 0;
        for (CommandBuffer commandBuffer : commandBuffers) {
            commandBuffer.reset();
            commandBuffer.beginRecording();
            geometryRenderActivity.recordCommandBuffer(commandBuffer, globalBuffers, idx);
            skyboxRenderActivity.recordCommandBuffer(commandBuffer, globalBuffers, idx);
            geometryRenderActivity.endRenderPass(commandBuffer);
            shadowRenderActivity.recordCommandBuffer(commandBuffer, globalBuffers, idx);
            commandBuffer.endRecording();
            idx++;
        }
    }
	
	public void render(Window window, Scene scene) {
		if (gameItemsLoadedTimeStamp < scene.getGameItemsLoadedTimeStamp()) {
            gameItemsLoadedTimeStamp = scene.getGameItemsLoadedTimeStamp();
            device.waitIdle();
            globalBuffers.loadGameItems(vulkanModels, scene, commandPool, graphQueue, swapChain.getNumImages());
            globalBuffers.loadSkybox(skybox, scene, commandPool, graphQueue, swapChain.getNumImages());
            animationComputeActivity.onAnimatedGameItemsLoaded(globalBuffers);
            recordCommands();
        }
		if (window.getWidth() <= 0 && window.getHeight() <= 0) {
            return;
        }
        if (window.isResized() || swapChain.acquireNextImage()) {
            window.setResized(false);
            resize(window);
            window.updateProjectionMatrix();
            swapChain.acquireNextImage();
        }
        
        globalBuffers.loadInstanceData(scene, vulkanModels, swapChain.getCurrentFrame());
        globalBuffers.loadSkyboxInstanceData(scene, skybox, swapChain.getCurrentFrame());
        
        animationComputeActivity.recordCommandBuffer(globalBuffers);
        animationComputeActivity.submit();

        //CommandBuffer commandBuffer = geometryRenderActivity.beginRecording();
        //geometryRenderActivity.recordCommandBuffer(commandBuffer, vulkanModels, animationComputeActivity.getGameItemAnimationsBuffers());
        //skyboxRenderActivity.recordCommandBuffer(commandBuffer, skybox);
        //geometryRenderActivity.endRenderPass(commandBuffer);
        //shadowRenderActivity.recordCommandBuffer(commandBuffer, vulkanModels, animationComputeActivity.getGameItemAnimationsBuffers());
        //geometryRenderActivity.endRecording(commandBuffer);
        //geometryRenderActivity.submit(graphQueue);
        
        CommandBuffer commandBuffer = acquireCurrentCommandBuffer();
        geometryRenderActivity.render();
        skyboxRenderActivity.render();
        shadowRenderActivity.render();
        submitSceneCommand(graphQueue, commandBuffer);
        
        commandBuffer = lightingRenderActivity.beginRecording(shadowRenderActivity.getShadowCascades());
        lightingRenderActivity.recordCommandBuffer(commandBuffer);
        nuklearRenderActivity.recordCommandBuffer(commandBuffer);
        lightingRenderActivity.endRecording(commandBuffer);
        lightingRenderActivity.submit(graphQueue);

        if (swapChain.presentImage(graphQueue)) {
            window.setResized(true);
        }
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
        recordCommands();
        List<Attachment> attachments = new ArrayList<>(geometryRenderActivity.getAttachments());
        attachments.add(shadowRenderActivity.getDepthAttachment());
        lightingRenderActivity.resize(swapChain, attachments);
        nuklearRenderActivity.resize(swapChain);
    }
	
	public void submitSceneCommand(Queue queue, CommandBuffer commandBuffer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int idx = swapChain.getCurrentFrame();
            Fence currentFence = fences[idx];
            SwapChain.SyncSemaphores syncSemaphores = swapChain.getSyncSemaphoresList()[idx];
            queue.submit(stack.pointers(commandBuffer.getVkCommandBuffer()),
                    stack.longs(syncSemaphores.imgAcquisitionSemaphore().getVkSemaphore()),
                    stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT),
                    stack.longs(syncSemaphores.geometryCompleteSemaphore().getVkSemaphore()), currentFence);
        }
    }
}
