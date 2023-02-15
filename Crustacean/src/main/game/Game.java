package main.game;

import static org.lwjgl.glfw.GLFW.*;

import java.util.*;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import main.engine.*;
import main.engine.graphics.lights.AmbientLight;
import main.engine.items.GameItemAnimation;
import main.game.hud.TransparentWindow;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import main.engine.graphics.ModelData;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.camera.MouseBoxSelectionDetector;
import main.game.hud.GameMenu;
import main.engine.graphics.hud.NKHudElement;
import main.engine.graphics.lights.DirectionalLight;
import main.engine.graphics.lights.Light;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.items.GameItem;
import main.engine.items.SkyBox;
import main.engine.loaders.assimp.ModelLoader;
import main.engine.physics.Physics;
import main.engine.scene.Level;
import main.engine.scene.SceneLight;
import main.engine.sound.SoundBuffer;
import main.engine.sound.SoundListener;
import main.engine.sound.SoundManager;
import main.engine.sound.SoundSource;
import main.engine.utility.ResourcePaths;

import crab.newton.*;
import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;

import java.lang.foreign.*;

public class Game implements IGameLogic {
	
	private static final EngineProperties engineProperties = EngineProperties.INSTANCE;
	
	private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;
    
    //private GameHud gHud;

    private float lightAngle;
    
    private static final float CAMERA_POS_STEP = 0.05f;
    
    private float angleInc;
    
    private Light directionalLight;
    
    private final SoundManager soundMgr;
    
    //private FlowParticleEmitter particleEmitter;
    
    private MouseBoxSelectionDetector selectDetector;
    
    private boolean updatePhysics;
    
    private enum Sounds { FIRE };
    
    private GameItemAnimation bob, monster;
    
    private SkyBox skybox;
    
    private Vector3f rotatingAngle = new Vector3f(1, 1, 1);
    
    private VKRenderer vkRenderer;

    private MemorySession gameSession;
    
    private Physics gamePhysics;
    
    private GameMenu menu;
    private Level currentLevel;


    private int levelSelection;
    
    public Game() {
        soundMgr = new SoundManager();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        angleInc = 0;
        lightAngle = 45;
        updatePhysics = false;
    }
    
    public static void main(String[] args) {
        try {
            Map<String, String> fuck = new HashMap<>();
            fuck.put("writer1", "console");
            fuck.put("writer2", "file");
            fuck.put("writer2.file", "log.txt");
            Configuration.replace(fuck);
            Logger.debug("Application Directory: {}", System.getProperty("user.dir"));
            IGameLogic gameLogic = new Game();
            Window.WindowOptions opts = new Window.WindowOptions();
            opts.showFps = true;
            opts.compatibleProfile = true;
            opts.frustumCulling = true;
            GameEngine gameEng = new GameEngine("GAME", opts, gameLogic);
            gameEng.start();
        } catch (Exception excp) {
            Logger.error(excp);
            System.exit(-1);
        }
    }
    
    @Override
    public void init(Window window, Dominion dominion, VKRenderer renderer) throws Exception {
        soundMgr.init();

        gameSession = MemorySession.openShared();
        Newton.loadNewton(ResourcePaths.Newton.NEWTON_DLL, gameSession);
        /*
        try {
            Newton.loadNewton();
            Logger.debug("Loaded Newton in Jar");
        } catch (IOException e) {
            Newton.loadNewton("C:\\Users\\Christopher\\Documents\\Workspace\\Crustacean\\resources\\newtondll\\newton.dll");
            Logger.debug("Loaded Newton Absolutely");
        }
        */
        
        vkRenderer = renderer;
        
        gamePhysics = new Physics();

        //renderer.selectCamera(GameEngine.DEFAULT_CAMERA_NAME);
        menu = new GameMenu(window, dominion, renderer, gamePhysics, gameSession);
        currentLevel = menu.getLevel();
        levelSelection = menu.getCurrentLevel();
        vkRenderer.setNulkearElements(new NKHudElement[] {menu, new TransparentWindow(window)});
        menu.hideWindow(true);
        currentLevel.load(dominion, renderer, gamePhysics, gameSession);
        
        if (currentLevel instanceof Sponza zaza) {
            bob = zaza.bobAnimation;
            monster = zaza.monsterAnimation;
        }
        
        String skyboxId = "skyboxModel";
        ModelData skyboxModel = ModelLoader.loadModel(skyboxId, ResourcePaths.Models.SKYBOX_OBJ, 
        		ResourcePaths.Textures.TEXTURE_DIR, false);
        skyboxModel.getMaterialList().set(0, new ModelData.Material(ResourcePaths.Textures.SKYBOX_TEXTURE));
        skybox = new SkyBox(skyboxModel);
        skybox.setScale(200f);
        skybox.buildModelMatrix();
        dominion.createEntity("skybox", skybox);
        renderer.loadSkyBox(skyboxModel);
        

        AmbientLight ambientLight = new AmbientLight();
        ambientLight.ambientLight().set(0.2f, 0.2f, 0.2f, 1.0f);
        dominion.createEntity(ambientLight);
        List<Light> lights = new ArrayList<>();
        directionalLight = new Light();
        directionalLight.getPosition().set(0.0f, 1.0f, 0.0f, 0.0f);
        directionalLight.getColor().set(1.0f, 1.0f, 1.0f, 1.0f);
        lights.add(directionalLight);
        updateDirectionalLight();
        renderer.setDirectionalLight(directionalLight);
        for (Light light : lights) {
            dominion.createEntity(light);
        }
    }
    
