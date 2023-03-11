package main.engine.graphics.vulkan;

import org.lwjgl.vulkan.*;

import main.engine.graphics.GraphConstants;

import java.lang.foreign.*;

import static java.lang.foreign.ValueLayout.JAVA_FLOAT;
import static org.lwjgl.vulkan.VK11.*;

public class VertexBufferStructure extends VertexInputStateInfo {

	public static final int TEXT_COORD_COMPONENTS = 2;
	private static final int NORMAL_COMPONENTS = 3;
    private static final int NUMBER_OF_ATTRIBUTES = 5;
    private static final int POSITION_COMPONENTS = 3;
    public static final int SIZE_IN_BYTES = (POSITION_COMPONENTS + NORMAL_COMPONENTS * 3 + TEXT_COORD_COMPONENTS) * GraphConstants.FLOAT_SIZE_BYTES;
    public static final GroupLayout VertexLayout = MemoryLayout.structLayout(
            JAVA_FLOAT.withName("PositionX"), JAVA_FLOAT.withName("PositionY"), JAVA_FLOAT.withName("PositionZ"),
            JAVA_FLOAT.withName("NormalX"), JAVA_FLOAT.withName("NormalY"), JAVA_FLOAT.withName("NormalZ"),
            JAVA_FLOAT.withName("TangentX"), JAVA_FLOAT.withName("TangentY"), JAVA_FLOAT.withName("TangentZ"),
            JAVA_FLOAT.withName("BiTangentX"), JAVA_FLOAT.withName("BiTangentX"), JAVA_FLOAT.withName("BiTangentX"),
            JAVA_FLOAT.withName("TextCoordX"), JAVA_FLOAT.withName("TextCoordY")
    );
    private final VkVertexInputAttributeDescription.Buffer viAttrs;
    private final VkVertexInputBindingDescription.Buffer viBindings;

    public VertexBufferStructure() {
        viAttrs = VkVertexInputAttributeDescription.calloc(NUMBER_OF_ATTRIBUTES);
        viBindings = VkVertexInputBindingDescription.calloc(1);
        vi = VkPipelineVertexInputStateCreateInfo.calloc();

        int i = 0;
        // Position
        viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(0);
        
        // Normal
        i++;
        viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(POSITION_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES);

        // Tangent
        i++;
        viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(NORMAL_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES + POSITION_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES);

        // BiTangent
        i++;
        viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(NORMAL_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES * 2 + POSITION_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES);

        // Texture coordinates
        i++;
        viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32_SFLOAT)
                .offset(NORMAL_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES * 3 + POSITION_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES);

        viBindings.get(0)
                .binding(0)
                .stride(POSITION_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES +
                        NORMAL_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES * 3 +
                        TEXT_COORD_COMPONENTS * GraphConstants.FLOAT_SIZE_BYTES)
                .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

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