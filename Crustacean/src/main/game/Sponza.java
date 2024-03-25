package main.game;

import java.util.ArrayList;
import java.util.List;

import crab.newton.NewtonWorld;
import java.lang.foreign.*;

import dev.dominion.ecs.api.Dominion;
import main.engine.ItemLoadTimestamp;
import main.engine.graphics.ModelData;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.items.GameItem;
import main.engine.items.GameItemAnimation;
import main.engine.items.GameItemManager;
import main.engine.loaders.assimp.ModelLoader;
import main.engine.physics.Physics;
import main.engine.scene.Level;
import main.engine.scene.Scene;
import main.engine.utility.AxisRotation;
import main.engine.utility.ResourcePaths;

public class Sponza extends Scene.Tag implements Level {

    //private final List<ModelData> modelDataList;
    //public final GameItem sponza;
    //public final GameItem bob;
    //public final GameItemAnimation bobAnimation;
    //public final GameItem monster;
    //public final GameItemAnimation monsterAnimation;
    //public final GameItemManager manager;

    public Sponza() {
        /*
        modelDataList = new ArrayList<>();
        manager = new GameItemManager();

        String sponzaModelId = "sponza-model";
        ModelData sponzaModelData = ModelLoader.loadModel(sponzaModelId, ResourcePaths.Models.SPONZA_GLTF,
                ResourcePaths.Models.SPONZA_DIR, false);
        modelDataList.add(sponzaModelData);
        sponza = manager.createGameItem(sponzaModelId);

        String bobModelId = "bob-model";
        ModelData bobModelData = ModelLoader.loadModel(bobModelId, ResourcePaths.Models.BOBLAMP_MD5MESH,
                ResourcePaths.Models.BOBLAMP_DIR, true);
        int maxFrames = bobModelData.getAnimationsList().get(0).frames().size();
        modelDataList.add(bobModelData);
        bob = manager.createGameItem(bobModelId);
        bob.setScale(0.04f);
        AxisRotation rot = AxisRotation.UP;
        rot.setRotation((float) Math.toRadians(-90.0f));
        bob.setRotation(rot.getQuatRotation());
        bob.buildModelMatrix();
        bobAnimation = new GameItemAnimation(false, 0, 0, maxFrames);

        String monsterModelId = "monster-model";
        ModelData monsterModelData = ModelLoader.loadModel(monsterModelId, ResourcePaths.Models.MONSTER_MD5MESH,
                ResourcePaths.Models.MONSTER_DIR, true);
        int monsterMax = monsterModelData.getAnimationsList().get(0).frames().size();
        modelDataList.add(monsterModelData);
        monster = manager.createGameItem(monsterModelId);
        monster.setScale(0.02f);
        //rot.setRotation((float) Math.toRadians(-90.0f));
        //monster.setRotation(rot.getQuatRotation());
        monster.setPosition(-5f, 0f, 0f);
        monster.buildModelMatrix();
        monsterAnimation = new GameItemAnimation(false, 0, 0, monsterMax);
        */
    }
	@Override
	public void load(Scene scene, VKRenderer renderer, Physics physics) throws Exception {
        dominion.createEntity(sponza);
        dominion.createEntity(bob, bobAnimation);
        dominion.createEntity(monster, monsterAnimation);

        ItemLoadTimestamp stamp = ItemLoadTimestamp.getTimeStamp(dominion);
        stamp.gameItemLoadedTimestamp = System.currentTimeMillis();

        Camera camera = renderer.getCamera();
        camera.setPosition(-6.0f, 2.0f, 0.0f);
        camera.setRotationEuler((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f), 0.0f);
        camera.updateQuat();

        for (ModelData modelData : modelDataList) {
            dominion.createEntity(modelData);
        }
        
        renderer.loadModels();
	}

    @Override
    public void reset(Scene scene, VKRenderer renderer, Physics physics) throws Exception {
        bobAnimation.setCurrentFrame(0);
        bobAnimation.setLoaded(false);
        monsterAnimation.setCurrentFrame(0);
        monsterAnimation.setLoaded(false);

        Camera camera = renderer.getCamera();
        camera.setPosition(-6.0f, 2.0f, 0.0f);
        camera.setRotationEuler((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f), 0.0f);
        camera.updateQuat();
    }
}