    private void setupSounds() throws Exception {
        SoundBuffer buffFire = new SoundBuffer(ResourcePaths.Sounds.FIRE_OGG);
        soundMgr.addSoundBuffer(buffFire);
        SoundSource sourceFire = new SoundSource(true, false);
        Vector3f pos = new Vector3f(); //particleEmitter.getBaseParticle().getPosition();
        sourceFire.setPosition(pos);
        sourceFire.setBuffer(buffFire.getBufferId());
        soundMgr.addSoundSource(Sounds.FIRE.toString(), sourceFire);
        sourceFire.play();
        
        soundMgr.setListener(new SoundListener(new Vector3f()));      
    }
    
    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        //scene.setSceneLight(sceneLight);

        // Ambient Light
        //sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.getSkyBoxLight().set(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.setShadowPosMult(5);
        directionalLight.setOrthoCords(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f);
        //sceneLight.setDirectionalLight(directionalLight);
    }

    @Override
    public void inputAndUpdate(Window window, Dominion dominion, VKRenderer renderer, long diffTimeNanos) throws Exception {
        if (!currentLevel.equals(menu.getLevel())) {
            currentLevel = menu.getLevel();
            levelSelection = menu.getCurrentLevel();
            vkRenderer.unloadModels();
            Results<Results.With1<GameItem>> results = dominion.findEntitiesWith(GameItem.class);
            for (Iterator<Results.With1<GameItem>> itr = results.iterator(); itr.hasNext();) {
                Results.With1<GameItem> result = itr.next();
                dominion.deleteEntity(result.entity());
            }
            currentLevel.load(dominion, vkRenderer, gamePhysics, gameSession);
            if (currentLevel instanceof Sponza zaza) {
                bob = zaza.bobAnimation;
                monster = zaza.monsterAnimation;
            }
        }
    	cameraInc.set(0, 0, 0);
        KeyboardInput keyboard = window.getKeyboardInput();
    	if (keyboard.isKeyPressedOnce(GLFW_KEY_ESCAPE)) {
            Logger.debug("Escape key pressed");
            boolean shouldHide = !menu.isHidden();
    		menu.hideWindow(shouldHide);
    	}
        if (keyboard.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (keyboard.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (keyboard.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (keyboard.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (keyboard.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (keyboard.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;
        }
        
        if (keyboard.isKeyPressed(GLFW_KEY_LEFT)) {
            angleInc -= 0.05f;
            directionalLight.setChanged(true);
        } else if (keyboard.isKeyPressed(GLFW_KEY_RIGHT)) {
            angleInc += 0.05f;
            directionalLight.setChanged(true);
        } else {
            angleInc = 0;
            directionalLight.setChanged(false);
        }
        
        if (keyboard.isKeyPressedOnce(GLFW_KEY_SPACE) && currentLevel instanceof Sponza) {
            bob.setStarted(!bob.isStarted());
            monster.setStarted(!monster.isStarted());
        }
        
        if (keyboard.isKeyPressedOnce(GLFW_KEY_F) && currentLevel instanceof NewtonDemo) {
        	updatePhysics = !updatePhysics;
        }
        
        lightAngle += angleInc;
        if (lightAngle < 0) {
            lightAngle = 0;
        } else if (lightAngle > 180) {
            lightAngle = 180;
        }
        updateDirectionalLight();
        /**/
        if (currentLevel instanceof Sponza) {
            if (bob.isStarted()) {
                int currentFrame = Math.floorMod(bob.getCurrentFrame() + 1, bob.maxFrames);
                bob.setCurrentFrame(currentFrame);
            }

            if (monster.isStarted()) {
                int currentFrame = Math.floorMod(monster.getCurrentFrame() + 1, monster.maxFrames);
                monster.setCurrentFrame(currentFrame);
            }
        }
        
        if (updatePhysics && currentLevel instanceof NewtonDemo newtonDemo) {
        	float diffTimeSeconds = (float) (diffTimeNanos / 1000000000f); //1000000000f
        	gamePhysics.update(diffTimeSeconds, dominion);
    	}
    	MouseInput mouseInput = window.getMouseInput();
        Camera camera = renderer.getCamera();
    	// Update camera based on mouse            
        if (menu.isHidden()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }
        
        // Update camera position
        Vector3f prevPos = new Vector3f(camera.getPosition());
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);
        // Check if there has been a collision. If true, set the y position to
        // the maximum height
        float height = -Float.MAX_VALUE;
        if ( camera.getPosition().y <= height )  {
            camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
        }
        
        camera.updateViewMatrixQuat();
    }

    @Override
    public void cleanup() {
    	if (vkRenderer != null) {
    		vkRenderer.cleanup();
    	}
    	soundMgr.cleanup();
        /*
        if ( gHud != null ) {
            gHud.cleanup();
        }*/
        if (gameSession.isAlive()) {
            gameSession.close();
        }
        Newton.unloadNewton();
    }
    
    private void updateDirectionalLight() {
        float zValue = (float) Math.cos(Math.toRadians(lightAngle));
        float yValue = (float) Math.sin(Math.toRadians(lightAngle));
        Vector4f lightDirection = directionalLight.getPosition();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();
        lightDirection.w = 0.0f;
    }
}
