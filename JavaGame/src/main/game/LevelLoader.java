package main.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joml.Matrix4f;
import org.tinylog.Logger;

import jdk.incubator.foreign.*;
import crab.newton.NewtonBody;
import crab.newton.NewtonBox;
import crab.newton.NewtonCollision;
import crab.newton.NewtonDynamicBody;
import crab.newton.NewtonMesh;
import crab.newton.NewtonWorld;
import main.engine.Scene;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.items.GameItem;
import main.engine.loaders.assimp.ModelLoader;
import main.engine.physics.PhysUtils;
import main.engine.utility.AxisRotation;
import main.engine.utility.ResourcePaths;

public class LevelLoader {
	
	public enum Levels {NEWTONPLAYGROUND, SPONZA};
	
	private LevelLoader() {
	}
	
	public static void loadLevel(Levels level, Scene scene, VKRenderer renderer, NewtonWorld world, ResourceScope scope) throws Exception {
		switch (level) {
			case NEWTONPLAYGROUND -> {loadNewtonPlayground(scene, renderer, world, scope);}
			case SPONZA -> {loadSponza(scene, renderer);}
		}
	}
	
	private static void loadNewtonPlayground(Scene scene, VKRenderer renderer, NewtonWorld world, ResourceScope scope) throws Exception {
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
    	cubeBody.setForceAndTorqueCallback((bodyPtr, timestep, threadIndex) -> {
    		NewtonBody body = NewtonBody.wrap(bodyPtr);
    		float[] mass = body.getMass();
    		float[] newMass = new float[] {0f, mass[0] * -1.0f, 0f};
    		body.setForce(newMass);
    	}, scope);
    	newtonCube.setBody(cubeBody);
    	Matrix4f matrix = new Matrix4f();
    	matrix.identity();
    	matrix.transpose();
    	matrix.get(matArr);
        mesh.applyBoxMapping(0, 0, 0, matArr);
        List<ModelData.Material> newtonMaterials = new ArrayList<ModelData.Material>();
        newtonMaterials.add(newtonMaterial);
        ModelData newtonCubeData = PhysUtils.convertToModelData(mesh, newtonModelId, newtonMaterials);
        modelDataList.add(newtonCubeData);
        scene.addGameItem(newtonCube);
        boxCollision.destroy();
        mesh.destroy();

        renderer.loadModels(modelDataList);
	}
	
	private static void loadSponza(Scene scene, VKRenderer renderer) throws Exception {
		List<ModelData> modelDataList = new ArrayList<>();

    	String sponzaModelId = "sponza-model";
        ModelData sponzaModelData = ModelLoader.loadModel(sponzaModelId, ResourcePaths.Models.SPONZA_GLTF,
                ResourcePaths.Models.SPONZA_DIR, false);
        modelDataList.add(sponzaModelData);
        GameItem sponza = new GameItem("SponzaObject", sponzaModelId);
        scene.addGameItem(sponza);
        
        String bobModelId = "bob-model";
        ModelData bobModelData = ModelLoader.loadModel(bobModelId, ResourcePaths.Models.BOBLAMP_MD5MESH,
        		ResourcePaths.Models.BOBLAMP_DIR, true);
        int maxFrames = bobModelData.getAnimationsList().get(0).frames().size();
        modelDataList.add(bobModelData);
        GameItem bob = new GameItem("BobObject", bobModelId);
        bob.setScale(0.04f);
        AxisRotation rot = AxisRotation.UP;
        rot.setRotation((float) Math.toRadians(-90.0f));
        bob.setRotation(rot.getQuatRotation());
        bob.buildModelMatrix();
        bob.setGameItemAnimation(new GameItem.GameItemAnimation(false, 0, 0, maxFrames));
        scene.addGameItem(bob);
        
        String monsterModelId = "monster-model";
        ModelData monsterModelData = ModelLoader.loadModel(monsterModelId, ResourcePaths.Models.MONSTER_MD5MESH, 
        		ResourcePaths.Models.MONSTER_DIR, true);
        int monsterMax = monsterModelData.getAnimationsList().get(0).frames().size();
        modelDataList.add(monsterModelData);
        GameItem monster = new GameItem("MonsterObject", monsterModelId);
        monster.setScale(0.02f);
        //rot.setRotation((float) Math.toRadians(-90.0f));
        //monster.setRotation(rot.getQuatRotation());
        monster.setPosition(-5f, 0f, 0f);
        monster.buildModelMatrix();
        monster.setGameItemAnimation(new GameItem.GameItemAnimation(false, 0, 0, monsterMax));
        scene.addGameItem(monster);
        
        renderer.loadModels(modelDataList);
        renderer.loadAnimation(bob);
        renderer.loadAnimation(monster);
	}
}
