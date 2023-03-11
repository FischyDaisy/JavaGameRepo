package main.engine.graphics.vulkan.physics;

import crab.newton.NewtonMesh;

import java.util.ArrayList;
import java.util.List;

public class PhysicsModel {

    public final NewtonMesh mesh;
    public final List<PhysicsMesh> physicsMeshList;

    public PhysicsModel(NewtonMesh mesh) {
        this.mesh = mesh;
        physicsMeshList = new ArrayList<>();
    }

    public void addPhysicsMesh(PhysicsMesh physicsMesh) {
        physicsMeshList.add(physicsMesh);
    }

    public record PhysicsMesh(int verticesSize, int numIndices, int verticesOffset, int indicesOffset) {}
}
