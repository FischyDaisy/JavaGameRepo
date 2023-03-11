package main.engine.graphics.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCopy;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;

import java.lang.foreign.*;

public class StagingBuffer {
    private final MemorySegment dataSegment;
    private final MemorySession dataSession;
    private final VulkanBuffer stgVulkanBuffer;

    public StagingBuffer(Device device, long size) {
        stgVulkanBuffer = new VulkanBuffer(device, size, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        dataSession = MemorySession.openConfined();
        long mappedMemory = stgVulkanBuffer.map();
        MemoryAddress dataPtr = MemoryAddress.ofLong(mappedMemory);
        dataSegment = MemorySegment.ofAddress(dataPtr, stgVulkanBuffer.getRequestedSize(), dataSession);
    }

    public void cleanup() {
        stgVulkanBuffer.unMap();
        stgVulkanBuffer.cleanup();
        dataSession.close();
    }

    public ByteBuffer getDataBuffer() {
        return dataSegment.asByteBuffer();
    }

    public MemorySegment getDataSegment() {
        return dataSegment;
    }

    public long getRequestedSize() {
        return stgVulkanBuffer.getRequestedSize();
    }

    public void recordTransferCommand(CommandBuffer cmd, VulkanBuffer dstBuffer, long dstOffset, long size) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack)
                    .srcOffset(0).dstOffset(dstOffset).size(size);
            vkCmdCopyBuffer(cmd.getVkCommandBuffer(), stgVulkanBuffer.getBuffer(), dstBuffer.getBuffer(), copyRegion);
        }
    }

    public void recordTransferCommand(CommandBuffer cmd, VulkanBuffer dstBuffer) {
        recordTransferCommand(cmd, dstBuffer, 0, stgVulkanBuffer.getRequestedSize());
    }
}
