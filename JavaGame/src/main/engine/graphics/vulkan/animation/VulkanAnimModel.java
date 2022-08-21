package main.engine.graphics.vulkan.animation;

import java.util.ArrayList;
import java.util.List;

import main.engine.graphics.vulkan.VulkanModel;
import main.engine.items.GameItem;

public class VulkanAnimModel {
	private GameItem gameitem;
    private List<VulkanAnimMesh> vulkanAnimMeshList;
    private VulkanModel vulkanModel;

    public VulkanAnimModel(GameItem gameitem, VulkanModel vulkanModel) {
        this.gameitem = gameitem;
        this.vulkanModel = vulkanModel;
        vulkanAnimMeshList = new ArrayList<>();
    }

    public GameItem getGameItem() {
        return gameitem;
    }

    public List<VulkanAnimMesh> getVulkanAnimMeshList() {
        return vulkanAnimMeshList;
    }

    public VulkanModel getVulkanModel() {
        return vulkanModel;
    }

    public record VulkanAnimMesh(int meshOffset, VulkanModel.VulkanMesh vulkanMesh) {}
}
