package main.engine.graphics.vulkan.physics;

import main.engine.graphics.GraphConstants;
import main.engine.graphics.vulkan.VertexInputStateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

public class PhysicsVertexBufferStructure extends VertexInputStateInfo {

    private static final int NUMBER_OF_ATTRIBUTES = 5;
    private static final int POSITION_COMPONENTS = 3;
    public static final int SIZE_IN_BYTES = POSITION_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES;
    private final VkVertexInputAttributeDescription.Buffer viAttrs;
    private final VkVertexInputBindingDescription.Buffer viBindings;

    public PhysicsVertexBufferStructure() {
        viAttrs = VkVertexInputAttributeDescription.calloc(NUMBER_OF_ATTRIBUTES);
        viBindings = VkVertexInputBindingDescription.calloc(2);
        vi = VkPipelineVertexInputStateCreateInfo.calloc();

        int i = 0;
        // Position
        viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(0);
        // Model Matrix as a set of 4 Vectors
        i++;
        for (int j = 0; j < 4; j++) {
            viAttrs.get(i)
                    .binding(1)
                    .location(i)
                    .format(VK_FORMAT_R32G32B32A32_SFLOAT)
                    .offset(j * GraphConstants.VECTOR4F_SIZE_BYTES);
            i++;
        }

        // Non instanced data
        viBindings.get(0)
                .binding(0)
                .stride(SIZE_IN_BYTES)
                .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        // Instanced data
        viBindings.get(1)
                .binding(1)
                .stride(GraphConstants.MAT4X4_SIZE_BYTES)
                .inputRate(VK_VERTEX_INPUT_RATE_INSTANCE);

        vi
                .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                .pVertexBindingDescriptions(viBindings)
                .pVertexAttributeDescriptions(viAttrs);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        viBindings.free();
        viAttrs.free();
    }
}
