package main.engine.graphics.vulkan.geometry;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import java.nio.ByteBuffer;
import main.engine.EngineProperties;
import main.engine.graphics.GraphConstants;

public class GeometrySpecConstants {
	
	private final ByteBuffer data;
    private final VkSpecializationMapEntry.Buffer specEntryMap;
    private final VkSpecializationInfo specInfo;

    public GeometrySpecConstants() {
        EngineProperties engineProperties = EngineProperties.INSTANCE;
        data = MemoryUtil.memAlloc(GraphConstants.INT_SIZE_BYTES);
        data.putInt(engineProperties.getMaxTextures());
        data.flip();

        specEntryMap = VkSpecializationMapEntry.calloc(1);
        specEntryMap.get(0)
                .constantID(0)
                .size(GraphConstants.INT_SIZE_BYTES)
                .offset(0);

        specInfo = VkSpecializationInfo.calloc();
        specInfo.pData(data)
                .pMapEntries(specEntryMap);
    }

    public void cleanup() {
        MemoryUtil.memFree(specEntryMap);
        specInfo.free();
        MemoryUtil.memFree(data);
    }

    public VkSpecializationInfo getSpecInfo() {
        return specInfo;
    }
}
