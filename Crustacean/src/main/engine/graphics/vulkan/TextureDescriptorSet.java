package main.engine.graphics.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.vulkan.VK11.*;

public class TextureDescriptorSet extends DescriptorSet {
	
	public TextureDescriptorSet(DescriptorPool descriptorPool, DescriptorSetLayout descriptorSetLayout,
            VKTexture texture, TextureSampler textureSampler, int binding) {
		this(descriptorPool, descriptorSetLayout, Arrays.asList(texture), textureSampler, binding);
	}

    public TextureDescriptorSet(DescriptorPool descriptorPool, DescriptorSetLayout descriptorSetLayout,
    		List<VKTexture> textureList, TextureSampler textureSampler, int binding) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Device device = descriptorPool.getDevice();
            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            pDescriptorSetLayout.put(0, descriptorSetLayout.getVkDescriptorLayout());
            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                    .descriptorPool(descriptorPool.getVkDescriptorPool())
                    .pSetLayouts(pDescriptorSetLayout);

            LongBuffer pDescriptorSet = stack.mallocLong(1);
            VulkanUtils.vkCheck(vkAllocateDescriptorSets(device.getVkDevice(), allocInfo, pDescriptorSet),
                    "Failed to create descriptor set");
            vkDescriptorSet = pDescriptorSet.get(0);

            int numImages = textureList.size();
            VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(numImages, stack);
            for (int i = 0; i < numImages; i++) {
                VKTexture texture = textureList.get(i);
                imageInfo.get(i)
                        .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                        .imageView(texture.getImageView().getVkImageView())
                        .sampler(textureSampler.getVkSampler());
            }

            VkWriteDescriptorSet.Buffer descrBuffer = VkWriteDescriptorSet.calloc(1, stack);
            descrBuffer.get(0)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstSet(vkDescriptorSet)
                    .dstBinding(binding)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(numImages)
                    .pImageInfo(imageInfo);

            vkUpdateDescriptorSets(device.getVkDevice(), descrBuffer, null);
        }
    }
    
    public void update(Device device, VKTexture texture, TextureSampler textureSampler, int binding) {
    	try (MemoryStack stack = MemoryStack.stackPush()) {
    		VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack)
                    .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                    .imageView(texture.getImageView().getVkImageView())
                    .sampler(textureSampler.getVkSampler());

            VkWriteDescriptorSet.Buffer descrBuffer = VkWriteDescriptorSet.calloc(1, stack);
            descrBuffer.get(0)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstSet(vkDescriptorSet)
                    .dstBinding(binding)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .pImageInfo(imageInfo);

            vkUpdateDescriptorSets(device.getVkDevice(), descrBuffer, null);
    	}
    }
    
    public void update(Device device, long vkImageView, TextureSampler textureSampler, int binding) {
    	try (MemoryStack stack = MemoryStack.stackPush()) {
    		VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack)
                    .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                    .imageView(vkImageView)
                    .sampler(textureSampler.getVkSampler());

            VkWriteDescriptorSet.Buffer descrBuffer = VkWriteDescriptorSet.calloc(1, stack);
            descrBuffer.get(0)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstSet(vkDescriptorSet)
                    .dstBinding(binding)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .pImageInfo(imageInfo);

            vkUpdateDescriptorSets(device.getVkDevice(), descrBuffer, null);
    	}
    }
}