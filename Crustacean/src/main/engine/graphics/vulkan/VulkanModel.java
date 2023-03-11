package main.engine.graphics.vulkan;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.*;
import org.lwjgl.vulkan.VkBufferCopy;

import main.engine.graphics.GraphConstants;
import main.engine.graphics.ModelData;

import java.nio.*;
import java.util.*;

import static org.lwjgl.vulkan.VK11.*;

public class VulkanModel {

	public final String modelId;
    public final List<VulkanAnimationData> vulkanAnimationDataList;
    public final List<VulkanMesh> vulkanMeshList;

    public VulkanModel(String modelId) {
        this.modelId = modelId;
        vulkanMeshList = new ArrayList<>();
        vulkanAnimationDataList = new ArrayList<>();
    }

    public void addVulkanAnimationData(VulkanAnimationData vulkanAnimationData) {
        vulkanAnimationDataList.add(vulkanAnimationData);
    }

    public void addVulkanMesh(VulkanMesh vulkanMesh) {
        vulkanMeshList.add(vulkanMesh);
    }

    public boolean hasAnimations() {
        return !vulkanAnimationDataList.isEmpty();
    }

    public static class VulkanAnimationData {
        private List<VulkanAnimationFrame> vulkanAnimationFrameList;

        public VulkanAnimationData() {
            vulkanAnimationFrameList = new ArrayList<>();
        }

        public void addVulkanAnimationFrame(VulkanAnimationFrame vulkanAnimationFrame) {
            vulkanAnimationFrameList.add(vulkanAnimationFrame);
        }

        public List<VulkanAnimationFrame> getVulkanAnimationFrameList() {
            return vulkanAnimationFrameList;
        }
    }

    public record VulkanAnimationFrame(int jointMatricesOffset) {}

    public record VulkanMaterial(int globalMaterialIdx) {}

    public record VulkanMesh(int verticesSize, int numIndices, int verticesOffset, int indicesOffset,
                             int globalMaterialIdx, int weightsOffset) {}
}