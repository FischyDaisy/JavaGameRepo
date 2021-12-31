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

import main.engine.EngineProperties;
import main.engine.Scene;
import main.engine.SceneLight;
import main.engine.Window;
import main.engine.graphics.FrustumCullingFilter;
import main.engine.graphics.IHud;
import main.engine.graphics.ModelData;
import main.engine.graphics.Renderer;
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
import main.engine.graphics.opengl.shadows.ShadowCascade;
import main.engine.graphics.opengl.shadows.ShadowRenderer;
import main.engine.graphics.particles.IParticleEmitter;
import main.engine.graphics.vulkan.VulkanModel;
import main.engine.items.GameItem;
import main.engine.items.Portal;
import main.engine.items.SkyBox;
import main.engine.loaders.assimp.ModelLoader;
import main.engine.utility.ResourcePaths;
import main.engine.utility.Utils;
import static main.engine.utility.ResourcePaths.Shaders;

public class GLRenderer implements Renderer {
    
    private ShaderProgram skyBoxShaderProgram;
    
    private ShaderProgram particlesShaderProgram;
    
    private ShaderProgram portalErrShaderProgram;
    
    private ShaderProgram portalShaderProgram;
    
    private ShaderProgram gBufferShaderProgram;

    private ShaderProgram dirLightShaderProgram;

    private ShaderProgram pointLightShaderProgram;

    private ShaderProgram fogShaderProgram;
    
    private GLModel skyBoxModel;
    
    private final ShadowRenderer shadowRenderer;
    
    private final TextureCache textureCache;
    
    private final Transformation transformation;
    
    private final float specularPower;
    
    private final FrustumCullingFilter frustumFilter;

    private final List<GameItem> filteredItems;
    
    private final List<GLModel> glModels;
    
    private final List<InstancedGLModel> glInstancedModels;
    
    private final List<InstancedGLModel> particleModels;
    
    private GBuffer gBuffer;

    private SceneBuffer sceneBuffer;

    private GLModel bufferPassModel;

    private Matrix4f bufferPassModelMatrix;

    private Vector4f tmpVec;

    public GLRenderer(Window window) throws Exception {
    	transformation = new Transformation();
    	textureCache = TextureCache.getInstance();
    	specularPower = 10f;
    	frustumFilter = new FrustumCullingFilter();
        filteredItems = new ArrayList<GameItem>();
        glModels = new ArrayList<GLModel>();
        glInstancedModels = new ArrayList<InstancedGLModel>();
        particleModels = new ArrayList<InstancedGLModel>();
        tmpVec = new Vector4f();
        
        shadowRenderer = new ShadowRenderer();
        shadowRenderer.init();
    	
        gBuffer = new GBuffer(window);
        sceneBuffer = new SceneBuffer(window);
        setupSkyBoxShader();
        setupParticlesShader();
        setupGeometryShader();
        setupDirLightShader();
        setupPointLightShader();
        setupFogShader();

        bufferPassModelMatrix =  new Matrix4f();
        ModelData bufferPassData = ModelLoader.loadModel("buffer_pass_mesh", ResourcePaths.Models.BUFFER_PASS_OBJ,
        		ResourcePaths.Models.BUFFER_PASS_DIR);
        List<ModelData> list = new ArrayList<ModelData>();
        list.add(bufferPassData);
        bufferPassModel = GLModel.transformModels(list, textureCache).get(0);
    }
    
