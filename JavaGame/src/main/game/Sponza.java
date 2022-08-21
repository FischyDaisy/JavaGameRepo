package main.game;

import java.util.ArrayList;
import java.util.List;

import crab.newton.NewtonWorld;
import jdk.incubator.foreign.ResourceScope;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.items.GameItem;
import main.engine.loaders.assimp.ModelLoader;
import main.engine.physics.Physics;
import main.engine.scene.Level;
import main.engine.scene.Scene;
import main.engine.utility.AxisRotation;
import main.engine.utility.ResourcePaths;

public class Sponza implements Level {

	@Override
	public void load(Scene scene, VKRenderer renderer, NewtonWorld world, Physics physics, ResourceScope scope) throws Exception {
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
        
        renderer.loadModels(modelDataList, scene);
	}
}
