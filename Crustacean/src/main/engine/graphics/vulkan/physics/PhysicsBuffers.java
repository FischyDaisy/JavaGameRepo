package main.engine.graphics.vulkan.physics;

import crab.newton.NewtonBody;
import crab.newton.NewtonMesh;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import main.engine.EngineProperties;
import main.engine.enginelayouts.Vector3fLayout;
import main.engine.graphics.vulkan.*;
import org.tinylog.Logger;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;
import java.util.Iterator;

import static org.lwjgl.vulkan.VK10.*;
import static crab.newton.Newton.*;

public class PhysicsBuffers {
    private final VulkanBuffer indicesBuffer;
    private final long indexBufferSize;
    private final VulkanBuffer verticesBuffer;
    private final long vertexBufferSize;
    private VulkanBuffer indirectBuffer;
    private VulkanBuffer[] instanceDataBuffers;
    private int numIndirectCommands;

    public PhysicsBuffers(Device device) {
        Logger.debug("Creating physics buffers");
        EngineProperties engProps = EngineProperties.INSTANCE;
        vertexBufferSize = engProps.getMaxPhysicsVerticesBuffer();
        verticesBuffer = new VulkanBuffer(device, vertexBufferSize, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT |
                VK_BUFFER_USAGE_TRANSFER_DST_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
        indexBufferSize = engProps.getMaxPhysicsIndicesBuffer();
        indicesBuffer = new VulkanBuffer(device, indexBufferSize, VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, 0);
    }

    public void cleanup() {
        verticesBuffer.cleanup();
        indicesBuffer.cleanup();
        if (indirectBuffer != null) {
            indirectBuffer.cleanup();
        }
        if (instanceDataBuffers != null) {
            Arrays.stream(instanceDataBuffers).forEach(VulkanBuffer::cleanup);
        }
    }

    public VulkanBuffer getIndicesBuffer() {
        return indicesBuffer;
    }

    public VulkanBuffer getIndirectBuffer() {
        return indirectBuffer;
    }

    public VulkanBuffer[] getInstanceDataBuffers() {
        return instanceDataBuffers;
    }

    public int getNumIndirectCommands() {
        return numIndirectCommands;
    }

    public VulkanBuffer getVerticesBuffer() {
        return verticesBuffer;
    }

    public void loadNewtonModels(Dominion dominion, CommandPool commandPool, Queue queue) {
        Device device = commandPool.getDevice();
        CommandBuffer cmd = new CommandBuffer(commandPool, true, true);

        StagingBuffer verticesStgBuffer = new StagingBuffer(device, verticesBuffer.getRequestedSize());
        StagingBuffer indicesStgBuffer = new StagingBuffer(device, indicesBuffer.getRequestedSize());
        MemorySegment vertexData = verticesStgBuffer.getDataSegment();
        MemorySegment indicesData = indicesStgBuffer.getDataSegment();

        cmd.beginRecording();

        long vertexOffset = 0, indexOffset = 0;
        Results<Results.With1<NewtonMesh>> results = dominion.findEntitiesWith(NewtonMesh.class);
        for (Iterator<Results.With1<NewtonMesh>> itr = results.iterator(); itr.hasNext();) {
            Results.With1<NewtonMesh> result = itr.next();
            NewtonMesh mesh = result.comp();
            PhysicsModel physicsModel = new PhysicsModel(mesh);
            long vertexSize = VEC3F.byteSize() * mesh.getPointCount();
            mesh.getVertexChannel((int) VEC3F.byteSize(), vertexData.asSlice(vertexOffset));
            MemorySegment geometryHandle = mesh.beginHandle();
            int totalIndexCount = 0;
            long offset = indexOffset;
            for (int handle = mesh.firstMaterial(geometryHandle); handle != -1; handle = mesh.nextMaterial(geometryHandle, handle)) {
                mesh.materialGetIndexStream(geometryHandle, handle, indicesData.asSlice(offset));
                int indexCount = mesh.materialGetIndexCount(geometryHandle, handle);
                totalIndexCount += indexCount;
                offset += ValueLayout.JAVA_INT.byteSize() * indexCount;
            }
            physicsModel.addPhysicsMesh(new PhysicsModel.PhysicsMesh((int) vertexSize, totalIndexCount, (int) vertexOffset, (int) indexOffset));
            vertexOffset += vertexSize;
            indexOffset = offset;
            result.entity().add(physicsModel);
        }

        verticesStgBuffer.recordTransferCommand(cmd, verticesBuffer);
        indicesStgBuffer.recordTransferCommand(cmd, indicesBuffer);
        cmd.endRecording();

        cmd.submitAndWait(device, queue);
        cmd.cleanup();

        verticesStgBuffer.cleanup();
        indicesStgBuffer.cleanup();
    }
}
