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

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import main.engine.Scene;
import main.engine.SceneLight;
import main.engine.Window;
import main.engine.graphics.FrustumCullingFilter;
import main.engine.graphics.IHud;
import main.engine.graphics.IRenderer;
import main.engine.graphics.Transformation;
import main.engine.graphics.animation.AnimGameItem;
import main.engine.graphics.animation.AnimatedFrame;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.hud.MenuHud;
import main.engine.graphics.lights.DirectionalLight;
import main.engine.graphics.lights.PointLight;
import main.engine.graphics.lights.SpotLight;
import main.engine.graphics.particles.IParticleEmitter;
import main.engine.items.GameItem;
import main.engine.items.Portal;
import main.engine.items.SkyBox;
import main.engine.utility.Utils;

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
    
    private final Transformation transformation;
    
    private final float specularPower;
    
    private final FrustumCullingFilter frustumFilter;

    private final List<GameItem> filteredItems;

    public GLRenderer() {
    	transformation = new Transformation();
    	specularPower = 10f;
    	frustumFilter = new FrustumCullingFilter();
        filteredItems = new ArrayList<GameItem>();
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
        frustumFilter.filter(scene.getGameMeshes());
        frustumFilter.filter(scene.getGameInstancedMeshes());
        
        // Render depth map before view ports has been set up
        renderDepthMap(window, camera, scene);

        glViewport(0, 0, window.getWidth(), window.getHeight());
        
        // Update projection matrix once per render cycle
        window.updateProjectionMatrix();

        renderScene(window, camera, scene);
        renderSkyBox(window, camera, scene);
        renderParticles(window, camera, scene);
        
        renderPortalsPink(window, camera, scene);
        renderPortals(window, camera, scene, null);
        
        //renderAxes(camera);
        //renderCrossHair(window);
    }
    
    private void setupPortalErrShader() throws Exception {
    	portalErrShaderProgram = new ShaderProgram();
    	portalErrShaderProgram.createVertexShader(Utils.loadResource("/main/resources/shaders/pink_vertex.vs"));
    	portalErrShaderProgram.createFragmentShader(Utils.loadResource("/main/resources/shaders/pink_fragment.fs"));
    	portalErrShaderProgram.link();
    	
    	portalErrShaderProgram.createUniform("modelViewMatrix");
    	portalErrShaderProgram.createUniform("projectionMatrix");
    	
    	//portalErrShaderProgram.createUniform("texture_sampler");
    }
    
    private void setupPortalShader() throws Exception {
    	portalShaderProgram = new ShaderProgram();
    	portalShaderProgram.createVertexShader(Utils.loadResource("/main/resources/shaders/portal_vertex.vs"));
    	portalShaderProgram.createFragmentShader(Utils.loadResource("/main/resources/shaders/portal_fragment.fs"));
    	portalShaderProgram.link();
    	
    	portalShaderProgram.createUniform("modelViewMatrix");
    	portalShaderProgram.createUniform("projectionMatrix");
    	
    	portalShaderProgram.createUniform("texture_sampler");
    }
    
    private void setupParticlesShader() throws Exception {
        particlesShaderProgram = new ShaderProgram();
        particlesShaderProgram.createVertexShader(Utils.loadResource("/main/resources/shaders/particles_vertex.vs"));
        particlesShaderProgram.createFragmentShader(Utils.loadResource("/main/resources/shaders/particles_fragment.fs"));
        particlesShaderProgram.link();

        particlesShaderProgram.createUniform("projectionMatrix");
        particlesShaderProgram.createUniform("texture_sampler");
        
        particlesShaderProgram.createUniform("numCols");
        particlesShaderProgram.createUniform("numRows");
    }
    
    private void setupDepthShader() throws Exception {
        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource("/main/resources/shaders/depth_vertex.vs"));
        depthShaderProgram.createFragmentShader(Utils.loadResource("/main/resources/shaders/depth_fragment.fs"));
        depthShaderProgram.link();

        depthShaderProgram.createUniform("isInstanced");
        depthShaderProgram.createUniform("jointsMatrix");
        depthShaderProgram.createUniform("modelLightViewNonInstancedMatrix");
        depthShaderProgram.createUniform("orthoProjectionMatrix");
    }
    
    private void setupSkyBoxShader() throws Exception {
        skyBoxShaderProgram = new ShaderProgram();
        skyBoxShaderProgram.createVertexShader(Utils.loadResource("/main/resources/shaders/sb_vertex.vs"));
        skyBoxShaderProgram.createFragmentShader(Utils.loadResource("/main/resources/shaders/sb_fragment.fs"));
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
        sceneShaderProgram.createVertexShader(Utils.loadResource("/main/resources/shaders/scene_vertex.vs"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("/main/resources/shaders/scene_fragment.fs"));
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
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
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
                
                Texture text = mesh.getMaterial().getTexture();
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
    
    private void renderNonInstancedMeshes(Scene scene, ShaderProgram shader, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        sceneShaderProgram.setUniform("isInstanced", 0);

        // Render each mesh with the associated game Items
        Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            if (viewMatrix != null) {
                shader.setUniform("material", mesh.getMaterial());
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getId());
            }

            Texture text = mesh.getMaterial().getTexture();
            if (text != null) {
                sceneShaderProgram.setUniform("numCols", text.getNumCols());
                sceneShaderProgram.setUniform("numRows", text.getNumRows());
            }

            mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
            	sceneShaderProgram.setUniform("selectedNonInstanced", gameItem.isSelected() ? 1.0f : 0.0f);
                Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
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
            }
            );
        }
    }

    private void renderInstancedMeshes(Scene scene, ShaderProgram shader, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        shader.setUniform("isInstanced", 1);

        // Render each mesh with the associated game Items
        Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
        for (InstancedMesh mesh : mapMeshes.keySet()) {
            Texture text = mesh.getMaterial().getTexture();
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
        		Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
        		Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
        		portalErrShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        	});
        }
        portalErrShaderProgram.unbind();
    }
    
    private void renderPortals(Window window, Camera camera, Scene scene, Portal skipPortal) {
    	//portalShaderProgram.bind();
    	//renderPortalList(window, camera, scene, skipPortal, 0);
    	//portalShaderProgram.unbind();
    	
    	
    }
    
    private void renderPortalList(Window window, Camera camera, Scene scene, Portal skipPortal, int curFBO) {
    	FrameBuffer frameBuf = skipPortal != null ? skipPortal.getFrameBuffer() : null;
    	if (skipPortal != null) {
        	glBindFramebuffer(GL_FRAMEBUFFER, frameBuf.getFrameBufferId());
        	glViewport(0, 0, FrameBuffer.FBO_SIZE, FrameBuffer.FBO_SIZE);
        	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    	} else {
    		glBindFramebuffer(GL_FRAMEBUFFER, curFBO);
    	}
    	portalShaderProgram.bind();
    	List<GameItem> filteredPortals = new ArrayList<GameItem>();
    	float aspectRatio = (float) window.getWidth() / (float) window.getHeight();
    	Matrix4f projectionMatrix = skipPortal != null ? new Matrix4f().setPerspective(Window.FOV, aspectRatio, Window.Z_NEAR, Window.Z_FAR) : window.getProjectionMatrix();
    	portalShaderProgram.setUniform("projectionMatrix", projectionMatrix);
    	portalShaderProgram.setUniform("texture_sampler", 0);
    	Matrix4f viewMatrix = camera.getViewMatrix();
    	Matrix4f lightViewMatrix = transformation.getLightViewMatrix();
    	
    	frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
        frustumFilter.filter(scene.getPortalMeshes());
        frustumFilter.filter(scene.getGameMeshes());
        frustumFilter.filter(scene.getGameInstancedMeshes());
        
        if (!scene.containsPortals()) {
        	if (skipPortal == null) {
        		portalShaderProgram.unbind();
        		glBindFramebuffer(GL_FRAMEBUFFER, curFBO);
        		return;
        	} else {
        		//portalShaderProgram.bind();
        		renderNonInstancedMeshes(scene, sceneShaderProgram, viewMatrix, lightViewMatrix);
                renderInstancedMeshes(scene, sceneShaderProgram, viewMatrix, lightViewMatrix);
                portalShaderProgram.unbind();
                glBindFramebuffer(GL_FRAMEBUFFER, curFBO);
        		return;
        	}
        } else {
        	Map<Mesh, List<GameItem>> portalMap = scene.getPortalMeshes();
        	for (Mesh mesh : portalMap.keySet()) {
        		filteredPortals.clear();
                for(GameItem portal : portalMap.get(mesh)) {
                    if ( portal.isInsideFrustum() ) {
                    	filteredPortals.add(portal);
                    }
                }
        		mesh.renderList(filteredPortals, (GameItem portal) -> {
        			/**/
        			Vector3f normal = transformation.forward(portal);
        	    	Vector3f camPos = new Vector3f(camera.getPosition());
        	    	boolean frontDirection = camPos.sub(((Portal) portal).getPosition()).dot(normal) > 0f;
        	    	Portal.Warp warp = frontDirection ? ((Portal) portal).getFront() : ((Portal) portal).getBack();
        	    	if (frontDirection) {
        	    		normal = normal.negate();
        	    	}
        	    	
        	    	Matrix4f modelMatrix = transformation.buildModelMatrix(portal);
        	    	Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
        	    	portalShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        	    	
        	    	Vector3f pRotation = new Vector3f();
        	    	pRotation = portal.getRotation().getEulerAnglesXYZ(pRotation);
        	    	pRotation.x = (float) Math.toDegrees(pRotation.x);
        	    	pRotation.y = (float) Math.toDegrees(pRotation.y);
        	    	pRotation.z = (float) Math.toDegrees(pRotation.z);
        	    	
        			Camera pCam = Portal.createCamera(portal.getPosition(), pRotation);
        			pCam.setViewMatrix(Portal.updateCameraViewMatrix(camera, warp));
        			
        			renderPortalList(window, pCam, scene, warp.getToPortal(), ((Portal) portal).getFrameBuffer().getFrameBufferId());
        			
        			glActiveTexture(GL_TEXTURE0);
        			glBindTexture(GL_TEXTURE_2D, ((Portal) portal).getFrameBuffer().getTexture().getId());
        			
        		});
        	}
        	portalShaderProgram.unbind();
        	glBindFramebuffer(GL_FRAMEBUFFER, curFBO);
        	return;
        	
        }
    }
    
    public Texture getShadowMapTexture() {
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
