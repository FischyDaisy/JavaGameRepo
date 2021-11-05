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
import main.engine.graphics.IRenderer;
import main.engine.graphics.Material;
import main.engine.graphics.ModelData;
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
import main.engine.graphics.opengl.Mesh;
import main.engine.graphics.opengl.GLTexture;
import main.engine.graphics.particles.FlowParticleEmitter;
import main.engine.graphics.particles.Particle;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.graphics.weather.Fog;
import main.engine.graphics.opengl.GLRenderer;
import main.engine.items.GameItem;
import main.engine.items.Portal;
import main.engine.items.SkyBox;
import main.engine.items.Terrain;
import main.engine.loaders.md5.MD5AnimModel;
import main.engine.loaders.md5.MD5Loader;
import main.engine.loaders.md5.MD5Model;
import main.engine.loaders.obj.OBJLoader;
import main.engine.physics.NewtonLoader;
import main.engine.sound.SoundBuffer;
import main.engine.sound.SoundListener;
import main.engine.sound.SoundManager;
import main.engine.sound.SoundSource;

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
    
    private final SoundManager soundMgr;
    
    private FlowParticleEmitter particleEmitter;
    
    private MouseBoxSelectionDetector selectDetector;
    
    private boolean leftButtonPressed;
    
    private enum Sounds { MUSIC, BEEP, FIRE };
    
    private GameItem[] gameItems;
    
    private GameItem cube;
    
    private Vector3f rotatingAngle = new Vector3f(1, 1, 1);
    
    public Game() {
        camera = new Camera();
        soundMgr = new SoundManager();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        angleInc = 0;
        lightAngle = 45;
    }
    
    @Override
    public void init(Window window, Scene scene, IRenderer renderer) throws Exception {
        renderer.init(window, scene);
        soundMgr.init();
        
        leftButtonPressed = false;
        
        this.scene = scene;
        
        if (!engineProperties.useVulkan()) { //OpenGL
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

                buf = stbi_load(System.getProperty("user.dir") + "\\src\\main\\resources\\textures\\heightmap.png", w, h, channels, 4);
                if (buf == null) {
                    throw new Exception("Image file not loaded: " + stbi_failure_reason());
                }

                width = w.get();
                height = h.get();
            }

            int instances = height * width;
            Mesh mesh = OBJLoader.loadMesh("/main/resources/models/cube.obj", instances);
            mesh.setBoundingRadius(2);
            GLTexture texture = new GLTexture(System.getProperty("user.dir") + "\\src\\main\\resources\\textures\\terrain_textures.png", 2, 1);
            Material material = new Material(texture, reflectance);
            mesh.setMaterial(material);
            Mesh bunny = OBJLoader.loadMesh("/main/resources/models/bunny.obj");
            Material bMat = new Material(Material.DEFAULT_COLOR, reflectance);
            bunny.setMaterial(bMat);
            gameItems = new GameItem[instances + 1];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    GameItem gameItem = new GameItem(mesh);
                    gameItem.setScale(blockScale);
                    int rgb = HeightMapMesh.getRGB(i, j, width, buf);
                    incy = rgb / (10 * 255 * 255);
                    gameItem.setPosition(posx, starty + incy, posz);
                    int textPos = Math.random() > 0.5f ? 0 : 1;
                    gameItem.setTextPos(textPos);
                    gameItems[i * width + j] = gameItem;

                    posx += inc;
                }
                posx = startx;
                posz -= inc;
            }
            GameItem bun = new GameItem(bunny);
            bun.setPosition(0f, 4f, 0f);
            bun.setScale(2.0f);
            gameItems[gameItems.length - 1] = bun;
            //scene.setGameItems(gameItems);
            
            // Particles
            int maxParticles = 200;
            Vector3f particleSpeed = new Vector3f(0, 1, 0);
            particleSpeed.mul(2.5f);
            long ttl = (long) 4e9;
            long creationPeriodNanos = (long) 3e8;
            float range = 0.2f;
            float scale = 1.0f;
            Mesh partMesh = OBJLoader.loadMesh("/main/resources/models/particle.obj", maxParticles);
            GLTexture particleTexture = new GLTexture(System.getProperty("user.dir") + "\\src\\main\\resources\\textures\\particle_anim.png", 4, 4);
            Material partMaterial = new Material(particleTexture, reflectance);
            partMesh.setMaterial(partMaterial);
            Particle particle = new Particle(partMesh, particleSpeed, ttl, 100);
            particle.setScale(scale);
            particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodNanos);
            particleEmitter.setActive(true);
            particleEmitter.setPositionAndRange(range);
            particleEmitter.setSpeedAndRange(range);
            particleEmitter.setAnimRange(10);
            this.scene.setParticleEmitters(new FlowParticleEmitter[]{particleEmitter});
            
            // Portals
            Mesh pMesh = OBJLoader.loadMesh("/main/resources/models/double_quad.obj");
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
            scene.setGameItems(fullList);
            
            // Shadows
            scene.setRenderShadows(false);
            
            // Fog
            Vector3f fogColour = new Vector3f(0.5f, 0.5f, 0.5f);
            scene.setFog(new Fog(true, fogColour, 0.02f));
            
            // Setup  SkyBox
            SkyBox skyBox = new SkyBox("/main/resources/models/skybox.obj", new Vector4f(0.65f, 0.65f, 0.65f, 1.0f));
            skyBox.setScale(skyBoxScale);
            scene.setSkyBox(skyBox);
            
            // Setup Lights
            setupLights();
            
            // Create HUD
            Runtime.Version runtimeVersion = Runtime.version();
            String version = String.valueOf(runtimeVersion.version().get(0) + "." + runtimeVersion.version().get(1) + "." 
            		+ runtimeVersion.version().get(2));
            //hud = new Hud("Java Runtime Version: " + version + " | LWJGL Version: " + Version.getVersion());
            gHud = new GameHud(System.getProperties().getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe");
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
        	/*
        	float[] positions = new float[]{
                    -0.5f, 0.5f, 0.5f,
                    -0.5f, -0.5f, 0.5f,
                    0.5f, -0.5f, 0.5f,
                    0.5f, 0.5f, 0.5f,
                    -0.5f, 0.5f, -0.5f,
                    0.5f, 0.5f, -0.5f,
                    -0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
            };
            float[] textCoords = new float[]{
                    0.0f, 0.0f,
                    0.5f, 0.0f,
                    1.0f, 0.0f,
                    1.0f, 0.5f,
                    1.0f, 1.0f,
                    0.5f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.5f,
            };
            int[] indices = new int[]{
                    // Front face
                    0, 1, 3, 3, 1, 2,
                    // Top Face
                    4, 0, 3, 5, 4, 3,
                    // Right face
                    3, 2, 7, 5, 3, 7,
                    // Left face
                    6, 1, 0, 6, 0, 4,
                    // Bottom face
                    2, 1, 6, 2, 6, 7,
                    // Back face
                    7, 6, 4, 7, 4, 5,
            };
            
            float[] empF = new float[10];
            int[] empI = new int[10];

            String modelId = "CubeModel";
            ModelData.MeshData meshData = new ModelData.MeshData(positions, textCoords, empF, indices, empI, empF);
            List<ModelData.MeshData> meshDataList = new ArrayList<>();
            meshDataList.add(meshData);
            ModelData modelData = new ModelData(modelId, meshDataList);
            List<ModelData> modelDataList = new ArrayList<>();
            modelDataList.add(modelData);
            ((VKRenderer) renderer).loadModels(modelDataList);

            cube = new GameItem("CubeEntity", modelId);
            cube.setPosition(0, 0, -2);
            scene.addGameItem(cube);
            */
        }
    }
    
    private void setupSounds() throws Exception {
        SoundBuffer buffBack = new SoundBuffer("/main/resources/sounds/background.ogg");
        soundMgr.addSoundBuffer(buffBack);
        SoundSource sourceBack = new SoundSource(true, true);
        sourceBack.setBuffer(buffBack.getBufferId());
        soundMgr.addSoundSource(Sounds.MUSIC.toString(), sourceBack);
        
        SoundBuffer buffBeep = new SoundBuffer("/main/resources/sounds/beep.ogg");
        soundMgr.addSoundBuffer(buffBeep);
        SoundSource sourceBeep = new SoundSource(false, true);
        sourceBeep.setBuffer(buffBeep.getBufferId());
        soundMgr.addSoundSource(Sounds.BEEP.toString(), sourceBeep);
        
        SoundBuffer buffFire = new SoundBuffer("/main/resources/sounds/fire.ogg");
        soundMgr.addSoundBuffer(buffFire);
        SoundSource sourceFire = new SoundSource(true, false);
        Vector3f pos = particleEmitter.getBaseParticle().getPosition();
        sourceFire.setPosition(pos);
        sourceFire.setBuffer(buffFire.getBufferId());
        soundMgr.addSoundSource(Sounds.FIRE.toString(), sourceFire);
        sourceFire.play();
        
        soundMgr.setListener(new SoundListener(new Vector3f()));

        sourceBack.play();        
    }
    
    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.setShadowPosMult(5);
        directionalLight.setOrthoCords(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f);
        sceneLight.setDirectionalLight(directionalLight);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis) {
        if (!engineProperties.useVulkan()) {
        	if (mHud != null) {
        		mHud.input(window);
        	}
        	cameraInc.set(0, 0, 0);
            if (window.isKeyPressed(GLFW_KEY_W)) {
                cameraInc.z = -1;
            } else if (window.isKeyPressed(GLFW_KEY_S)) {
                cameraInc.z = 1;
            }
            if (window.isKeyPressed(GLFW_KEY_A)) {
                cameraInc.x = -1;
            } else if (window.isKeyPressed(GLFW_KEY_D)) {
                cameraInc.x = 1;
            }
            if (window.isKeyPressed(GLFW_KEY_Z)) {
                cameraInc.y = -1;
            } else if (window.isKeyPressed(GLFW_KEY_X)) {
                cameraInc.y = 1;
            }
            if (window.isKeyPressed(GLFW_KEY_LEFT)) {
                angleInc -= 0.05f;
                soundMgr.playSoundSource(Sounds.BEEP.toString());
            } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
                angleInc += 0.05f;
                soundMgr.playSoundSource(Sounds.BEEP.toString());
            } else {
                angleInc = 0;
            }
        } else {
        	angleInc += 1.0f;
            if (angleInc >= 360) {
                angleInc = angleInc - 360;
            }
            cube.getRotation().identity().rotateAxis((float) Math.toRadians(angleInc), rotatingAngle);
            cube.buildModelMatrix();
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
            Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
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
        }
    }

    @Override
    public void render(Window window, Scene scene, IRenderer renderer) {
    	this.scene = scene;
        renderer.render(window, camera, scene);
        if (mHud != null) {
        	mHud.render(window);
        }
        if (gHud != null) {
        	gHud.render(window);
        }
    }

    @Override
    public void cleanup(IRenderer renderer) {
    	renderer.cleanup();
    	soundMgr.cleanup();
        scene.cleanup();
        if ( mHud != null ) {
            mHud.cleanup();
        }
        if ( gHud != null ) {
            gHud.cleanup();
        }
    }
}
