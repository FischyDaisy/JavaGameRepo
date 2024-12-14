package main.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import main.engine.ItemLoadTimestamp;
import main.engine.graphics.camera.Camera;
import main.engine.scene.Scene;
import org.joml.Matrix4f;

import crab.newton.NewtonBody;
import java.lang.foreign.*;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.items.GameItem;
import main.engine.physics.HeightMap;
import main.engine.physics.Physics;
import main.engine.scene.Level;

public class NewtonDemo implements Level {

    //private final List<ModelData> modelDataList;
    //public final List<GameItem> gameItems;
    //public final List<NewtonBody> bodies;
    //private final GameItem heightField;
    //private final NewtonBody heightFieldBody;

    public NewtonDemo(Physics physics, Arena arena) throws Throwable {
        /*
        modelDataList = new ArrayList<>();
        gameItems = new ArrayList<>();
        bodies = new ArrayList<>();

        float[] offsetMatrix = new float[16];
        float[] matArr = new float[16];
        Matrix4f matrix = new Matrix4f();
        matrix.get(offsetMatrix);
        float[] params = new float[] {1f, 1f, 1f, 1f};
        float z = 0f;
        for (Physics.CollisionPrimitive primitive : Physics.CollisionPrimitive.values()) {
            String newtonModelId = "newton-" + primitive.getName();
            GameItem primitiveItem = new GameItem(newtonModelId);
            NewtonBody body = physics.createPrimitiveCollision(modelDataList, newtonModelId, primitive, params, offsetMatrix, 10f, arena);
            primitiveItem.setPosition(-2.5f, 2.5f, z);
            z += 5;
            primitiveItem.buildModelMatrix().get(matArr);
            body.setMatrix(matArr);
            gameItems.add(primitiveItem);
            bodies.add(body);
        }

        String heightModelId = "height-field";
        heightField = new GameItem(heightModelId);
        heightField.setPosition(-100f, 100f, -100f);
        HeightMap.HeightMapData heightMapData = HeightMap.createHeightMap(physics.getWorld(), heightModelId, 8, -50f, 200f, arena);
        Matrix4f heightFieldMat = heightField.buildModelMatrix();
        heightFieldMat.get(matArr);
        heightFieldBody = physics.getWorld().createDynamicBody(heightMapData.collision(), matArr);
        ModelData heightFieldModel = heightMapData.modeldata();
        modelDataList.add(heightFieldModel);
        heightMapData.collision().destroy();
        */
    }

    public void cleanup() {
    }

	@Override
	public void load(Scene scene, VKRenderer renderer, Physics physics) throws Exception {
        int size = gameItems.size();
        for (int i = 0; i < size; i++) {
            dominion.createEntity(gameItems.get(i), bodies.get(i));
        }
        dominion.createEntity(heightField, heightFieldBody);
        ItemLoadTimestamp stamp = ItemLoadTimestamp.getTimeStamp(dominion);
        stamp.gameItemLoadedTimestamp = System.currentTimeMillis();
        Camera camera = renderer.getCamera();
        camera.setPosition(-6.0f, 2.0f, 0.0f);
        camera.setRotationEuler((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f), 0.0f);
        camera.updateQuat();

        for (ModelData modelData : modelDataList) {
            System.out.println(modelData.toString());
            dominion.createEntity(modelData);
        }

        renderer.loadModels();
	}

    @Override
    public void reset(Scene scene, VKRenderer renderer, Physics physics) throws Exception {
        float z = 0f;
        float[] matArr = new float[16];
        Results<Results.With2<GameItem, NewtonBody>> results = dominion.findEntitiesWith(GameItem.class, NewtonBody.class);
        for (Iterator<Results.With2<GameItem, NewtonBody>> itr = results.iterator(); itr.hasNext();) {
            Results.With2<GameItem, NewtonBody> result = itr.next();
            GameItem item = result.comp1();
            NewtonBody body = result.comp2();
            if (item.modelId().equals("height-field")) {
                continue;
            }
            item.setPosition(-2.5f, 2.5f, z);
            z += 5;
            item.buildModelMatrix().get(matArr);
            body.setMatrix(matArr);
        }

        Camera camera = renderer.getCamera();
        camera.setPosition(-6.0f, 2.0f, 0.0f);
        camera.setRotationEuler((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f), 0.0f);
        camera.updateQuat();
    }
}
