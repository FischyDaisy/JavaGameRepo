package main.engine.graphics.opengl.geometry;

import main.engine.Scene;
import main.engine.Window;
import main.engine.graphics.animation.AnimGameItem;
import main.engine.graphics.animation.AnimatedFrame;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.opengl.GLModel;
import main.engine.graphics.opengl.GLRenderer;
import main.engine.graphics.opengl.GLTexture;
import main.engine.graphics.opengl.InstancedGLModel;
import main.engine.graphics.opengl.ShaderProgram;
import main.engine.graphics.opengl.GLModel.GLMaterial;
import main.engine.graphics.opengl.shadows.ShadowCascade;
import main.engine.graphics.opengl.shadows.ShadowRenderer;
import main.engine.items.GameItem;
import main.engine.utility.Utils;

import static main.engine.utility.ResourcePaths.Shaders;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

public class GeometryRenderActivity {
	
	private final GBuffer gBuffer;
	private final Window window;
	private final Camera camera;
	private final Scene scene;
	
	private ShaderProgram geometryShader;
	
	public GeometryRenderActivity(Window window, Camera camera, Scene scene) throws Exception {
		gBuffer = new GBuffer(window);
		this.window = window;
		this.camera = camera;
		this.scene = scene;
		createGeometryShader();
		
	}
	
	private void createGeometryShader() throws Exception {
		geometryShader = new ShaderProgram();
		geometryShader.createVertexShader(Utils.loadResource(Shaders.Vulkan.GEOMETRY_VERTEX_GLSL));
		geometryShader.createFragmentShader(Utils.loadResource(Shaders.Vulkan.GEOMETRY_FRAGMENT_GLSL));
		geometryShader.link();
		
		geometryShader.createUniform("projectionMatrix");
		geometryShader.createUniform("viewMatrix");
		geometryShader.createUniform("modelMatrix");
		geometryShader.createUniform("textSampler");
		geometryShader.createUniform("normalSampler");
		geometryShader.createUniform("metRoughSampler");
		geometryShader.createMaterialUniform("MaterialUniform");
	}
	
	public void render(GLRenderer renderer, List<GLModel> glModels, List<InstancedGLModel> glInstancedModels) {
		// Render G-Buffer for writing
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gBuffer.getGBufferId());

        renderer.clear();

        glDisable(GL_BLEND);

        geometryShader.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        geometryShader.setUniform("viewMatrix", viewMatrix);
        geometryShader.setUniform("projectionMatrix", projectionMatrix);

        geometryShader.setUniform("textSampler", 0);
        geometryShader.setUniform("normalSampler", 1);
        geometryShader.setUniform("metRoughSampler", 2);

        renderNonInstancedMeshes(scene, glModels);

        renderInstancedMeshes(scene, viewMatrix, glInstancedModels);

        geometryShader.unbind();

        glEnable(GL_BLEND);
	}
	
	private void renderNonInstancedMeshes(Scene scene, List<GLModel> glModels) {
		geometryShader.setUniform("isInstanced", 0);

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
            	geometryShader.setUniform("material", material);

                GLTexture text = material.texture();
                if (text != null) {
                	geometryShader.setUniform("numCols", text.getNumCols());
                	geometryShader.setUniform("numRows", text.getNumRows());
                }
                
                shadowRenderer.bindTextures(GL_TEXTURE2);

                model.renderList(items, material, (GameItem gameItem) -> {
                	geometryShader.setUniform("selectedNonInstanced", gameItem.isSelected() ? 1.0f : 0.0f);
                    Matrix4f modelMatrix = gameItem.buildModelMatrix();
                    geometryShader.setUniform("modelNonInstancedMatrix", modelMatrix);
                    if (gameItem instanceof AnimGameItem) {
                        AnimGameItem animGameItem = (AnimGameItem) gameItem;
                        AnimatedFrame frame = animGameItem.getCurrentAnimation().getCurrentFrame();
                        geometryShader.setUniform("jointsMatrix", frame.getJointMatrices());
                    }
                });
            }
        }
    }

    private void renderInstancedMeshes(Scene scene, Matrix4f viewMatrix, List<InstancedGLModel> glInstancedModels) {
    	geometryShader.setUniform("isInstanced", 1);

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
                	geometryShader.setUniform("numCols", text.getNumCols());
                	geometryShader.setUniform("numRows", text.getNumRows());
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
}