    public void render(Window window, Camera camera, Scene scene, boolean sceneChanged) {
        clear();
        
        if (window.getOptions().frustumCulling) {
        	frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
            //frustumFilter.filter(scene.getPortalMeshes());
            frustumFilter.filter(scene.getModelMap(), glModels);
            frustumFilter.filter(scene.getInstancedModelMap(), glInstancedModels);
        }
        
        // Render depth map before view ports has been set up
        if (scene.isRenderShadows() && sceneChanged) {
            shadowRenderer.render(window, scene, camera, transformation, this);
        }

        glViewport(0, 0, window.getWidth(), window.getHeight());
        
        // Update projection matrix once per render cycle
        window.updateProjectionMatrix();
        
        //renderPortals(window, camera, scene, 0, 20);
        
        // if (window.getOptions().frustumCulling) {
         	//frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
             //frustumFilter.filter(scene.getPortalMeshes());
             //frustumFilter.filter(scene.getModelMap(), glModels);
             //frustumFilter.filter(scene.getInstancedModelMap(), glInstancedModels);
         //}
        
        renderGeometry(window, camera, scene);

        initLightRendering();
        renderPointLights(window, camera, scene);
        renderDirectionalLight(window, camera, scene);
        endLightRendering();

        renderFog(window, camera, scene);
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

        particlesShaderProgram.createUniform("viewMatrix");
        particlesShaderProgram.createUniform("projectionMatrix");
        particlesShaderProgram.createUniform("texture_sampler");
        
        particlesShaderProgram.createUniform("numCols");
        particlesShaderProgram.createUniform("numRows");
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
        
        skyBoxShaderProgram.createUniform("depthsText");
        skyBoxShaderProgram.createUniform("screenSize");
    }
    
