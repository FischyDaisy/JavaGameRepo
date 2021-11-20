package main.engine.graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL14C.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14C.glBlendEquation;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.nuklear.Nuklear.NK_ANTI_ALIASING_ON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import main.engine.Scene;
import main.engine.SceneLight;
import main.engine.Window;
import main.engine.graphics.FrustumCullingFilter;
import main.engine.graphics.IHud;
import main.engine.graphics.IRenderer;
import main.engine.graphics.ModelData;
import main.engine.graphics.TextureCache;
import main.engine.graphics.Transformation;
import main.engine.graphics.animation.AnimGameItem;
import main.engine.graphics.animation.AnimatedFrame;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.hud.MenuHud;
import main.engine.graphics.lights.DirectionalLight;
import main.engine.graphics.lights.PointLight;
import main.engine.graphics.lights.SpotLight;
import main.engine.graphics.opengl.GLModel.GLMaterial;
import main.engine.graphics.particles.IParticleEmitter;
import main.engine.graphics.vulkan.VulkanModel;
import main.engine.items.GameItem;
import main.engine.items.Portal;
import main.engine.items.SkyBox;
import main.engine.utility.Utils;
import static main.engine.utility.ResourcePaths.Shaders;

public class GLRenderer implements IRenderer {

    /**
     * Field of View in Radians
     */
    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.f;
    
    private static final int MAX_POINT_LIGHTS = 5;

    private static final int MAX_SPOT_LIGHTS = 5;
    
    private ShadowMap shadowMap;

    private ShaderProgram depthShaderProgram;

    private ShaderProgram sceneShaderProgram;
    
    private ShaderProgram skyBoxShaderProgram;
    
    private ShaderProgram particlesShaderProgram;
    
    private ShaderProgram portalErrShaderProgram;
    
    private ShaderProgram portalShaderProgram;
    
    private final TextureCache textureCache;
    
    private final Transformation transformation;
    
    private final float specularPower;
    
    private final FrustumCullingFilter frustumFilter;

    private final List<GameItem> filteredItems;
    
    private final List<GLModel> glModels;

    public GLRenderer() {
    	transformation = new Transformation();
    	textureCache = new TextureCache();
    	specularPower = 10f;
    	frustumFilter = new FrustumCullingFilter();
        filteredItems = new ArrayList<GameItem>();
        glModels = new ArrayList<GLModel>();
    }

    @Override
    public void init(Window window, Scene scene) throws Exception {
    	shadowMap = new ShadowMap();
    	
    	setupDepthShader();
    	setupSkyBoxShader();
    	setupSceneShader();
    	setupParticlesShader();
    	setupPortalErrShader();
    	setupPortalShader();
    }
    
    @Override
    public void render(Window window, Camera camera, Scene scene) {
        clear();
        
        frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
        frustumFilter.filter(scene.getPortalMeshes());
        frustumFilter.filter(scene.getGameMeshes());
        frustumFilter.filter(scene.getGameInstancedMeshes());
        
        // Render depth map before view ports has been set up
        renderDepthMap(window, camera, scene);

        glViewport(0, 0, window.getWidth(), window.getHeight());
        
        // Update projection matrix once per render cycle
        window.updateProjectionMatrix();
        
        //renderPortals(window, camera, scene, 0, 20);
        
        frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
        frustumFilter.filter(scene.getPortalMeshes());
        frustumFilter.filter(scene.getGameMeshes());
        frustumFilter.filter(scene.getGameInstancedMeshes());

        renderScene(window, camera, scene);
        renderSkyBox(window, camera, scene);
        renderParticles(window, camera, scene);
        
        //renderAxes(camera);
        //renderCrossHair(window);
    }
    
