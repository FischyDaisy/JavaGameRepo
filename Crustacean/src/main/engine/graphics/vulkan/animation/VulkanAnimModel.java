package main.engine.graphics.vulkan.animation;

import java.util.ArrayList;
import java.util.List;

import main.engine.graphics.vulkan.VulkanModel;
import main.engine.items.GameItem;

public class VulkanAnimModel {
	public final GameItem gameitem;
    public final List<VulkanAnimMesh> vulkanAnimMeshList;
    public final VulkanModel vulkanModel;

    public VulkanAnimModel(GameItem gameitem, VulkanModel vulkanModel) {
        this.gameitem = gameitem;
        this.vulkanModel = vulkanModel;
        vulkanAnimMeshList = new ArrayList<>();
    }

    public record VulkanAnimMesh(int meshOffset, VulkanModel.VulkanMesh vulkanMesh) {}
}