    private void setupGeometryShader() throws Exception {
        gBufferShaderProgram = new ShaderProgram();
        gBufferShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.GBUFFER_VERTEX));
        gBufferShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.GBUFFER_FRAGMENT));
        gBufferShaderProgram.link();

        gBufferShaderProgram.createUniform("projectionMatrix");
        gBufferShaderProgram.createUniform("viewMatrix");
        gBufferShaderProgram.createUniform("texture_sampler");
        gBufferShaderProgram.createUniform("normalMap");
        gBufferShaderProgram.createMaterialUniform("material");
        gBufferShaderProgram.createUniform("isInstanced");
        gBufferShaderProgram.createUniform("modelNonInstancedMatrix");
        gBufferShaderProgram.createUniform("selectedNonInstanced");
        gBufferShaderProgram.createUniform("jointsMatrix");
        gBufferShaderProgram.createUniform("numCols");
        gBufferShaderProgram.createUniform("numRows");

        // Create uniforms for shadow mapping
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            gBufferShaderProgram.createUniform("shadowMap_" + i);
        }
        gBufferShaderProgram.createUniform("orthoProjectionMatrix", ShadowRenderer.NUM_CASCADES);
        gBufferShaderProgram.createUniform("lightViewMatrix", ShadowRenderer.NUM_CASCADES);
        gBufferShaderProgram.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
        gBufferShaderProgram.createUniform("renderShadow");
    }

    private void setupDirLightShader() throws Exception {
        dirLightShaderProgram = new ShaderProgram();
        dirLightShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.LIGHT_VERTEX));
        dirLightShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.DIR_LIGHT_FRAGMENT));
        dirLightShaderProgram.link();

        dirLightShaderProgram.createUniform("modelMatrix");
        dirLightShaderProgram.createUniform("projectionMatrix");

        dirLightShaderProgram.createUniform("screenSize");
        dirLightShaderProgram.createUniform("positionsText");
        dirLightShaderProgram.createUniform("diffuseText");
        dirLightShaderProgram.createUniform("specularText");
        dirLightShaderProgram.createUniform("normalsText");
        dirLightShaderProgram.createUniform("shadowText");

        dirLightShaderProgram.createUniform("specularPower");
        dirLightShaderProgram.createUniform("ambientLight");
        dirLightShaderProgram.createDirectionalLightUniform("directionalLight");
    }

    private void setupPointLightShader() throws Exception {
        pointLightShaderProgram = new ShaderProgram();
        pointLightShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.LIGHT_VERTEX));
        pointLightShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.POINT_LIGHT_FRAGMENT));
        pointLightShaderProgram.link();

        pointLightShaderProgram.createUniform("modelMatrix");
        pointLightShaderProgram.createUniform("projectionMatrix");

        pointLightShaderProgram.createUniform("screenSize");
        pointLightShaderProgram.createUniform("positionsText");
        pointLightShaderProgram.createUniform("diffuseText");
        pointLightShaderProgram.createUniform("specularText");
        pointLightShaderProgram.createUniform("normalsText");
        pointLightShaderProgram.createUniform("shadowText");

        pointLightShaderProgram.createUniform("specularPower");
        pointLightShaderProgram.createPointLightUniform("pointLight");
    }

    private void setupFogShader() throws Exception {
        fogShaderProgram = new ShaderProgram();
        fogShaderProgram.createVertexShader(Utils.loadResource(Shaders.OpenGL.LIGHT_VERTEX));
        fogShaderProgram.createFragmentShader(Utils.loadResource(Shaders.OpenGL.FOG_FRAGMENT));
        fogShaderProgram.link();

        fogShaderProgram.createUniform("modelMatrix");
        fogShaderProgram.createUniform("viewMatrix");
        fogShaderProgram.createUniform("projectionMatrix");

        fogShaderProgram.createUniform("screenSize");
        fogShaderProgram.createUniform("positionsText");
        fogShaderProgram.createUniform("depthText");
        fogShaderProgram.createUniform("sceneText");

        fogShaderProgram.createFogUniform("fog");
        fogShaderProgram.createUniform("ambientLight");
        fogShaderProgram.createUniform("lightColor");
        fogShaderProgram.createUniform("lightIntensity");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }
    
    public void loadSkyBox(ModelData skybox) throws Exception {
    	List<ModelData> list = new ArrayList<ModelData>();
    	list.add(skybox);
    	skyBoxModel = GLModel.transformModels(list, textureCache).get(0);
    }
    
    public void loadParticles(List<ModelData> modelDataList, int maxParticles) throws Exception {
    	particleModels.addAll(InstancedGLModel.transformModels(modelDataList, textureCache, maxParticles));
    }
    
    public void loadModels(List<ModelData> modelDataList) throws Exception {
    	glModels.addAll(GLModel.transformModels(modelDataList, textureCache));
    	shadowRenderer.loadModels(glModels);
    }
    
    public void clearModels() {
    	glModels.clear();
    	shadowRenderer.clearModels();
    }
    
    public void loadInstanceModels(List<ModelData> modelDataList, int numInstances) throws Exception {
    	glInstancedModels.addAll((InstancedGLModel.transformModels(modelDataList, textureCache, numInstances)));
    	shadowRenderer.loadInstancedModels(glInstancedModels);
    }
    
    public void clearInstanceModels() {
    	glInstancedModels.clear();
    	shadowRenderer.clearInstancedModels();
    }
    
    private void renderGeometry(Window window, Camera camera, Scene scene) {
        // Render G-Buffer for writing
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gBuffer.getGBufferId());

        clear();

        glDisable(GL_BLEND);

        gBufferShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        gBufferShaderProgram.setUniform("viewMatrix", viewMatrix);
        gBufferShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        gBufferShaderProgram.setUniform("texture_sampler", 0);
        gBufferShaderProgram.setUniform("normalMap", 1);

        List<ShadowCascade> shadowCascades = shadowRenderer.getShadowCascades();
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);
            gBufferShaderProgram.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix(), i);
            gBufferShaderProgram.setUniform("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
            gBufferShaderProgram.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix(), i);
        }
        shadowRenderer.bindTextures(GL_TEXTURE2);
        int start = 2;
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            gBufferShaderProgram.setUniform("shadowMap_" + i, start + i);
        }
        gBufferShaderProgram.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);

        renderNonInstancedMeshes(scene);

        renderInstancedMeshes(scene, viewMatrix);

        gBufferShaderProgram.unbind();

        glEnable(GL_BLEND);
    }

    private void initLightRendering() {
        // Bind scene buffer
        glBindFramebuffer(GL_FRAMEBUFFER, sceneBuffer.getBufferId());

        // Clear G-Buffer
        clear();

        // Disable depth testing to allow the drawing of multiple layers with the same depth
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        // Bind GBuffer for reading
        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.getGBufferId());
    }

    private void endLightRendering() {
        // Bind screen for writing
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    private void renderPointLights(Window window, Camera camera, Scene scene) {
        pointLightShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        pointLightShaderProgram.setUniform("modelMatrix", bufferPassModelMatrix);
        pointLightShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        // Specular factor
        pointLightShaderProgram.setUniform("specularPower", specularPower);

        // Bind the G-Buffer textures
        int[] textureIds = this.gBuffer.getTextureIds();
        int numTextures = textureIds != null ? textureIds.length : 0;
        for (int i=0; i<numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        }

        pointLightShaderProgram.setUniform("positionsText", 0);
        pointLightShaderProgram.setUniform("diffuseText", 1);
        pointLightShaderProgram.setUniform("specularText", 2);
        pointLightShaderProgram.setUniform("normalsText", 3);
        pointLightShaderProgram.setUniform("shadowText", 4);

        pointLightShaderProgram.setUniform("screenSize", (float) gBuffer.getWidth(), (float)gBuffer.getHeight());

        SceneLight sceneLight = scene.getSceneLight();
        PointLight[] pointLights = sceneLight.getPointLightList();
        int numPointLights = pointLights != null ? pointLights.length : 0;
        for(int i=0; i<numPointLights; i++) {
            // Get a copy of the point light object and transform its position to view coordinates
            PointLight currPointLight = new PointLight(pointLights[i]);
            Vector3f lightPos = currPointLight.getPosition();
            tmpVec.set(lightPos, 1);
            tmpVec.mul(viewMatrix);
            lightPos.x = tmpVec.x;
            lightPos.y = tmpVec.y;
            lightPos.z = tmpVec.z;
            pointLightShaderProgram.setUniform("pointLight", currPointLight);

            bufferPassModel.render();
        }

        pointLightShaderProgram.unbind();
    }

    private void renderDirectionalLight(Window window, Camera camera, Scene scene) {
        dirLightShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        dirLightShaderProgram.setUniform("modelMatrix", bufferPassModelMatrix);
        dirLightShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        // Specular factor
        dirLightShaderProgram.setUniform("specularPower", specularPower);

        // Bind the G-Buffer textures
        int[] textureIds = this.gBuffer.getTextureIds();
        int numTextures = textureIds != null ? textureIds.length : 0;
        for (int i=0; i<numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        }

        dirLightShaderProgram.setUniform("positionsText", 0);
        dirLightShaderProgram.setUniform("diffuseText", 1);
        dirLightShaderProgram.setUniform("specularText", 2);
        dirLightShaderProgram.setUniform("normalsText", 3);
        dirLightShaderProgram.setUniform("shadowText", 4);

        dirLightShaderProgram.setUniform("screenSize", (float) gBuffer.getWidth(), (float)gBuffer.getHeight());

        // Ambient light
        SceneLight sceneLight = scene.getSceneLight();
        dirLightShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());

        // Directional light
        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        tmpVec.set(currDirLight.getDirection(), 0);
        tmpVec.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(tmpVec.x, tmpVec.y, tmpVec.z));
        dirLightShaderProgram.setUniform("directionalLight", currDirLight);

        bufferPassModel.render();

        dirLightShaderProgram.unbind();
    }

    private void renderFog(Window window, Camera camera, Scene scene) {
        fogShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        fogShaderProgram.setUniform("modelMatrix", bufferPassModelMatrix);
        fogShaderProgram.setUniform("viewMatrix", viewMatrix);
        fogShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        // Bind the scene buffer texture and the the depth texture of the G-Buffer
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, gBuffer.getPositionTexture());
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, gBuffer.getDepthTexture());
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, sceneBuffer.getTextureId());

        fogShaderProgram.setUniform("positionsText", 0);
        fogShaderProgram.setUniform("depthText", 1);
        fogShaderProgram.setUniform("sceneText", 2);

        fogShaderProgram.setUniform("screenSize", (float) window.getWidth(), (float)window.getHeight());

        fogShaderProgram.setUniform("fog", scene.getFog());
        SceneLight sceneLight = scene.getSceneLight();
        fogShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
        DirectionalLight dirLight = sceneLight.getDirectionalLight();
        fogShaderProgram.setUniform("lightColor", dirLight.getColor());
        fogShaderProgram.setUniform("lightIntensity", dirLight.getIntensity());

        bufferPassModel.render();

        fogShaderProgram.unbind();
    }
    
    private void renderParticles(Window window, Camera camera, Scene scene) {
        if (scene.getParticleEmitters() != null) {
        	
        	glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            
        	particlesShaderProgram.bind();

        	Matrix4f viewMatrix = camera.getViewMatrix();
            particlesShaderProgram.setUniform("viewMatrix", viewMatrix);
            particlesShaderProgram.setUniform("texture_sampler", 0);
            Matrix4f projectionMatrix = window.getProjectionMatrix();
            particlesShaderProgram.setUniform("projectionMatrix", projectionMatrix);
            
            IParticleEmitter[] emitters = scene.getParticleEmitters();
            int numEmitters = emitters != null ? emitters.length : 0;

            glDepthMask(false);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);

            for (int i = 0; i < numEmitters; i++) {
                IParticleEmitter emitter = emitters[i];
                for (InstancedGLModel model : particleModels) {
                	for (GLMaterial material : model.getGLMaterialList()) {
                		//InstancedMesh mesh = (InstancedMesh)emitter.getBaseParticle().getMesh();
                        
                        GLTexture text = material.texture();
                        particlesShaderProgram.setUniform("numCols", text.getNumCols());
                        particlesShaderProgram.setUniform("numRows", text.getNumRows());

                        model.renderListInstanced(emitter.getParticles(), true, transformation, viewMatrix, material);
                        //mesh.renderListInstanced(emitter.getParticles(), true, transformation, viewMatrix, null);
                	}
                }
            }

            glDisable(GL_BLEND);
            glDepthMask(true);

            particlesShaderProgram.unbind();
        }
    }
    
    private void renderSkyBox(Window window, Camera camera, Scene scene) {
    	SkyBox skyBox = scene.getSkyBox();
        if (skyBox != null && skyBoxModel != null) {
            skyBoxShaderProgram.bind();

            for (GLMaterial material : skyBoxModel.getGLMaterialList()) {
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
                
                Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
                skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLight().getSkyBoxLight());
                skyBoxShaderProgram.setUniform("color", material.ambientColor());
                skyBoxShaderProgram.setUniform("hasTexture", material.isTextured() ? 1 : 0);
                
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, gBuffer.getDepthTexture());
                skyBoxShaderProgram.setUniform("screenSize", (float)window.getWidth(), (float)window.getHeight());
                skyBoxShaderProgram.setUniform("depthsText", 1);

                skyBoxModel.render(material);

                viewMatrix.m30(m30);
                viewMatrix.m31(m31);
                viewMatrix.m32(m32);
            }
            
            skyBoxShaderProgram.unbind();
        }
    }
    
    private void renderSkyBox(Matrix4f projMat, Camera camera, Scene scene, int width, int height) {
    	SkyBox skyBox = scene.getSkyBox();
        if (skyBox != null && skyBoxModel != null) {
            skyBoxShaderProgram.bind();

            for (GLMaterial material : skyBoxModel.getGLMaterialList()) {
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
                
                Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
                skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLight().getSkyBoxLight());
                skyBoxShaderProgram.setUniform("color", material.ambientColor());
                skyBoxShaderProgram.setUniform("hasTexture", material.isTextured() ? 1 : 0);
                
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, gBuffer.getDepthTexture());
                skyBoxShaderProgram.setUniform("screenSize", (float)width, (float)height);
                skyBoxShaderProgram.setUniform("depthsText", 1);


                skyBoxModel.render(material);

                viewMatrix.m30(m30);
                viewMatrix.m31(m31);
                viewMatrix.m32(m32);
            }
            
            skyBoxShaderProgram.unbind();
        }
    }
    
    private void renderNonInstancedMeshes(Scene scene) {
        gBufferShaderProgram.setUniform("isInstanced", 0);

        // Render each mesh with the associated game Items
        //Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        Map<String, List<GameItem>> modelMap = scene.getModelMap();
        for (GLModel model : glModels) {
        	String modelId = model.getModelId();
        	List<GameItem> items = modelMap.get(modelId);
        	if (items.isEmpty()) {
        		continue;
        	}
            for (GLMaterial material : model.getGLMaterialList()) {
            	if (material.glMeshList().isEmpty()) {
            		continue;
            	}
            	gBufferShaderProgram.setUniform("material", material);

                GLTexture text = material.texture();
                if (text != null) {
                	gBufferShaderProgram.setUniform("numCols", text.getNumCols());
                	gBufferShaderProgram.setUniform("numRows", text.getNumRows());
                }
                
                shadowRenderer.bindTextures(GL_TEXTURE2);

                model.renderList(items, material, (GameItem gameItem) -> {
                	gBufferShaderProgram.setUniform("selectedNonInstanced", gameItem.isSelected() ? 1.0f : 0.0f);
                    Matrix4f modelMatrix = gameItem.buildModelMatrix();
                    gBufferShaderProgram.setUniform("modelNonInstancedMatrix", modelMatrix);
                    if (gameItem instanceof AnimGameItem) {
                        AnimGameItem animGameItem = (AnimGameItem) gameItem;
                        AnimatedFrame frame = animGameItem.getCurrentAnimation().getCurrentFrame();
                        gBufferShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
                    }
                });
            }
        }
    }

    private void renderInstancedMeshes(Scene scene, Matrix4f viewMatrix) {
    	gBufferShaderProgram.setUniform("isInstanced", 1);

        // Render each mesh with the associated game Items
        Map<String, List<GameItem>> modelMap = scene.getInstancedModelMap();
        for (InstancedGLModel model : glInstancedModels) {
        	String modelId = model.getModelId();
        	List<GameItem> items = modelMap.get(modelId);
        	if (items.isEmpty()) {
        		continue;
        	}
            for (GLMaterial material : model.getGLMaterialList()) {
            	if (material.glMeshList().isEmpty()) {
            		continue;
            	}
            	
            	GLTexture text = material.texture();
                if (text != null) {
                	gBufferShaderProgram.setUniform("numCols", text.getNumCols());
                	gBufferShaderProgram.setUniform("numRows", text.getNumRows());
                }
                
                gBufferShaderProgram.setUniform("material", material);

                filteredItems.clear();
                for(GameItem gameItem : items) {
                    if ( gameItem.isInsideFrustum() ) {
                        filteredItems.add(gameItem);
                    }
                }
                shadowRenderer.bindTextures(GL_TEXTURE2);
                
                model.renderListInstanced(filteredItems, transformation, viewMatrix, material);
            }
        }
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
        //frustumFilter.filter(scene.getPortalMeshes());
        //frustumFilter.filter(scene.getGameMeshes());
        //frustumFilter.filter(scene.getGameInstancedMeshes());
        
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
    				
    				//renderScene(pCam.clipOblique(window, p), pCam, scene);
    				renderSkyBox(pCam.clipOblique(window, p), pCam, scene, window.getWidth(), window.getHeight());
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
    			//renderScene(window, camera, scene);
    			renderSkyBox(window, camera, scene);
    		}
    	}
    }

    public void cleanup() {
    	if (shadowRenderer != null) {
            shadowRenderer.cleanup();
        }
        if (skyBoxShaderProgram != null) {
            skyBoxShaderProgram.cleanup();
        }
        if (particlesShaderProgram != null) {
            particlesShaderProgram.cleanup();
        }
        if (portalShaderProgram != null) {
        	portalShaderProgram.cleanup();
        }
        if (portalErrShaderProgram != null) {
        	portalErrShaderProgram.cleanup();
        }
        if (gBufferShaderProgram != null) {
        	gBufferShaderProgram.cleanup();
        }
        if (dirLightShaderProgram != null) {
        	dirLightShaderProgram.cleanup();
        }
        if (pointLightShaderProgram != null) {
        	pointLightShaderProgram.cleanup();
        }
        if (fogShaderProgram != null) {
        	fogShaderProgram.cleanup();
        }
    }
}
