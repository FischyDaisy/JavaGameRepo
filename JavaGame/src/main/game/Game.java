package main.game;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.NK_ANTI_ALIASING_ON;
import static org.lwjgl.stb.STBImage.*;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.Version;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.openal.AL11;

import main.engine.EngineProperties;
import main.engine.IGameLogic;
import main.engine.MouseInput;
import main.engine.Scene;
import main.engine.SceneLight;
import main.engine.Window;
import main.engine.graphics.HeightMapMesh;
import main.engine.graphics.IHud;
import main.engine.graphics.IHudElement;
import main.engine.graphics.Material;
import main.engine.graphics.ModelData;
import main.engine.graphics.Renderer;
import main.engine.graphics.Transformation;
import main.engine.graphics.animation.AnimGameItem;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.camera.CameraBoxSelectionDetector;
import main.engine.graphics.camera.MouseBoxSelectionDetector;
import main.engine.graphics.hud.Calculator;
import main.engine.graphics.hud.Demo;
import main.engine.graphics.hud.GameHud;
import main.engine.graphics.hud.MenuHud;
import main.engine.graphics.lights.DirectionalLight;
import main.engine.graphics.lights.Light;
import main.engine.graphics.opengl.Mesh;
import main.engine.graphics.opengl.GLTexture;
import main.engine.graphics.opengl.GLTextureCache;
import main.engine.graphics.opengl.InstancedGLModel;
import main.engine.graphics.particles.FlowParticleEmitter;
import main.engine.graphics.particles.Particle;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.graphics.weather.Fog;
import main.engine.graphics.opengl.GLRenderer;
import main.engine.items.GameItem;
import main.engine.items.GameItem.GameItemAnimation;
import main.engine.items.Portal;
import main.engine.items.SkyBox;
import main.engine.items.Terrain;
import main.engine.loaders.assimp.ModelLoader;
import main.engine.loaders.obj.OBJLoader;
import main.engine.physics.NewtonLoader;
import main.engine.sound.SoundBuffer;
import main.engine.sound.SoundListener;
import main.engine.sound.SoundManager;
import main.engine.sound.SoundSource;
import main.engine.utility.AxisRotation;
import main.engine.utility.ResourcePaths;

import com.newton.*;
import com.newton.generated.Newton_h;

import jdk.incubator.foreign.*;

public class Game implements IGameLogic {
	
	private static final EngineProperties engineProperties = EngineProperties.getInstance();
	
	private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;
    
    private final Camera camera;

    private Scene scene;
    
    private MenuHud mHud;
    
    private GameHud gHud;

    private float lightAngle;
    
    private static final float CAMERA_POS_STEP = 0.05f;
    
    private Terrain terrain;
    
    private float angleInc;
    
    private Light directionalLight;
    
    private final SoundManager soundMgr;
    
    private FlowParticleEmitter particleEmitter;
    
    private MouseBoxSelectionDetector selectDetector;
    
    private boolean leftButtonPressed;
    
    private boolean firstTime;

    private boolean sceneChanged;
    
    private enum Sounds { FIRE };
    
    private GameItem[] gameItems;
    
    private GameItem bob;
    
    private GameItem monster;
    
    private int maxFrames = 0;
    
    private int monsterMax;
    
    private AnimGameItem momster;
    
    private Vector3f rotatingAngle = new Vector3f(1, 1, 1);
    
    private GLRenderer glRenderer;
    
    private VKRenderer vkRenderer;
    
    public Game() {
        camera = new Camera();
        soundMgr = new SoundManager();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        angleInc = 0;
        lightAngle = 45;
        firstTime = true;
    }
    
    @Override
    public void init(Window window, Scene scene, Renderer renderer) throws Exception {
        soundMgr.init();
        
        leftButtonPressed = false;
        
        this.scene = scene;
        
        if (!engineProperties.useVulkan()) { //OpenGL
        	glRenderer = (GLRenderer) renderer;
        	float reflectance = 1f;

            float blockScale = 0.5f;
            float skyBoxScale = 100.0f;
            float extension = 2.0f;

            float startx = extension * (-skyBoxScale + blockScale);
            float startz = extension * (skyBoxScale - blockScale);
            float starty = -1.0f;
            float inc = blockScale * 2;

            float posx = startx;
            float posz = startz;
            float incy = 0.0f;
            
            selectDetector = new MouseBoxSelectionDetector();

            ByteBuffer buf;
            int width;
            int height;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);

                buf = stbi_load(System.getProperty("user.dir") + "\\resources\\textures\\heightmap.png", w, h, channels, 4);
                if (buf == null) {
                    throw new Exception("Image file not loaded: " + stbi_failure_reason());
                }

                width = w.get();
                height = h.get();
            }

