package main.game;

import java.util.ArrayList;
import java.util.List;

import crab.newton.NewtonWorld;
import java.lang.foreign.*;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import main.engine.Engine;
import main.engine.ItemLoadTimestamp;
import main.engine.graphics.ModelData;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.input.EngineInput;
import main.engine.input.KeyboardInput;
import main.engine.items.GameItem;
import main.engine.items.GameItemAnimation;
import main.engine.items.GameItemManager;
import main.engine.loaders.assimp.ModelLoader;
import main.engine.physics.Physics;
import main.engine.scene.Level;
import main.engine.scene.Scene;
import main.engine.scripts.EntityInitializer;
import main.engine.utility.AxisRotation;
import main.engine.utility.ResourcePaths;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;

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

    public static void initCamera(Entity entity) {
        Camera camera = entity.get(Camera.class);
        camera.setPosition(-6.0f, 2.0f, 0.0f);
        camera.setRotationEuler((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f), 0.0f);
        camera.updateQuat();
    }

    public static void initBob(Entity entity) {
        GameItem bob = entity.get(GameItem.class);
        bob.setScale(0.04f);
        AxisRotation rot = AxisRotation.UP;
        rot.setRotation((float) Math.toRadians(-90.0f));
        bob.setRotation(rot.getQuatRotation());
        bob.buildModelMatrix();

        GameItemAnimation bobAnimation = entity.get(GameItemAnimation.class);
        bobAnimation.setCurrentFrame(0);
        bobAnimation.setLoaded(false);
    }

    public static void initMonster(Entity entity) {
        GameItem monster = entity.get(GameItem.class);
        monster.setScale(0.02f);
        monster.setPosition(-5f, 0f, 0f);
        monster.buildModelMatrix();

        GameItemAnimation monsterAnimation = entity.get(GameItemAnimation.class);
        monsterAnimation.setCurrentFrame(0);
        monsterAnimation.setLoaded(false);
    }

    public static void initCamera(Scene scene, VKRenderer renderer, Physics physics) {
        Camera camera = new Camera();
    }

    public static void loadBob(Scene scene, Engine engine) {
        String bobModelId = "bob-model";
        ModelData bobModelData = ModelLoader.loadModel(bobModelId, ResourcePaths.Models.BOBLAMP_MD5MESH,
                ResourcePaths.Models.BOBLAMP_DIR, true);
        int maxFrames = bobModelData.getAnimationsList().get(0).frames().size();

        Entity entity = scene.addAnimatedGameItem(bobModelId, maxFrames);
        entity.add(bobModelData);
        entity.add(new EntityInitializer(Sponza::initBob));
    }

    public static void loadMonster(Scene scene, Engine engine) {
        String monsterModelId = "monster-model";
        ModelData monsterModelData = ModelLoader.loadModel(monsterModelId, ResourcePaths.Models.MONSTER_MD5MESH,
                ResourcePaths.Models.MONSTER_DIR, true);
        int maxFrames = monsterModelData.getAnimationsList().get(0).frames().size();

        Entity entity = scene.addAnimatedGameItem(monsterModelId, maxFrames);
        entity.add(monsterModelData);
        entity.add(new EntityInitializer(Sponza::initMonster));
    }

    public static void loadSponza(Scene scene, Engine engine) {
        String sponzaModelId = "sponza-model";
        ModelData sponzaModelData = ModelLoader.loadModel(sponzaModelId, ResourcePaths.Models.SPONZA_GLTF,
                ResourcePaths.Models.SPONZA_DIR, false);
        Entity entity = scene.addStaticGameItem(sponzaModelId);
        entity.add(sponzaModelData);
    }

    public static void updateCamera(Entity entity, EngineInput engineInput) {
        Camera camera = entity.get(Camera.class);
        Vector3f cameraInc = new Vector3f();

        if (engineInput.keyboardInput.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (engineInput.keyboardInput.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (engineInput.keyboardInput.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (engineInput.keyboardInput.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (engineInput.keyboardInput.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (engineInput.keyboardInput.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;
        }

        Vector3f prevPos = new Vector3f(camera.getPosition());
        camera.movePosition(cameraInc.x * Game.CAMERA_POS_STEP, cameraInc.y * Game.CAMERA_POS_STEP, cameraInc.z * Game.CAMERA_POS_STEP);
        // Check if there has been a collision. If true, set the y position to
        // the maximum height
        float height = -Float.MAX_VALUE;
        if ( camera.getPosition().y <= height )  {
            camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
        }

        Vector2f rotVec = engineInput.mouseInput.getDisplVec();
        camera.moveRotation(rotVec.x * Game.MOUSE_SENSITIVITY, rotVec.y * Game.MOUSE_SENSITIVITY, 0);

        camera.updateViewMatrixQuat();
    }

    public static void updateAnimation(Entity entity, EngineInput engineInput) {
        GameItemAnimation animation = entity.get(GameItemAnimation.class);

        if (engineInput.keyboardInput.isKeyPressedOnce(GLFW_KEY_SPACE)) animation.toggle();

        if (animation.isStarted()) {
            int currentFrame = Math.floorMod(animation.getCurrentFrame() + 1, animation.maxFrames);
            animation.setCurrentFrame(currentFrame);
        }
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
