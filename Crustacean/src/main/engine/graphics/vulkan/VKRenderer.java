package main.engine.graphics.vulkan;

import java.util.*;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import main.engine.ItemLoadTimestamp;
import main.engine.graphics.lights.Light;
import main.engine.graphics.vulkan.skybox.SkyboxBuffers;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import main.engine.EngineProperties;
import main.engine.Window;
import main.engine.graphics.ModelData;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.ui.NKHudElement;
import main.engine.graphics.vulkan.animation.AnimationComputeActivity;
import main.engine.graphics.vulkan.geometry.GeometryRenderActivity;
import main.engine.graphics.vulkan.lighting.LightingRenderActivity;
import main.engine.graphics.vulkan.nuklear.NuklearRenderActivity;
import main.engine.graphics.vulkan.shadows.ShadowRenderActivity;
import main.engine.graphics.vulkan.skybox.SkyboxRenderActivity;

import static org.lwjgl.vulkan.VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;

public class VKRenderer {
	
	private static final EngineProperties engProps = EngineProperties.INSTANCE;
	
	private final AnimationComputeActivity animationComputeActivity;
	private final CommandPool commandPool;
    private final Device device;
    private final Dominion dominion;
    private final GlobalBuffers globalBuffers;
    private final SkyboxBuffers skyboxBuffers;
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
    private final VKTextureCache modelTextureCache;
    private final VKTextureCache skyboxTextureCache;
    
    private CommandBuffer[] commandBuffers;
    private long gameItemsLoadedTimeStamp;
    private Fence[] fences;
    private SwapChain swapChain;
    private VulkanModel skybox;
    private Camera camera;
    private Light directionalLight;
	