            List<ModelData> modelList = new ArrayList<ModelData>();
            int instances = height * width;
            String cubeModelId = "CubeModel";
            ModelData modelData = ModelLoader.loadModel(cubeModelId, ResourcePaths.Models.MCUBE_OBJ,
                    ResourcePaths.Textures.TEXTURE_DIR, false);
            modelData.getMaterialList().set(0, new ModelData.Material(System.getProperty("user.dir") + "\\resources\\textures\\terrain_textures.png",
            		2, 1));
            modelList.add(modelData);
            ((GLRenderer) renderer).loadInstanceModels(modelList, instances);
            String modelId = "bunny";
            modelData = ModelLoader.loadModel(modelId, ResourcePaths.Models.BUNNY_OBJ,
                    ResourcePaths.Textures.TEXTURE_DIR, false);
            String houseId = "house";
            ModelData houseData = ModelLoader.loadModel(houseId, ResourcePaths.Models.HOUSE_OBJ, 
            		ResourcePaths.Models.HOUSE_DIR, false);
            modelList.clear();
            modelList.add(modelData);
            //modelList.add(houseData);
            //modelList.add(animData);
            ((GLRenderer) renderer).loadModels(modelList);
            gameItems = new GameItem[instances];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    GameItem gameItem = new GameItem("CubeObject", cubeModelId);
                    gameItem.setScale(blockScale);
                    int rgb = HeightMapMesh.getRGB(i, j, width, buf);
                    incy = rgb / (10 * 255 * 255);
                    gameItem.setPosition(posx, starty + incy, posz);
                    int textPos = Math.random() > 0.5f ? 0 : 1;
                    gameItem.setTextPos(textPos);
                    scene.addInstancedGameItem(gameItem);
                    gameItems[i * width + j] = gameItem;

