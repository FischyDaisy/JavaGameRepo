package main.engine.graphics.opengl.shadows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Renderer;

import org.joml.Matrix4f;

import main.engine.EngineProperties;
import main.engine.Scene;
import main.engine.SceneLight;
import main.engine.Window;
import main.engine.graphics.ModelData;
import main.engine.graphics.TextureCache;
import main.engine.graphics.Transformation;
import main.engine.graphics.animation.AnimGameItem;
import main.engine.graphics.animation.AnimatedFrame;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.lights.DirectionalLight;
import main.engine.graphics.opengl.GLModel;
import main.engine.graphics.opengl.GLTexture;
import main.engine.graphics.opengl.InstancedGLModel;
import main.engine.graphics.opengl.Mesh;
import main.engine.graphics.opengl.ShaderProgram;
import main.engine.graphics.opengl.GLModel.GLMaterial;
import main.engine.graphics.opengl.GLRenderer;
import main.engine.items.GameItem;
import main.engine.utility.Utils;
import main.engine.utility.ResourcePaths.Shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;

public class ShadowRenderer {
	
	private static final EngineProperties props = EngineProperties.getInstance();

    public static final int NUM_CASCADES = 3;

    public static final float[] CASCADE_SPLITS = new float[]{props.getZFar() / 20.0f, props.getZFar() / 10.0f, props.getZFar()};

    private ShaderProgram depthShaderProgram;

    private List<ShadowCascade> shadowCascades;

    private ShadowBuffer shadowBuffer;
    
    private final List<GLModel> glModels;
    
    private final List<InstancedGLModel> glInstancedModels;

    private final List<GameItem> filteredItems;

    public ShadowRenderer() {
    	glModels = new ArrayList<GLModel>();
        glInstancedModels = new ArrayList<InstancedGLModel>();
        filteredItems = new ArrayList<GameItem>();
    }

    public void init() throws Exception {
        shadowBuffer = new ShadowBuffer();
        shadowCascades = new ArrayList<ShadowCascade>();

        setupDepthShader();

        float zNear = props.getZNear();
        for (int i = 0; i < NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = new ShadowCascade(zNear, CASCADE_SPLITS[i]);
            shadowCascades.add(shadowCascade);
            zNear = CASCADE_SPLITS[i];
        }
    }

    public List<ShadowCascade> getShadowCascades() {
        return shadowCascades;
    }

    public void bindTextures(int start) {
        this.shadowBuffer.bindTextures(start);
    }

    private void setupDepthShader() throws Exception {
        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.DEPTH_VERTEX));
        depthShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.DEPTH_FRAGMENT));
        depthShaderProgram.link();

        depthShaderProgram.createUniform("isInstanced");
        depthShaderProgram.createUniform("modelNonInstancedMatrix");
        depthShaderProgram.createUniform("lightViewMatrix");
        depthShaderProgram.createUniform("jointsMatrix");
        depthShaderProgram.createUniform("orthoProjectionMatrix");
    }

    private void update(Window window, Matrix4f viewMatrix, Scene scene) {
        SceneLight sceneLight = scene.getSceneLight();
        DirectionalLight directionalLight = sceneLight != null ? sceneLight.getDirectionalLight() : null;
        for (int i = 0; i < NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);
            shadowCascade.update(window, viewMatrix, directionalLight);
        }
    }
    
    public void loadModels(List<GLModel> glModels) {
    	this.glModels.addAll(glModels);
    }
    
    public void clearModels() {
    	this.glModels.clear();
    }
    
    public void loadInstancedModels(List<InstancedGLModel> glInstancedModels) {
    	this.glInstancedModels.addAll(glInstancedModels);
    }
    
    public void clearInstancedModels() {
    	this.glInstancedModels.clear();
    }

    public void render(Window window, Scene scene, Camera camera, Transformation transformation, GLRenderer renderer) {
        update(window, camera.getViewMatrix(), scene);

        // Setup view port to match the texture size
        glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer.getDepthMapFBO());
        glViewport(0, 0, ShadowBuffer.SHADOW_MAP_WIDTH, ShadowBuffer.SHADOW_MAP_HEIGHT);
        glClear(GL_DEPTH_BUFFER_BIT);

        depthShaderProgram.bind();

        // Render scene for each cascade map
        for (int i = 0; i < NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);

            depthShaderProgram.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix());
            depthShaderProgram.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix());

            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowBuffer.getDepthMapTexture().getIds()[i], 0);
            glClear(GL_DEPTH_BUFFER_BIT);

            renderNonInstancedMeshes(scene, transformation);

            renderInstancedMeshes(scene, transformation);
        }

        // Unbind
        depthShaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void renderNonInstancedMeshes(Scene scene, Transformation transformation) {
        depthShaderProgram.setUniform("isInstanced", 0);

        // Render each mesh with the associated game Items
        Map<String, List<GameItem>> modelMap = scene.getModelMap();
        for (GLModel model : glModels) {
        	String modelId = model.getModelId();
        	List<GameItem> items = modelMap.get(modelId);
        	if (items.isEmpty()) {
        		continue;
        	}
        	for (GLMaterial material : model.getGLMaterialList()) {
                model.renderList(items, material, (GameItem gameItem) -> {
                    Matrix4f modelMatrix = gameItem.buildModelMatrix();
                    depthShaderProgram.setUniform("modelNonInstancedMatrix", modelMatrix);
                    if (gameItem instanceof AnimGameItem) {
                        AnimGameItem animGameItem = (AnimGameItem) gameItem;
                        AnimatedFrame frame = animGameItem.getCurrentAnimation().getCurrentFrame();
                        depthShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
                    }
                });
            }
        }
    }

    private void renderInstancedMeshes(Scene scene, Transformation transformation) {
        depthShaderProgram.setUniform("isInstanced", 1);

        // Render each mesh with the associated game Items
        Map<String, List<GameItem>> modelMap = scene.getInstancedModelMap();
        for (InstancedGLModel model : glInstancedModels) {
        	String modelId = model.getModelId();
        	List<GameItem> items = modelMap.get(modelId);
        	if (items.isEmpty()) {
        		continue;
        	}
        	for (GLMaterial material : model.getGLMaterialList()) {
        		filteredItems.clear();
                for (GameItem gameItem : items) {
                    if (gameItem.isInsideFrustum()) {
                        filteredItems.add(gameItem);
                    }
                }
                bindTextures(GL_TEXTURE2);

                model.renderListInstanced(filteredItems, transformation, null, material);
        	}
        }
    }

    public void cleanup() {
        if (shadowBuffer != null) {
            shadowBuffer.cleanup();
        }
        if (depthShaderProgram != null) {
            depthShaderProgram.cleanup();
        }
    }

}