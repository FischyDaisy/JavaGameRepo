package main.engine.graphics.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK11.*;

public class ImageView {

    private final Device device;
    private final long vkImageView;

    public ImageView(Device device, long vkImage, int format, int aspectMask, int mipLevels) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer lp = stack.mallocLong(1);
            VkImageViewCreateInfo viewCreateInfo = VkImageViewCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(vkImage)
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(format)
                    .subresourceRange(it -> it
                            .aspectMask(aspectMask)
                            .baseMipLevel(0)
                            .levelCount(mipLevels)
                            .baseArrayLayer(0)
                            .layerCount(1));

            VulkanUtils.vkCheck(vkCreateImageView(device.getVkDevice(), viewCreateInfo, null, lp),
                    "Failed to create image view");
            vkImageView = lp.get(0);
        }
    }

    public void cleanup() {
        vkDestroyImageView(device.getVkDevice(), vkImageView, null);
    }

    public long getVkImageView() {
        return vkImageView;
    }
}