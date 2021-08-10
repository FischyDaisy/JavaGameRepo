package main.game;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.Version;

import main.engine.EngineProperties;
import main.engine.IGameLogic;
import main.engine.MouseInput;
import main.engine.Scene;
import main.engine.SceneLight;
import main.engine.Window;
import main.engine.graphics.Camera;
import main.engine.graphics.IRenderer;
import main.engine.graphics.Material;
import main.engine.graphics.animation.AnimGameItem;
import main.engine.graphics.lights.DirectionalLight;
import main.engine.graphics.opengl.Mesh;
import main.engine.graphics.opengl.Texture;
import main.engine.graphics.particles.FlowParticleEmitter;
import main.engine.graphics.particles.Particle;
import main.engine.graphics.opengl.GLRenderer;
import main.engine.items.GameItem;
import main.engine.items.Terrain;
import main.engine.loaders.md5.MD5AnimModel;
import main.engine.loaders.md5.MD5Loader;
import main.engine.loaders.md5.MD5Model;
import main.engine.loaders.obj.OBJLoader;

public class Game implements IGameLogic {
	
	private static final EngineProperties engineProperties = EngineProperties.getInstance();
	
	private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;
    
    private final Camera camera;

    private Scene scene;
    
    private Hud hud;

    private float lightAngle;
    
    private static final float CAMERA_POS_STEP = 0.05f;
    
    private Terrain terrain;
    
    private float angleInc;
    
    private FlowParticleEmitter particleEmitter;
    
    private AnimGameItem monster;
    
    public Game() {
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        angleInc = 0;
        lightAngle = 45;;
    }
    
    @Override
    public void init(Window window, Scene scene, IRenderer renderer) throws Exception {
        renderer.init(window, scene);
        
        this.scene = scene;
        
        if (!engineProperties.useVulkan()) {
        	// Setup  GameItems
            float reflectance = 1f;
            
            Mesh quadMesh = OBJLoader.loadMesh("/main/resources/models/plane.obj");
            Material quadMaterial = new Material(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f), reflectance);
            quadMesh.setMaterial(quadMaterial);
            GameItem quadGameItem = new GameItem(quadMesh);
            quadGameItem.setPosition(0, 0, 0);
            quadGameItem.setScale(2.5f);
            
            // Setup  GameItems
            MD5Model md5Meshodel = MD5Model.parse("/main/resources/models/monster.md5mesh");
            MD5AnimModel md5AnimModel = MD5AnimModel.parse("/main/resources/models/monster.md5anim");
            //MD5Model md5Meshodel = MD5Model.parse("/models/boblamp.md5mesh");
            //MD5AnimModel md5AnimModel = MD5AnimModel.parse("/models/boblamp.md5anim");
            
            monster = MD5Loader.process(md5Meshodel, md5AnimModel, new Vector4f(1, 1, 1, 1));
            monster.setScale(0.05f);
            monster.setRotation(90, 0, 90);
            //monster.setRotation(90, 0, 0);

            scene.setGameItems(new GameItem[] { quadGameItem} );
            
            Vector3f particleSpeed = new Vector3f(0, 1, 0);
            particleSpeed.mul(2.5f);
            long ttl = 4000000000L;
            int maxParticles = 200;
            long creationPeriodMillis = 300000000L;
            float range = 0.2f;
            float scale = 1.0f;
            Mesh partMesh = OBJLoader.loadMesh("/main/resources/models/particle.obj");
            Texture texture = new Texture(System.getProperty("user.dir") + "\\src\\main\\resources\\textures\\particle_tmp.png");
            Material partMaterial = new Material(texture, reflectance);
            partMesh.setMaterial(partMaterial);
            Particle particle = new Particle(partMesh, particleSpeed, ttl);
            particle.setScale(scale);
            particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodMillis);
            particleEmitter.setActive(true);
            particleEmitter.setPositionRndRange(range);
            particleEmitter.setSpeedAndRange(range);
            this.scene.setParticleEmitters(new FlowParticleEmitter[] {particleEmitter});
            
            // Setup Lights
            setupLights();
            
            // Create HUD
            Runtime.Version runtimeVersion = Runtime.version();
            String version = String.valueOf(runtimeVersion.version().get(0) + "." + runtimeVersion.version().get(1) + "." 
            		+ runtimeVersion.version().get(2));
            hud = new Hud("Java Runtime Version: " + version + " | LWJGL Version: " + Version.getVersion());
            //hud = new Hud(System.getProperties().getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe");
            
            camera.getPosition().x = 0.25f;
            camera.getPosition().y = 6.5f;
            camera.getPosition().z = 6.5f;
            camera.getRotation().x = 25;
            camera.getRotation().y = -1;
        }
        
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
            } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
                angleInc += 0.05f;
            } else {
                angleInc = 0;
            }
            if (window.isKeyPressed(GLFW_KEY_SPACE) ) {
                monster.nextFrame();
            }
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
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
            
            particleEmitter.update((long)(interval));
        }
    }

    @Override
    public void render(Window window, Scene scene, IRenderer renderer) {
    	this.scene = scene;
    	if (hud != null) {
            hud.updateSize(window);
        }
        renderer.render(window, camera, scene, hud);
    }

    @Override
    public void cleanup(IRenderer renderer) {
    	renderer.cleanup();
        scene.cleanup();
        if ( hud != null ) {
            hud.cleanup();
        }
    }
}
