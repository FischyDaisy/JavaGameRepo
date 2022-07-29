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
	public void load(Scene scene, VKRenderer renderer, NewtonWorld world, ResourceScope scope) throws Exception {
		List<ModelData> modelDataList = new ArrayList<>();
		
		String newtonModelId = "newton-cube";
        GameItem newtonCube = new GameItem("Cubby", newtonModelId);
        newtonCube.setPosition(-2.5f, 2.5f, 0f);
        //newtonCube.buildModelMatrix();
        ModelData.Material newtonMaterial = new ModelData.Material(ResourcePaths.Textures.THIS_PIC_GOES_HARD);
        SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
        NewtonCollision boxCollision = NewtonBox.create(world, 1f, 1f, 1f, 0, null, allocator);
        NewtonMesh mesh = NewtonMesh.createFromCollision(boxCollision);
        Matrix4f cubeMatrix = newtonCube.buildModelMatrix();
    	float[] matArr = new float[16];
    	//cubeMatrix.transpose();
    	cubeMatrix.get(matArr);
    	//cubeMatrix.transpose();
    	NewtonBody cubeBody = NewtonDynamicBody.create(world, boxCollision, matArr, allocator);
    	Logger.debug("GameItem Matrix Array: {}", Arrays.toString(matArr));
    	Logger.debug("Newton Body Initial Position: {}", Arrays.toString(cubeBody.getPosition()));
    	float[] inertiaOrigin = boxCollision.calculateInertiaMatrix();
    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
    	cubeBody.setMassMatrix(0.25f, 0.25f * inertiaOrigin[0], 0.25f * inertiaOrigin[1], 0.25f * inertiaOrigin[2]);
    	cubeBody.setCenterOfMass(origin);
    	cubeBody.setForceAndTorqueCallback(Physics::applyGravity, scope);
    	newtonCube.setBody(cubeBody);
    	Matrix4f matrix = new Matrix4f();
    	matrix.identity();
    	matrix.get(matArr);
        mesh.applyBoxMapping(0, 0, 0, matArr);
        List<ModelData.Material> newtonMaterials = new ArrayList<ModelData.Material>();
        newtonMaterials.add(newtonMaterial);
        ModelData newtonCubeData = PhysUtils.convertToModelData(mesh, newtonModelId, newtonMaterials);
        modelDataList.add(newtonCubeData);
        scene.addGameItem(newtonCube);
        boxCollision.destroy();
        mesh.destroy();
        
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

        renderer.loadModels(modelDataList);
	}
}
