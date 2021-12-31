package main.engine.graphics.vulkan;

import java.util.ArrayList;
import java.util.List;

import main.engine.EngineProperties;
import main.engine.Scene;
import main.engine.Window;
import main.engine.graphics.IHud;
import main.engine.graphics.ModelData;
import main.engine.graphics.Renderer;
import main.engine.graphics.TextureCache;
import main.engine.graphics.camera.Camera;

public class VKRenderer implements Renderer {
	
	private static final EngineProperties engProps = EngineProperties.getInstance();
	
	private final CommandPool commandPool;
    private final Device device;
    private final ForwardRenderActivity fwdRenderActivity;
    private final Queue.GraphicsQueue graphQueue;
    private final Instance instance;
    private final PhysicalDevice physicalDevice;
    private final PipelineCache pipelineCache;
    private final Queue.PresentQueue presentQueue;
    private final Surface surface;
    private final TextureCache textureCache;
    private final List<VulkanModel> vulkanModels;
    
    private SwapChain swapChain;
	
	public VKRenderer(Window window, Scene scene) throws Exception {
		instance = new Instance(engProps.isValidate());
        physicalDevice = PhysicalDevice.createPhysicalDevice(instance, engProps.getPhysDeviceName());
        device = new Device(physicalDevice);
        surface = new Surface(physicalDevice, window.getWindowHandle());
        graphQueue = new Queue.GraphicsQueue(device, 0);
        presentQueue = new Queue.PresentQueue(device, surface, 0);
        swapChain = new SwapChain(device, surface, window, engProps.getRequestedImages(),
                engProps.isvSync());
        commandPool = new CommandPool(device, graphQueue.getQueueFamilyIndex());
        pipelineCache = new PipelineCache(device);
        fwdRenderActivity = new ForwardRenderActivity(swapChain, commandPool, pipelineCache, scene, window);
        vulkanModels = new ArrayList<>();
        textureCache = TextureCache.getInstance();
	}

	public void render(Window window, Camera camera, Scene scene) {
		if (window.getWidth() <= 0 && window.getHeight() <= 0) {
            return;
        }
        if (window.isResized() || swapChain.acquireNextImage()) {
            window.setResized(false);
            resize(window);
            window.updateProjectionMatrix();
            swapChain.acquireNextImage();
        }

        fwdRenderActivity.recordCommandBuffer(vulkanModels);
        fwdRenderActivity.submit(presentQueue);

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
        fwdRenderActivity.cleanup();
        commandPool.cleanup();
        swapChain.cleanup();
        surface.cleanup();
        device.cleanup();
        physicalDevice.cleanup();
        instance.cleanup();
	}
	
	public void loadSkyBox(ModelData skybox) throws Exception {
	}
	
	public void loadParticles(List<ModelData> modelDataList, int maxParticles) throws Exception {
	}
	
	public void loadModels(List<ModelData> modelDataList) throws Exception {
		vulkanModels.addAll(VulkanModel.transformModels(modelDataList, textureCache, commandPool, graphQueue));
		
		fwdRenderActivity.registerModels(vulkanModels);
    }
	
	public void clearAndLoadModels(List<ModelData> modelDataList) throws Exception {
		vulkanModels.clear();
		loadModels(modelDataList);
	}
	
	private void resize(Window window) {
        EngineProperties engProps = EngineProperties.getInstance();

        device.waitIdle();
        graphQueue.waitIdle();

        swapChain.cleanup();

        swapChain = new SwapChain(device, surface, window, engProps.getRequestedImages(),
                engProps.isvSync());
        fwdRenderActivity.resize(swapChain);
    }
}
