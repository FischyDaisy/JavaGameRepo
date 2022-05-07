package main.engine.graphics.vulkan.lighting;

import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import main.engine.graphics.vulkan.VertexInputStateInfo;

import static org.lwjgl.vulkan.VK11.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;

public class EmptyVertexBufferStructure extends VertexInputStateInfo {

    public EmptyVertexBufferStructure() {
        vi = VkPipelineVertexInputStateCreateInfo.calloc();
        vi.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                .pVertexBindingDescriptions(null)
                .pVertexAttributeDescriptions(null);
    }
}