                    posx += inc;
                }
                posx = startx;
                posz -= inc;
            }
            GameItem bun = new GameItem("BunnyObject", modelId);
            bun.setPosition(0f, 4f, 0f);
            bun.setScale(2.0f);
            scene.addGameItem(bun);
            
            GameItem house = new GameItem("HouseObject", houseId);
            //scene.addGameItem(house);
            
            //momster = new AnimGameItem("momsterObject", animId, animData.getAnimations());
           // momster.setPosition(-5f, 4f, 0f);
            //momster.setScale(1.0f);
            //scene.addGameItem(momster);
            //scene.setGameItems(gameItems);
            
            // Particles
            int maxParticles = 200;
            Vector3f particleSpeed = new Vector3f(0, 1, 0);
            particleSpeed.mul(2.5f);
            long ttl = (long) 4e9;
            long creationPeriodNanos = (long) 3e8;
            float range = 0.2f;
            float scale = 1.0f;
            //Mesh partMesh = OBJLoader.loadMesh("/resources/models/particle.obj", maxParticles);
            String partId = "particle";
            modelData = ModelLoader.loadModel(partId, ResourcePaths.Models.PARTICLE_OBJ, 
            		ResourcePaths.Textures.TEXTURE_DIR, false);
            modelData.getMaterialList().set(0, new ModelData.Material(System.getProperty("user.dir") + "\\resources\\textures\\particle_anim.png",
            		4, 4));
            modelList.clear();
            modelList.add(modelData);
            glRenderer.loadParticles(modelList, maxParticles);
            GLTexture particleTexture = GLTextureCache.getInstance().get(System.getProperty("user.dir") + "\\resources\\textures\\particle_anim.png", 4, 4);
            //Material partMaterial = new Material(particleTexture, reflectance);
            //partMesh.setMaterial(partMaterial);
            Particle particle = new Particle("FlameParticle", partId, particleTexture, particleSpeed, ttl, 100);
            particle.setScale(scale);
            particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodNanos);
            particleEmitter.setActive(true);
            particleEmitter.setPositionAndRange(range);
            particleEmitter.setSpeedAndRange(range);
            particleEmitter.setAnimRange(10);
            this.scene.setParticleEmitters(new FlowParticleEmitter[]{particleEmitter});
            
            // Portals
            /*
             
            Mesh pMesh = OBJLoader.loadMesh("/resources/models/double_quad.obj");
            pMesh.setBoundingRadius(1.0f);
            Portal portalA = new Portal(pMesh);
            Portal portalB = new Portal(pMesh);
            portalA.setPosition(0f, 4f, 0f);
            portalB.setPosition(5f, 4f, 0f);
            GameItem[] fullList = new GameItem[gameItems.length + 2];
            for (int i = 0; i < gameItems.length; i++) {
            	fullList[i] = gameItems[i];
            }
            fullList[fullList.length - 2] = portalA;
            fullList[fullList.length - 1] = portalB;
            Portal.connect(portalA, portalB, new Transformation());
            */
            // Shadows
            scene.setRenderShadows(true);
            
            // Fog
            Vector3f fogColour = new Vector3f(0.5f, 0.5f, 0.5f);
            //scene.setFog(new Fog(true, fogColour, 0.02f));
            
            // Setup  SkyBox
            ModelData skyboxData = ModelLoader.loadModel("skyBoxModel", ResourcePaths.Models.SKYBOX_OBJ, 
            		ResourcePaths.Textures.TEXTURE_DIR, false);
            SkyBox skyBox = new SkyBox(skyboxData, glRenderer, new Vector4f(0.65f, 0.65f, 0.65f, 1.0f));
            skyBox.setScale(skyBoxScale);
            scene.setSkyBox(skyBox);
            
            // Setup Lights
            setupLights();
            
            // Create HUD
            Runtime.Version runtimeVersion = Runtime.version();
            String version = String.valueOf(runtimeVersion.version().get(0) + "." + runtimeVersion.version().get(1) + "." 
            		+ runtimeVersion.version().get(2));
            //hud = new Hud("Java Runtime Version: " + version + " | LWJGL Version: " + Version.getVersion());
            //gHud = new GameHud(System.getProperties().getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe");
            //mHud = new MenuHud(window);
            //mHud.setElements(new IHudElement[] {new Demo(), new Calculator()});
            
            camera.getPosition().x = 0.25f;
            camera.getPosition().y = 6.5f;
            camera.getPosition().z = 6.5f;
            camera.getRotationEuler().x = 25;
            camera.getRotationEuler().y = -1;
            
            stbi_image_free(buf);
            
            // Sounds
            this.soundMgr.init();
            this.soundMgr.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
            setupSounds();
        } else { //Vulkan
        	vkRenderer = (VKRenderer) renderer;
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
            maxFrames = bobModelData.getAnimationsList().get(0).frames().size();
            modelDataList.add(bobModelData);
            bob = new GameItem("BobObject", bobModelId);
            bob.setScale(0.04f);
            AxisRotation rot = AxisRotation.UP;
            rot.setRotation((float) Math.toRadians(-90.0f));
            bob.setRotation(rot.getQuatRotation());
            bob.buildModelMatrix();
            bob.setGameItemAnimation(new GameItem.GameItemAnimation(false, 0, 0));
            scene.addGameItem(bob);
            
            String monsterModelId = "monster-model";
            ModelData monsterModelData = ModelLoader.loadModel(monsterModelId, ResourcePaths.Models.MONSTER_MD5MESH, 
            		ResourcePaths.Models.MONSTER_DIR, true);
            monsterMax = monsterModelData.getAnimationsList().get(0).frames().size();
            modelDataList.add(monsterModelData);
            monster = new GameItem("MonsterObject", monsterModelId);
            monster.setScale(0.02f);
            //rot.setRotation((float) Math.toRadians(-90.0f));
            //monster.setRotation(rot.getQuatRotation());
            monster.setPosition(-5f, 0f, 0f);
            monster.buildModelMatrix();
            monster.setGameItemAnimation(new GameItem.GameItemAnimation(false, 0, 0));
            scene.addGameItem(monster);

            vkRenderer.loadModels(modelDataList);
            vkRenderer.loadAnimation(bob);
            vkRenderer.loadAnimation(monster);
            
            camera.setPosition(-6.0f, 2.0f, 0.0f);
            camera.setRotationEuler((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f), 0.0f);
            scene.setCamera(camera);
            
            scene.getSceneLight().getAmbientLight().set(0.2f, 0.2f, 0.2f, 1.0f);
            List<Light> lights = new ArrayList<>();
            directionalLight = new Light();
            directionalLight.getPosition().set(0.0f, 1.0f, 0.0f, 0.0f);
            directionalLight.getColor().set(1.0f, 1.0f, 1.0f, 1.0f);
            lights.add(directionalLight);
            updateDirectionalLight();
            
            Light[] lightArr = new Light[lights.size()];
            lightArr = lights.toArray(lightArr);
            scene.getSceneLight().setLights(lightArr);
        }
    }
    
    private void setupSounds() throws Exception {
        
        SoundBuffer buffFire = new SoundBuffer(ResourcePaths.Sounds.FIRE_OGG);
        soundMgr.addSoundBuffer(buffFire);
        SoundSource sourceFire = new SoundSource(true, false);
        Vector3f pos = particleEmitter.getBaseParticle().getPosition();
        sourceFire.setPosition(pos);
        sourceFire.setBuffer(buffFire.getBufferId());
        soundMgr.addSoundSource(Sounds.FIRE.toString(), sourceFire);
        sourceFire.play();
        
        soundMgr.setListener(new SoundListener(new Vector3f()));      
    }
    
    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

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
    public void input(Window window, Scene scene, long diffTimeMillis) {
    	sceneChanged = false;
        if (!engineProperties.useVulkan()) {
        	if (mHud != null) {
        		mHud.input(window);
        		
        	}
        	cameraInc.set(0, 0, 0);
            if (window.isKeyPressed(GLFW_KEY_W)) {
                cameraInc.z = -1;
                sceneChanged = true;
            } else if (window.isKeyPressed(GLFW_KEY_S)) {
                cameraInc.z = 1;
                sceneChanged = true;
            }
            if (window.isKeyPressed(GLFW_KEY_A)) {
                cameraInc.x = -1;
                sceneChanged = true;
            } else if (window.isKeyPressed(GLFW_KEY_D)) {
                cameraInc.x = 1;
                sceneChanged = true;
            }
            if (window.isKeyPressed(GLFW_KEY_Z)) {
                cameraInc.y = -1;
                sceneChanged = true;
            } else if (window.isKeyPressed(GLFW_KEY_X)) {
                cameraInc.y = 1;
                sceneChanged = true;
            }
            if (window.isKeyPressed(GLFW_KEY_LEFT)) {
                angleInc -= 0.05f;
                sceneChanged = true;
            } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
                angleInc += 0.05f;
                sceneChanged = true;
            } else {
                angleInc = 0;
            }
            if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            	momster.getCurrentAnimation().nextFrame();
            	sceneChanged = true;
            }
        } else {
        	cameraInc.set(0, 0, 0);
            if (window.isKeyPressed(GLFW_KEY_W)) {
                cameraInc.z = -1;
                sceneChanged = true;
            } else if (window.isKeyPressed(GLFW_KEY_S)) {
                cameraInc.z = 1;
                sceneChanged = true;
            }
            if (window.isKeyPressed(GLFW_KEY_A)) {
                cameraInc.x = -1;
                sceneChanged = true;
            } else if (window.isKeyPressed(GLFW_KEY_D)) {
                cameraInc.x = 1;
                sceneChanged = true;
            }
            if (window.isKeyPressed(GLFW_KEY_Z)) {
                cameraInc.y = -1;
                sceneChanged = true;
            } else if (window.isKeyPressed(GLFW_KEY_X)) {
                cameraInc.y = 1;
                sceneChanged = true;
            }
            
            if (window.isKeyPressed(GLFW_KEY_LEFT)) {
                angleInc -= 0.05f;
                scene.getSceneLight().setLightChanged(true);
            } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
                angleInc += 0.05f;
                scene.getSceneLight().setLightChanged(true);
            } else {
                angleInc = 0;
                scene.getSceneLight().setLightChanged(false);
            }
            
            if (window.isKeyPressed(GLFW_KEY_SPACE)) {
                bob.getGameItemAnimation().setStarted(!bob.getGameItemAnimation().isStarted());
                monster.getGameItemAnimation().setStarted(!monster.getGameItemAnimation().isStarted());
            }
            
            lightAngle += angleInc;
            if (lightAngle < 0) {
                lightAngle = 0;
            } else if (lightAngle > 180) {
                lightAngle = 180;
            }
            updateDirectionalLight();
            
            GameItem.GameItemAnimation itemAnimation = bob.getGameItemAnimation();
            if (itemAnimation.isStarted()) {
                int currentFrame = Math.floorMod(itemAnimation.getCurrentFrame() + 1, maxFrames);
                itemAnimation.setCurrentFrame(currentFrame);
            }
            
            itemAnimation = monster.getGameItemAnimation();
            if (itemAnimation.isStarted()) {
                int currentFrame = Math.floorMod(itemAnimation.getCurrentFrame() + 1, monsterMax);
                itemAnimation.setCurrentFrame(currentFrame);
            }
        }
    }

    @Override
    public void update(double interval, Window window) {
    	MouseInput mouseInput = window.getMouseInput();
        if (!engineProperties.useVulkan()) {
        	// Update camera based on mouse            
            if (mouseInput.isRightButtonPressed()) {
                Vector2f rotVec = mouseInput.getDisplVec();
                camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
                sceneChanged = true;
            }
            
            // Update camera position
            Vector3f prevPos = new Vector3f(camera.getPosition());
            camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);        
            // Check if there has been a collision. If true, set the y position to
            // the maximum height
            float height = terrain != null ? terrain.getHeight(camera.getPosition()) : -Float.MAX_VALUE;
            if ( camera.getPosition().y <= height )  {
                camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
            }

            lightAngle += angleInc;
            if (lightAngle < 0) {
                lightAngle = 0;
            } else if (lightAngle > 180) {
                lightAngle = 180;
            }
            float zValue = (float) Math.cos(Math.toRadians(lightAngle));
            float yValue = (float) Math.sin(Math.toRadians(lightAngle));
            Vector3f lightDirection = new Vector3f();//this.scene.getSceneLight().getDirectionalLight().getDirection();
            lightDirection.x = 0;
            lightDirection.y = yValue;
            lightDirection.z = zValue;
            lightDirection.normalize();
            
            if (gHud != null) {
            	gHud.updateSize(window);
            	gHud.updateCrossHair(window);
            }
            
            particleEmitter.update(Double.valueOf(interval).longValue());
            
            // Update view matrix
            //camera.updateViewMatrixEuler();
            camera.updateViewMatrixQuat();
            
            // Update sound listener position;
            soundMgr.updateListenerPosition(camera);
            
            boolean aux = mouseInput.isLeftButtonPressed();        
            if (aux && (!this.leftButtonPressed) && this.selectDetector.selectGameItem(gameItems, window, mouseInput.getCurrentPos(), camera)) {
                //this.gHud.incCounter();
            }
            this.leftButtonPressed = aux;
        } else {
        	// Update camera based on mouse            
            if (mouseInput.isRightButtonPressed()) {
                Vector2f rotVec = mouseInput.getDisplVec();
                camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
                sceneChanged = true;
            }
            
            // Update camera position
            Vector3f prevPos = new Vector3f(camera.getPosition());
            camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);        
            // Check if there has been a collision. If true, set the y position to
            // the maximum height
            float height = terrain != null ? terrain.getHeight(camera.getPosition()) : -Float.MAX_VALUE;
            if ( camera.getPosition().y <= height )  {
                camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
            }
            
            camera.updateViewMatrixQuat();
        }
    }

    @Override
    public void render(Window window, Scene scene) {
    	if (firstTime) {
            sceneChanged = true;
            firstTime = false;
        }
    	this.scene = scene;
        if (glRenderer != null) {
        	glRenderer.render(window, camera, scene, sceneChanged);
        }
        if (vkRenderer != null) {
        	vkRenderer.render(window, scene);
        }
        if (mHud != null) {
        	mHud.render(window);
        }
        if (gHud != null) {
        	gHud.render(window);
        }
    }

    @Override
    public void cleanup() {
    	if (glRenderer != null) {
    		glRenderer.cleanup();
    	}
    	if (vkRenderer != null) {
    		vkRenderer.cleanup();
    	}
    	soundMgr.cleanup();
        scene.cleanup();
        if ( mHud != null ) {
            mHud.cleanup();
        }
        if ( gHud != null ) {
            gHud.cleanup();
        }
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
    
    public static String arrToString(float[] arr) {
    	String result = "";
    	for (int i = 0; i < arr.length; i++) {
    		result = result + ":" + arr[i] + ":";
    	}
    	return result;
    }
}