	public VKRenderer(Window window, Dominion dominion) throws Exception {
        this.dominion = dominion;
        camera = dominion.findEntitiesWith(Camera.class).iterator().next().comp();
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
        skyboxBuffers = new SkyboxBuffers(device);
        geometryRenderActivity = new GeometryRenderActivity(swapChain, pipelineCache, window, globalBuffers);
        shadowRenderActivity = new ShadowRenderActivity(swapChain, pipelineCache, window);
        skyboxRenderActivity = new SkyboxRenderActivity(swapChain, pipelineCache, window,
				geometryRenderActivity.getFrameBuffer().getRenderPass().getVkRenderPass(), skyboxBuffers);
        List<Attachment> attachments = new ArrayList<>(geometryRenderActivity.getAttachments());
        attachments.add(shadowRenderActivity.getDepthAttachment());
        lightingRenderActivity = new LightingRenderActivity(swapChain, commandPool, pipelineCache, attachments, window);
        animationComputeActivity = new AnimationComputeActivity(commandPool, pipelineCache);
        nuklearRenderActivity = new NuklearRenderActivity(swapChain, commandPool, graphQueue, pipelineCache,
                lightingRenderActivity.getLightingFrameBuffer().getLightingRenderPass().getVkRenderPass(), window);
        modelTextureCache = new VKTextureCache();
        skyboxTextureCache = new VKTextureCache();
        gameItemsLoadedTimeStamp = 0;
        createCommandBuffers();
        window.setUiInput(() -> nuklearRenderActivity.input(window));
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
        modelTextureCache.cleanup();
        skyboxTextureCache.cleanup();
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
        skyboxBuffers.cleanup();
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
	
	public void loadSkyBox(ModelData skyboxModelData) throws Exception {
		Logger.debug("Loading Skybox model");
		skybox = skyboxBuffers.loadSkyboxModel(skyboxModelData, skyboxTextureCache, commandPool, graphQueue);
		Logger.debug("Loaded Skybox model");
		
		skyboxRenderActivity.loadModel(skyboxTextureCache);
	}
	
	public void loadParticles(List<ModelData> modelDataList, int maxParticles) throws Exception {
	}
	
	public void loadModels() throws Exception {
		Logger.debug("Loading models");
        globalBuffers.loadModels(dominion, modelTextureCache, commandPool, graphQueue);
		Logger.debug("Loaded models");
		
		geometryRenderActivity.loadModels(modelTextureCache);
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

    public Light getDirectionalLight() {
        return directionalLight;
    }

    public void setDirectionalLight(Light directionalLight) {
        this.directionalLight = directionalLight;
    }

    public void selectCamera(String cameraName) {
        Results<Results.With1<Camera>> results = dominion.findEntitiesWith(Camera.class);
        for (Iterator<Results.With1<Camera>> itr = results.iterator(); itr.hasNext();) {
            Results.With1<Camera> result = itr.next();
            if (result.entity().getName().equals(cameraName)) {
                this.camera = result.comp();
                return;
            }
        }
    }

    public Camera getCamera() {
        return camera;
    }
	
	private void recordCommands() {
        int idx = 0;
        for (CommandBuffer commandBuffer : commandBuffers) {
            commandBuffer.reset();
            commandBuffer.beginRecording();
            geometryRenderActivity.recordCommandBuffer(commandBuffer, globalBuffers, idx);
            skyboxRenderActivity.recordCommandBuffer(commandBuffer, skyboxBuffers, idx);
            geometryRenderActivity.endRenderPass(commandBuffer);
            shadowRenderActivity.recordCommandBuffer(commandBuffer, globalBuffers, idx);
            commandBuffer.endRecording();
            idx++;
        }
    }
	
	public void render(Window window) throws Throwable {
        ItemLoadTimestamp timestamp = dominion.findEntitiesWith(ItemLoadTimestamp.class).iterator().next().comp();
		if (gameItemsLoadedTimeStamp < timestamp.gameItemLoadedTimestamp) {
            gameItemsLoadedTimeStamp = timestamp.gameItemLoadedTimestamp;
            device.waitIdle();
            globalBuffers.loadGameItems(dominion, commandPool, graphQueue, swapChain.getNumImages());
            skyboxBuffers.loadSkybox(skybox, commandPool, graphQueue, swapChain.getNumImages());
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

        globalBuffers.loadInstanceData(dominion, swapChain.getCurrentFrame());
        skyboxBuffers.loadSkyboxInstanceData(dominion, skybox, swapChain.getCurrentFrame());

        animationComputeActivity.recordCommandBuffer(globalBuffers, dominion);
        animationComputeActivity.submit();
        
        CommandBuffer commandBuffer = acquireCurrentCommandBuffer();
        geometryRenderActivity.render(camera);
        skyboxRenderActivity.render(camera);
        shadowRenderActivity.render(camera, directionalLight);
        submitSceneCommand(graphQueue, commandBuffer);
        
        commandBuffer = lightingRenderActivity.beginRecording(shadowRenderActivity.getShadowCascades(), dominion, camera);
        lightingRenderActivity.recordCommandBuffer(commandBuffer);
        nuklearRenderActivity.recordCommandBuffer(commandBuffer);
        lightingRenderActivity.endRecording(commandBuffer);
        lightingRenderActivity.submit(graphQueue);

        if (swapChain.presentImage(graphQueue)) {
            window.setResized(true);
        }
	}

    //public void renderPhysicMeshes()
	
	private void resize(Window window) {
        device.waitIdle();
        graphQueue.waitIdle();

        swapChain.cleanup();

        swapChain = new SwapChain(device, surface, window, engProps.getRequestedImages(),
                engProps.isvSync());
        geometryRenderActivity.resize(swapChain);
        skyboxRenderActivity.resize(swapChain);
        shadowRenderActivity.resize(swapChain, camera, directionalLight);
        recordCommands();
        List<Attachment> attachments = new ArrayList<>(geometryRenderActivity.getAttachments());
        attachments.add(shadowRenderActivity.getDepthAttachment());
        lightingRenderActivity.resize(swapChain, attachments);
        nuklearRenderActivity.resize(swapChain);
    }
	
	private void submitSceneCommand(Queue queue, CommandBuffer commandBuffer) {
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

    public void unloadModels() {
        Logger.debug("Unloading  Models");
        device.waitIdle();
        Results<Results.With1<VulkanModel>> vulkanModels = dominion.findEntitiesWith(VulkanModel.class);
        Iterator<Results.With1<VulkanModel>> itr = vulkanModels.iterator();
        while (itr.hasNext()) {
            Results.With1<VulkanModel> vulkanModel = itr.next();
            dominion.deleteEntity(vulkanModel.entity());
        }
        globalBuffers.resetModelBuffers();
        modelTextureCache.cleanup();
        Logger.debug("Unloaded Model(s)");
    }
}
