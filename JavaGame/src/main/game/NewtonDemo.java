package main.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joml.Matrix4f;
import org.tinylog.Logger;

import crab.newton.NewtonBody;
import crab.newton.NewtonBox;
import crab.newton.NewtonCollision;
import crab.newton.NewtonDynamicBody;
import crab.newton.NewtonMesh;
import crab.newton.NewtonWorld;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;
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

	@Override
	public void load(Scene scene, VKRenderer renderer, NewtonWorld world, Physics physics, ResourceScope scope) throws Exception {
		List<ModelData> modelDataList = new ArrayList<>();
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		
		
		float[] offsetMatrix = new float[16];
		float[] matArr = new float[16];
		Matrix4f matrix = new Matrix4f();
		matrix.get(offsetMatrix);
		float[] params = new float[] {1f, 1f, 1f, 1f};
        GameItem primitiveItem;
        float z = 0f;
        /*
        for (Physics.CollisionPrimitive primitive : Physics.CollisionPrimitive.values()) {
        	String newtonModelId = "newton-" + primitive.getName();
    		String gameItemId = primitive.getName() + "-item";
    		primitiveItem = Physics.createPrimitiveCollision(world, physics, scene, modelDataList, newtonModelId, gameItemId, primitive, params, offsetMatrix, 10f, scope);
    		primitiveItem.setPosition(-2.5f, 2.5f, z);
    		z += 5;
            matrix.set(primitiveItem.buildModelMatrix());
            matrix.get(matArr);
            primitiveItem.getBody().setMatrix(matArr);
        }
        
        
        String heightModelId = "height-field";
        GameItem heightField = new GameItem("HeightFieldItem", heightModelId);
        heightField.setPosition(-100f, 100f, -100f);
        HeightMap.HeightMapData heightMapData = HeightMap.createHeightMap(world, heightModelId, 8, -50f, 200f, allocator);
        Matrix4f heightFieldMat = heightField.buildModelMatrix();
        heightFieldMat.get(matArr);
        NewtonBody heightFieldBody = NewtonDynamicBody.create(world, heightMapData.collision(), matArr, allocator);
        heightField.setBody(heightFieldBody);
        ModelData heightFieldModel = heightMapData.modeldata();
        modelDataList.add(heightFieldModel);
        scene.addGameItem(heightField);
        heightMapData.collision().destroy();
*/
        renderer.loadModels(modelDataList, scene);
	}
}
