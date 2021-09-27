package main.engine.graphics.vulkan;

import main.engine.EngineProperties;
import main.engine.Scene;
import main.engine.Window;
import main.engine.graphics.IHud;
import main.engine.graphics.IRenderer;
import main.engine.graphics.camera.Camera;

public class VKRenderer implements IRenderer {
	
	private static final EngineProperties engProps = EngineProperties.getInstance();
	
	private CommandPool commandPool;
    private Device device;
    private ForwardRenderActivity fwdRenderActivity;
    private Queue.GraphicsQueue graphQueue;
    private Instance instance;
    private PhysicalDevice physicalDevice;
    private Queue.PresentQueue presentQueue;
    private Surface surface;
    private SwapChain swapChain;
	
	public VKRenderer(Window window, Scene scene) {
	}

	@Override
	public void init(Window window, Scene scene) throws Exception {
		instance = new Instance(engProps.isValidate());
        physicalDevice = PhysicalDevice.createPhysicalDevice(instance, engProps.getPhysDeviceName());
        device = new Device(physicalDevice);
        surface = new Surface(physicalDevice, window.getWindowHandle());
        graphQueue = new Queue.GraphicsQueue(device, 0);
        presentQueue = new Queue.PresentQueue(device, surface, 0);
        swapChain = new SwapChain(device, surface, window, engProps.getRequestedImages(),
                engProps.isvSync());
        commandPool = new CommandPool(device, graphQueue.getQueueFamilyIndex());
        fwdRenderActivity = new ForwardRenderActivity(swapChain, commandPool);
	}

	@Override
	public void render(Window window, Camera camera, Scene scene) {
		swapChain.acquireNextImage();

        fwdRenderActivity.submit(presentQueue);

        swapChain.presentImage(graphQueue);
	}

	@Override
	public void cleanup() {
		presentQueue.waitIdle();
        graphQueue.waitIdle();
        device.waitIdle();
        fwdRenderActivity.cleanup();
        commandPool.cleanup();
        swapChain.cleanup();
        surface.cleanup();
        device.cleanup();
        physicalDevice.cleanup();
        instance.cleanup();
	}
}
