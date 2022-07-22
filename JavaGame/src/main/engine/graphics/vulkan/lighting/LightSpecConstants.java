package main.engine.graphics.vulkan.lighting;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import main.engine.EngineProperties;
import main.engine.graphics.GraphConstants;

import java.nio.ByteBuffer;

public class LightSpecConstants {

    private final ByteBuffer data;
    private final VkSpecializationMapEntry.Buffer specEntryMap;
    private final VkSpecializationInfo specInfo;

    public LightSpecConstants() {
    	EngineProperties engineProperties = EngineProperties.INSTANCE;
        data = MemoryUtil.memAlloc(GraphConstants.INT_SIZE_BYTES * 4 + GraphConstants.FLOAT_SIZE_BYTES);
        data.putInt(GraphConstants.MAX_LIGHTS);
        data.putInt(GraphConstants.SHADOW_MAP_CASCADE_COUNT);
        data.putInt(engineProperties.isShadowPcf() ? 1 : 0);
        data.putFloat(engineProperties.getShadowBias());
        data.putInt(engineProperties.isShadowDebug() ? 1 : 0);
        data.flip();

        specEntryMap = VkSpecializationMapEntry.calloc(5);
        specEntryMap.get(0)
                .constantID(0)
                .size(GraphConstants.INT_SIZE_BYTES)
                .offset(0);
        specEntryMap.get(1)
                .constantID(1)
                .size(GraphConstants.INT_SIZE_BYTES)
                .offset(GraphConstants.INT_SIZE_BYTES);
        specEntryMap.get(2)
                .constantID(2)
                .size(GraphConstants.INT_SIZE_BYTES)
                .offset(GraphConstants.INT_SIZE_BYTES * 2);
        specEntryMap.get(3)
                .constantID(3)
                .size(GraphConstants.FLOAT_SIZE_BYTES)
                .offset(GraphConstants.INT_SIZE_BYTES * 3);
        specEntryMap.get(4)
                .constantID(4)
                .size(GraphConstants.INT_SIZE_BYTES)
                .offset(GraphConstants.INT_SIZE_BYTES * 3 + GraphConstants.FLOAT_SIZE_BYTES);

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