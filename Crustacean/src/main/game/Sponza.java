package main.game;

import java.util.ArrayList;
import java.util.List;

import crab.newton.NewtonWorld;
import java.lang.foreign.*;
import main.engine.graphics.ModelData;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.items.GameItem;
import main.engine.items.GameItemAnimation;
import main.engine.loaders.assimp.ModelLoader;
import main.engine.physics.Physics;
import main.engine.scene.Level;
import main.engine.scene.Scene;
import main.engine.utility.AxisRotation;
import main.engine.utility.ResourcePaths;

public class Sponza implements Level {

    private final List<ModelData> modelDataList;
    private final GameItem sponza;
    private final GameItem bob;
    private final GameItem monster;

    public Sponza() {
        modelDataList = new ArrayList<>();

        String sponzaModelId = "sponza-model";
        ModelData sponzaModelData = ModelLoader.loadModel(sponzaModelId, ResourcePaths.Models.SPONZA_GLTF,
                ResourcePaths.Models.SPONZA_DIR, false);
        modelDataList.add(sponzaModelData);
        sponza = new GameItem("SponzaObject", sponzaModelId);

        String bobModelId = "bob-model";
        ModelData bobModelData = ModelLoader.loadModel(bobModelId, ResourcePaths.Models.BOBLAMP_MD5MESH,
                ResourcePaths.Models.BOBLAMP_DIR, true);
        int maxFrames = bobModelData.getAnimationsList().get(0).frames().size();
        modelDataList.add(bobModelData);
        bob = new GameItem("BobObject", bobModelId);
        bob.setScale(0.04f);
        AxisRotation rot = AxisRotation.UP;
        rot.setRotation((float) Math.toRadians(-90.0f));
        bob.setRotation(rot.getQuatRotation());
        bob.buildModelMatrix();
        bob.setAnimation(new GameItemAnimation(false, 0, 0, maxFrames));

        String monsterModelId = "monster-model";
        ModelData monsterModelData = ModelLoader.loadModel(monsterModelId, ResourcePaths.Models.MONSTER_MD5MESH,
                ResourcePaths.Models.MONSTER_DIR, true);
        int monsterMax = monsterModelData.getAnimationsList().get(0).frames().size();
        modelDataList.add(monsterModelData);
        monster = new GameItem("MonsterObject", monsterModelId);
        monster.setScale(0.02f);
        //rot.setRotation((float) Math.toRadians(-90.0f));
        //monster.setRotation(rot.getQuatRotation());
        monster.setPosition(-5f, 0f, 0f);
        monster.buildModelMatrix();
        monster.setAnimation(new GameItemAnimation(false, 0, 0, monsterMax));
    }
	@Override
	public void load(Scene scene, VKRenderer renderer, NewtonWorld world, Physics physics, MemorySession session) throws Exception {
        scene.addGameItem(sponza);
        scene.addGameItem(bob);
        scene.addGameItem(monster);
        
        renderer.loadModels(modelDataList, scene);
	}

    @Override
    public void reset(Scene scene, VKRenderer renderer, NewtonWorld world, Physics physics, MemorySession session) throws Exception {
        bob.getAnimation().setCurrentFrame(0);
        monster.getAnimation().setCurrentFrame(0);
    }
}