    private void setupPortalErrShader() throws Exception {
    	portalErrShaderProgram = new ShaderProgram();
    	portalErrShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.PINK_VERTEX));
    	portalErrShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.PINK_FRAGMENT));
    	portalErrShaderProgram.link();
    	
    	portalErrShaderProgram.createUniform("modelViewMatrix");
    	portalErrShaderProgram.createUniform("projectionMatrix");
    	
    	//portalErrShaderProgram.createUniform("texture_sampler");
    }
    
    private void setupPortalShader() throws Exception {
    	portalShaderProgram = new ShaderProgram();
    	portalShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.PORTAL_VERTEX));
    	portalShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.PORTAL_FRAGMENT));
    	portalShaderProgram.link();
    	
    	portalShaderProgram.createUniform("modelViewMatrix");
    	portalShaderProgram.createUniform("projectionMatrix");
    	
    	portalShaderProgram.createUniform("texture_sampler");
    }
    
    private void setupParticlesShader() throws Exception {
        particlesShaderProgram = new ShaderProgram();
        particlesShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.PARTICLES_VERTEX));
        particlesShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.PARTICLES_FRAGMENT));
        particlesShaderProgram.link();

        particlesShaderProgram.createUniform("projectionMatrix");
        particlesShaderProgram.createUniform("texture_sampler");
        
        particlesShaderProgram.createUniform("numCols");
        particlesShaderProgram.createUniform("numRows");
    }
    
    private void setupDepthShader() throws Exception {
        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.DEPTH_VERTEX));
        depthShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.DEPTH_FRAGMENT));
        depthShaderProgram.link();

        depthShaderProgram.createUniform("isInstanced");
        depthShaderProgram.createUniform("jointsMatrix");
        depthShaderProgram.createUniform("modelLightViewNonInstancedMatrix");
        depthShaderProgram.createUniform("orthoProjectionMatrix");
    }
    
    private void setupSkyBoxShader() throws Exception {
        skyBoxShaderProgram = new ShaderProgram();
        skyBoxShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.SB_VERTEX));
        skyBoxShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.SB_FRAGMENT));
        skyBoxShaderProgram.link();

        // Create uniforms for projection matrix
        skyBoxShaderProgram.createUniform("projectionMatrix");
        skyBoxShaderProgram.createUniform("modelViewMatrix");
        skyBoxShaderProgram.createUniform("texture_sampler");
        skyBoxShaderProgram.createUniform("ambientLight");
        skyBoxShaderProgram.createUniform("color");
        skyBoxShaderProgram.createUniform("hasTexture");
    }
    
    private void setupSceneShader() throws Exception {
        // Create shader
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.SCENE_VERTEX));
        sceneShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.SCENE_FRAGMENT));
        sceneShaderProgram.link();

        // Create uniforms for modelView and projection matrices and texture
        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("modelViewNonInstancedMatrix");
        sceneShaderProgram.createUniform("texture_sampler");
        sceneShaderProgram.createUniform("normalMap");
        
        // Create uniform for material
        sceneShaderProgram.createMaterialUniform("material");
        
        // Create lighting related uniforms
        sceneShaderProgram.createUniform("specularPower");
        sceneShaderProgram.createUniform("ambientLight");
        sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        sceneShaderProgram.createDirectionalLightUniform("directionalLight");
        sceneShaderProgram.createFogUniform("fog");
        
        // Create uniforms for shadow mapping
        sceneShaderProgram.createUniform("shadowMap");
        sceneShaderProgram.createUniform("orthoProjectionMatrix");
        sceneShaderProgram.createUniform("modelLightViewNonInstancedMatrix");
        sceneShaderProgram.createUniform("renderShadow");
        
        // Create uniform for joint matrices
        sceneShaderProgram.createUniform("jointsMatrix");

        sceneShaderProgram.createUniform("isInstanced");
        sceneShaderProgram.createUniform("numCols");
        sceneShaderProgram.createUniform("numRows");
        
        // Create uniform for object selection
        sceneShaderProgram.createUniform("selectedNonInstanced");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }
    
    @Override
    public void loadModels(List<ModelData> modelDataList) throws Exception {
    	glModels.addAll(GLModel.transformModels(modelDataList, textureCache));
    }
    
    @Override
    public void clearAndLoadModels(List<ModelData> modelDataList) throws Exception {
    	glModels.clear();
    	loadModels(modelDataList);
    }
    
    private void renderParticles(Window window, Camera camera, Scene scene) {
        if (scene.getParticleEmitters() != null) {
        	particlesShaderProgram.bind();

            particlesShaderProgram.setUniform("texture_sampler", 0);
            Matrix4f projectionMatrix = window.getProjectionMatrix();
            particlesShaderProgram.setUniform("projectionMatrix", projectionMatrix);

            Matrix4f viewMatrix = camera.getViewMatrix();
            IParticleEmitter[] emitters = scene.getParticleEmitters();
            int numEmitters = emitters != null ? emitters.length : 0;

            glDepthMask(false);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);

            for (int i = 0; i < numEmitters; i++) {
                IParticleEmitter emitter = emitters[i];
                InstancedMesh mesh = (InstancedMesh)emitter.getBaseParticle().getMesh();
                
                GLTexture text = mesh.getMaterial().getTexture();
                particlesShaderProgram.setUniform("numCols", text.getNumCols());
                particlesShaderProgram.setUniform("numRows", text.getNumRows());

                mesh.renderListInstanced(emitter.getParticles(), true, transformation, viewMatrix, null);
            }

            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDepthMask(true);

            particlesShaderProgram.unbind();
        }
    }
    
    private void renderDepthMap(Window window, Camera camera, Scene scene) {
    	if (scene.isRenderShadows()) {
            // Setup view port to match the texture size
            glBindFramebuffer(GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
            glViewport(0, 0, ShadowMap.SHADOW_MAP_WIDTH, ShadowMap.SHADOW_MAP_HEIGHT);
            glClear(GL_DEPTH_BUFFER_BIT);

            depthShaderProgram.bind();

            DirectionalLight light = scene.getSceneLight().getDirectionalLight();
            Vector3f lightDirection = light.getDirection();

            float lightAngleX = (float) Math.toDegrees(Math.acos(lightDirection.z));
            float lightAngleY = (float) Math.toDegrees(Math.asin(lightDirection.x));
            float lightAngleZ = 0;
            Matrix4f lightViewMatrix = transformation.updateLightViewMatrix(new Vector3f(lightDirection).mul(light.getShadowPosMult()), new Vector3f(lightAngleX, lightAngleY, lightAngleZ));
            DirectionalLight.OrthoCoords orthCoords = light.getOrthoCoords();
            Matrix4f orthoProjMatrix = transformation.updateOrthoProjectionMatrix(orthCoords.left, orthCoords.right, orthCoords.bottom, orthCoords.top, orthCoords.near, orthCoords.far);

            depthShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);

            renderNonInstancedMeshes(scene, depthShaderProgram, null, lightViewMatrix);

            renderInstancedMeshes(scene, depthShaderProgram, null, lightViewMatrix);

            // Unbind
            depthShaderProgram.unbind();
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }
    
    private void renderSkyBox(Window window, Camera camera, Scene scene) {
    	SkyBox skyBox = scene.getSkyBox();
        if (skyBox != null) {
            skyBoxShaderProgram.bind();

            skyBoxShaderProgram.setUniform("texture_sampler", 0);

            Matrix4f projectionMatrix = window.getProjectionMatrix();
            skyBoxShaderProgram.setUniform("projectionMatrix", projectionMatrix);
            Matrix4f viewMatrix = camera.getViewMatrix();
            float m30 = viewMatrix.m30();
            viewMatrix.m30(0);
            float m31 = viewMatrix.m31();
            viewMatrix.m31(0);
            float m32 = viewMatrix.m32();
            viewMatrix.m32(0);
            
            Mesh mesh = skyBox.getMesh();
            Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
            skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLight().getSkyBoxLight());
            skyBoxShaderProgram.setUniform("color", mesh.getMaterial().getAmbientColor());
            skyBoxShaderProgram.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);

            mesh.render();

            viewMatrix.m30(m30);
            viewMatrix.m31(m31);
            viewMatrix.m32(m32);
            
            skyBoxShaderProgram.unbind();
        }
    }
    
    private void renderSkyBox(Matrix4f projMat, Camera camera, Scene scene) {
    	SkyBox skyBox = scene.getSkyBox();
        if (skyBox != null) {
            skyBoxShaderProgram.bind();

            skyBoxShaderProgram.setUniform("texture_sampler", 0);

            Matrix4f projectionMatrix = projMat;
            skyBoxShaderProgram.setUniform("projectionMatrix", projectionMatrix);
            Matrix4f viewMatrix = camera.getViewMatrix();
            float m30 = viewMatrix.m30();
            viewMatrix.m30(0);
            float m31 = viewMatrix.m31();
            viewMatrix.m31(0);
            float m32 = viewMatrix.m32();
            viewMatrix.m32(0);
            
            Mesh mesh = skyBox.getMesh();
            Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
            skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLight().getSkyBoxLight());
            skyBoxShaderProgram.setUniform("color", mesh.getMaterial().getAmbientColor());
            skyBoxShaderProgram.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);

            mesh.render();

            viewMatrix.m30(m30);
            viewMatrix.m31(m31);
            viewMatrix.m32(m32);
            
            skyBoxShaderProgram.unbind();
        }
    }
    
    public void renderScene(Window window, Camera camera, Scene scene) {

        sceneShaderProgram.bind();

        // Update projection Matrix
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        Matrix4f orthoProjMatrix = transformation.getOrthoProjectionMatrix();
        sceneShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);
        Matrix4f lightViewMatrix = transformation.getLightViewMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();

        SceneLight sceneLight = scene.getSceneLight();
        renderLights(viewMatrix, sceneLight);

        sceneShaderProgram.setUniform("fog", scene.getFog());
        sceneShaderProgram.setUniform("texture_sampler", 0);
        sceneShaderProgram.setUniform("normalMap", 1);
        sceneShaderProgram.setUniform("shadowMap", 2);
        sceneShaderProgram.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);
        
        renderNonInstancedMeshes(scene, sceneShaderProgram, viewMatrix, lightViewMatrix);

        renderInstancedMeshes(scene, sceneShaderProgram, viewMatrix, lightViewMatrix);

        sceneShaderProgram.unbind();
    }
    
    private void renderScene(Matrix4f projMatrix, Camera camera, Scene scene) {

        sceneShaderProgram.bind();

        // Update projection Matrix
        Matrix4f projectionMatrix = projMatrix;
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        Matrix4f orthoProjMatrix = transformation.getOrthoProjectionMatrix();
        sceneShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);
        Matrix4f lightViewMatrix = transformation.getLightViewMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();

        SceneLight sceneLight = scene.getSceneLight();
        renderLights(viewMatrix, sceneLight);

        sceneShaderProgram.setUniform("fog", scene.getFog());
        sceneShaderProgram.setUniform("texture_sampler", 0);
        sceneShaderProgram.setUniform("normalMap", 1);
        sceneShaderProgram.setUniform("shadowMap", 2);
        sceneShaderProgram.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);
        
        renderNonInstancedMeshes(scene, sceneShaderProgram, viewMatrix, lightViewMatrix);

        renderInstancedMeshes(scene, sceneShaderProgram, viewMatrix, lightViewMatrix);

        sceneShaderProgram.unbind();
    }
    
    private void renderNonInstancedMeshes(Scene scene, ShaderProgram shader, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        sceneShaderProgram.setUniform("isInstanced", 0);

        // Render each mesh with the associated game Items
        //Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        Map<String, List<GameItem>> mapMeshes = scene.getModelMap();
        for (GLModel model : glModels) {
        	String modelId = model.getModelId();
        	List<GameItem> items = scene.getGameItemsByModelId(modelId);
        	if (items.isEmpty()) {
        		continue;
        	}
            for (GLMaterial material : model.getGLMaterialList()) {
            	if (material.glMeshList().isEmpty()) {
            		continue;
            	}
            	if (viewMatrix != null) {
                    shader.setUniform("material", material);
                    glActiveTexture(GL_TEXTURE2);
                    glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getId());
                }

                GLTexture text = material.texture();
                if (text != null) {
                    sceneShaderProgram.setUniform("numCols", text.getNumCols());
                    sceneShaderProgram.setUniform("numRows", text.getNumRows());
                }

                model.renderList(mapMeshes.get(modelId), material, (GameItem gameItem) -> {
                	sceneShaderProgram.setUniform("selectedNonInstanced", gameItem.isSelected() ? 1.0f : 0.0f);
                    Matrix4f modelMatrix = gameItem.buildModelMatrix();
                    if (viewMatrix != null) {
                        Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
                        sceneShaderProgram.setUniform("modelViewNonInstancedMatrix", modelViewMatrix);
                    }
                    Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
                    sceneShaderProgram.setUniform("modelLightViewNonInstancedMatrix", modelLightViewMatrix);

                    if (gameItem instanceof AnimGameItem) {
                        AnimGameItem animGameItem = (AnimGameItem) gameItem;
                        AnimatedFrame frame = animGameItem.getCurrentFrame();
                        shader.setUniform("jointsMatrix", frame.getJointMatrices());
                    }
                });
            }
        }
    }

    private void renderInstancedMeshes(Scene scene, ShaderProgram shader, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        shader.setUniform("isInstanced", 1);

        // Render each mesh with the associated game Items
        Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
        for (InstancedMesh mesh : mapMeshes.keySet()) {
            GLTexture text = mesh.getMaterial().getTexture();
            if (text != null) {
                sceneShaderProgram.setUniform("numCols", text.getNumCols());
                sceneShaderProgram.setUniform("numRows", text.getNumRows());
            }

            if (viewMatrix != null) {
                shader.setUniform("material", mesh.getMaterial());
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getId());
            }

            filteredItems.clear();
            for(GameItem gameItem : mapMeshes.get(mesh)) {
                if ( gameItem.isInsideFrustum() ) {
                    filteredItems.add(gameItem);
                }
            }
            mesh.renderListInstanced(filteredItems, transformation, viewMatrix, lightViewMatrix);
        }
    }
    
    private void renderLights(Matrix4f viewMatrix, SceneLight sceneLight) {

    	sceneShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
    	sceneShaderProgram.setUniform("specularPower", specularPower);

        // Process Point Lights
    	PointLight[] pointLightList = sceneLight.getPointLightList();
        int numLights = pointLightList != null ? pointLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the point light object and transform its position to view coordinates
            PointLight currPointLight = new PointLight(pointLightList[i]);
            Vector3f lightPos = currPointLight.getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            sceneShaderProgram.setUniform("pointLights", currPointLight, i);
        }

        // Process Spot Ligths
        SpotLight[] spotLightList = sceneLight.getSpotLightList();
        numLights = spotLightList != null ? spotLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the spot light object and transform its position and cone direction to view coordinates
            SpotLight currSpotLight = new SpotLight(spotLightList[i]);
            Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
            dir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));
            Vector3f lightPos = currSpotLight.getPointLight().getPosition();

            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;

            sceneShaderProgram.setUniform("spotLights", currSpotLight, i);
        }

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        sceneShaderProgram.setUniform("directionalLight", currDirLight);

    }
    
    private void renderPortalsPink(Window window, Camera camera, Scene scene) {
    	portalErrShaderProgram.bind();
    	
    	Matrix4f projectionMatrix = window.getProjectionMatrix();
    	portalErrShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        Matrix4f viewMatrix = camera.getViewMatrix();
        
        Map<Mesh, List<GameItem>> portalMap = scene.getPortalMeshes();
        for (Mesh mesh : portalMap.keySet()) {
        	mesh.renderList(portalMap.get(mesh), (GameItem gameItem) -> {
        		Matrix4f modelMatrix = gameItem.buildModelMatrix();
        		Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
        		portalErrShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        	});
        }
        portalErrShaderProgram.unbind();
    }
    
    private void renderPortalPink(Window window, Camera camera, Scene scene, Portal rPortal) {
    	portalErrShaderProgram.bind();
    	
    	Matrix4f projectionMatrix = window.getProjectionMatrix();
    	portalErrShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        Matrix4f viewMatrix = camera.getViewMatrix();
        
        Matrix4f modelMatrix = rPortal.buildModelMatrix();
		Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
		portalErrShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
		
		rPortal.getMesh().render();
		
        portalErrShaderProgram.unbind();
    }
    
    private void renderPortals(Window window, Camera camera, Scene scene, int recursionLevel, int maxRecursion) {
    	//portalShaderProgram.bind();
    	//renderPortalList(window, camera, scene, skipPortal, 0);
    	//portalShaderProgram.unbind();
    	
    	List<GameItem> filteredPortals = new ArrayList<GameItem>();
    	frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
        frustumFilter.filter(scene.getPortalMeshes());
        frustumFilter.filter(scene.getGameMeshes());
        frustumFilter.filter(scene.getGameInstancedMeshes());
        
        Map<Mesh, List<GameItem>> portalMap = scene.getPortalMeshes();
    	for (Mesh mesh : portalMap.keySet()) {
    		filteredPortals.clear();
            for(GameItem portal : portalMap.get(mesh)) {
                if ( portal.isInsideFrustum() ) {
                	filteredPortals.add(portal);
                }
            }
    		mesh.renderList(filteredPortals, (GameItem portal) -> {
    			Portal p = (Portal) portal;
    			
    			// Disable color and depth drawing
    			glColorMask(false, false, false, false);
    			glDepthMask(false);

    			// Disable depth test
    			glDisable(GL_DEPTH_TEST);
    			
    			// Enable stencil test, to prevent drawing outside
    			// region of current portal depth
    			glEnable(GL_STENCIL_TEST);
    			
    			// Fail stencil test when inside of outer portal
    			// (fail where we should be drawing the inner portal)
    			glStencilFunc(GL_NOTEQUAL, recursionLevel, 0xFF);
    			
    			// Increment stencil value on stencil fail
    			// (on area of inner portal)
    			glStencilOp(GL_INCR, GL_KEEP, GL_KEEP);
    			
    			// Enable (writing into) all stencil bits
    			glStencilMask(0xFF);
    			
    			renderPortalPink(window, camera, scene, p);
    			
    			//Make pCam
    			Camera pCam = camera.createPortalCam(p, p.getWarp().toPortal());
    			pCam.updateViewMatrixQuat();
    			
    			if (recursionLevel == maxRecursion || (!scene.containsPortals())) {
    				// Enable color and depth drawing
    				glColorMask(true, true, true, true);
    				glDepthMask(true);
    				
    				// Clear the depth buffer so we don't interfere with stuff
    				// outside of this inner portal
    				glClear(GL_DEPTH_BUFFER_BIT);

    				// Enable the depth test
    				// So the stuff we render here is rendered correctly
    				glEnable(GL_DEPTH_TEST);

    				// Enable stencil test
    				// So we can limit drawing inside of the inner portal
    				glEnable(GL_STENCIL_TEST);

    				// Disable drawing into stencil buffer
    				glStencilMask(0x00);

    				// Draw only where stencil value == recursionLevel + 1
    				// which is where we just drew the new portal
    				glStencilFunc(GL_EQUAL, recursionLevel + 1, 0xFF);
    				
    				renderScene(pCam.clipOblique(window, p), pCam, scene);
    				renderSkyBox(pCam.clipOblique(window, p), pCam, scene);
    			} else {
    				//renderPortals
    				renderPortals(window, pCam, scene, recursionLevel + 1, maxRecursion);
    			}
    			
    			// Disable color and depth drawing
    			glColorMask(false, false, false, false);
    			glDepthMask(false);

    			// Enable stencil test and stencil drawing
    			glEnable(GL_STENCIL_TEST);
    			glStencilMask(0xFF);

    			// Fail stencil test when inside of our newly rendered
    			// inner portal
    			glStencilFunc(GL_NOTEQUAL, recursionLevel + 1, 0xFF);

    			// Decrement stencil value on stencil fail
    			// This resets the incremented values to what they were before,
    			// eventually ending up with a stencil buffer full of zero's again
    			// after the last (outer) step.
    			glStencilOp(GL_DECR, GL_KEEP, GL_KEEP);
    			
    			renderPortalPink(window, camera, scene, p);
    		});
    		
    		// Disable the stencil test and stencil writing
    		glDisable(GL_STENCIL_TEST);
    		glStencilMask(0x00);

    		// Disable color writing
    		glColorMask(false, false, false, false);

    		// Enable the depth test, and depth writing.
    		glEnable(GL_DEPTH_TEST);
    		glDepthMask(true);

    		// Make sure we always write the data into the buffer
    		glDepthFunc(GL_ALWAYS);

    		// Clear the depth buffer
    		glClear(GL_DEPTH_BUFFER_BIT);
    		
    		renderPortalsPink(window, camera, scene);
    		
    		// Reset the depth function to the default
    		glDepthFunc(GL_LESS);

    		// Enable stencil test and disable writing to stencil buffer
    		glEnable(GL_STENCIL_TEST);
    		glStencilMask(0x00);

    		// Draw at stencil >= recursionlevel
    		// which is at the current level or higher (more to the inside)
    		// This basically prevents drawing on the outside of this level.
    		glStencilFunc(GL_LEQUAL, recursionLevel, 0xFF);

    		// Enable color and depth drawing again
    		glColorMask(true, true, true, true);
    		glDepthMask(true);

    		// And enable the depth test
    		glEnable(GL_DEPTH_TEST);
    		
    		if (recursionLevel != 0) {
    			renderScene(window, camera, scene);
    			renderSkyBox(window, camera, scene);
    		}
    	}
    }
    
    public GLTexture getShadowMapTexture() {
    	return shadowMap.getDepthMapTexture();
    }

    @Override
    public void cleanup() {
    	if (shadowMap != null) {
            shadowMap.cleanup();
        }
        if (depthShaderProgram != null) {
            depthShaderProgram.cleanup();
        }
        if (skyBoxShaderProgram != null) {
            skyBoxShaderProgram.cleanup();
        }
        if (sceneShaderProgram != null) {
            sceneShaderProgram.cleanup();
        }
        if (particlesShaderProgram != null) {
            particlesShaderProgram.cleanup();
        }
    }
}
