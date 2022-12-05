package main.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.engine.graphics.camera.Camera;
import org.joml.Matrix4f;
import org.tinylog.Logger;

import crab.newton.NewtonBody;
import crab.newton.NewtonBox;
import crab.newton.NewtonCollision;
import crab.newton.NewtonDynamicBody;
import crab.newton.NewtonMesh;
import crab.newton.NewtonWorld;
import java.lang.foreign.*;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.items.GameItem;
import main.engine.physics.HeightMap;
import main.engine.physics.PhysUtils;
import main.engine.physics.Physics;
import main.engine.scene.Level;
import main.engine.scene.Scene;
import main.engine.utility.ResourcePaths;

public class NewtonDemo implements Level {

    private final List<ModelData> modelDataList;
    private final List<GameItem> gameItems;
    private final GameItem heightField;

    public NewtonDemo(NewtonWorld world, Physics physics, MemorySession session) throws Exception {
        modelDataList = new ArrayList<>();
        gameItems = new ArrayList<>();

        float[] offsetMatrix = new float[16];
        float[] matArr = new float[16];
        Matrix4f matrix = new Matrix4f();
        matrix.get(offsetMatrix);
        float[] params = new float[] {1f, 1f, 1f, 1f};
        float z = 0f;
        GameItem primitiveItem;
        for (Physics.CollisionPrimitive primitive : Physics.CollisionPrimitive.values()) {
            String newtonModelId = "newton-" + primitive.getName();
            String gameItemId = primitive.getName() + "-item";
            primitiveItem = physics.createPrimitiveCollision(world, modelDataList, newtonModelId, gameItemId, primitive, params, offsetMatrix, 10f, session);
            primitiveItem.setPosition(-2.5f, 2.5f, z);
            z += 5;
            matrix.set(primitiveItem.buildModelMatrix());
            matrix.get(matArr);
            primitiveItem.getBody().setMatrix(matArr);
            gameItems.add(primitiveItem);
        }

        String heightModelId = "height-field";
        heightField = new GameItem("HeightFieldItem", heightModelId);
        heightField.setPosition(-100f, 100f, -100f);
        HeightMap.HeightMapData heightMapData = HeightMap.createHeightMap(world, heightModelId, 8, -50f, 200f, session);
        Matrix4f heightFieldMat = heightField.buildModelMatrix();
        heightFieldMat.get(matArr);
        NewtonBody heightFieldBody = NewtonDynamicBody.create(world, heightMapData.collision(), matArr, session);
        heightField.setBody(heightFieldBody);
        ModelData heightFieldModel = heightMapData.modeldata();
        modelDataList.add(heightFieldModel);
        heightMapData.collision().destroy();
    }

    public void cleanup() {

    }

    public List<GameItem> getGameItems() {
        return gameItems;
    }

	@Override
	public void load(Scene scene, VKRenderer renderer, NewtonWorld world, Physics physics, MemorySession session) throws Exception {
        for (GameItem item : gameItems) {
            scene.addGameItem(item);
        }
        scene.addGameItem(heightField);
        Camera camera = scene.getCamera();
        camera.setPosition(-6.0f, 2.0f, 0.0f);
        camera.setRotationEuler((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f), 0.0f);
        camera.updateQuat();

        renderer.loadModels(modelDataList, scene);
	}

    @Override
    public void reset(Scene scene, VKRenderer renderer, NewtonWorld world, Physics physics, MemorySession session) throws Exception {
        float z = 0f;
        Matrix4f matrix = new Matrix4f();
        float[] matArr = new float[16];
        for (GameItem item : gameItems) {
            item.setPosition(-2.5f, 2.5f, z);
            z += 5;
            matrix.set(item.buildModelMatrix());
            matrix.get(matArr);
            item.getBody().setMatrix(matArr);
        }

        Camera camera = scene.getCamera();
        camera.setPosition(-6.0f, 2.0f, 0.0f);
        camera.setRotationEuler((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f), 0.0f);
        camera.updateQuat();
    }
}
