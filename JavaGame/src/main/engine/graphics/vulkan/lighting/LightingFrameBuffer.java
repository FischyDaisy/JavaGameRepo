package main.engine.graphics.vulkan.lighting;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;

import main.engine.graphics.vulkan.*;

import java.nio.LongBuffer;
import java.util.Arrays;

public class LightingFrameBuffer {

    private final LightingRenderPass lightingRenderPass;

    private FrameBuffer[] frameBuffers;

    public LightingFrameBuffer(SwapChain swapChain) {
        //LOGGER.debug("Creating Lighting FrameBuffer");
        lightingRenderPass = new LightingRenderPass(swapChain);
        createFrameBuffers(swapChain);
    }

    public void cleanup() {
        //LOGGER.debug("Destroying Lighting FrameBuffer");
        Arrays.stream(frameBuffers).forEach(FrameBuffer::cleanup);
        lightingRenderPass.cleanup();
    }

    private void createFrameBuffers(SwapChain swapChain) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkExtent2D extent2D = swapChain.getSwapChainExtent();
            int width = extent2D.width();
            int height = extent2D.height();

            int numImages = swapChain.getNumImages();
            frameBuffers = new FrameBuffer[numImages];
            LongBuffer attachmentsBuff = stack.mallocLong(1);
            for (int i = 0; i < numImages; i++) {
                attachmentsBuff.put(0, swapChain.getImageViews()[i].getVkImageView());
                frameBuffers[i] = new FrameBuffer(swapChain.getDevice(), width, height,
                        attachmentsBuff, lightingRenderPass.getVkRenderPass(), 1);
            }
        }
    }

    public FrameBuffer[] getFrameBuffers() {
        return frameBuffers;
    }

    public LightingRenderPass getLightingRenderPass() {
        return lightingRenderPass;
    }

    public void resize(SwapChain swapChain) {
        Arrays.stream(frameBuffers).forEach(FrameBuffer::cleanup);
        createFrameBuffers(swapChain);
    }
}