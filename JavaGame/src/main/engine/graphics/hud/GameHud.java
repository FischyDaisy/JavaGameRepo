package main.engine.graphics.hud;

import java.awt.Font;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import main.engine.Window;
import main.engine.graphics.FontTexture;
import main.engine.graphics.IHud;
import main.engine.graphics.Material;
import main.engine.graphics.Transformation;
import main.engine.graphics.opengl.Mesh;
import main.engine.graphics.opengl.ShaderProgram;
import main.engine.graphics.opengl.GLTexture;
import main.engine.items.GameItem;
import main.engine.items.TextItem;
import main.engine.loaders.obj.OBJLoader;
import main.engine.utility.Utils;

import static main.engine.utility.ResourcePaths.Shaders;

/*
public class GameHud implements IHud {
	
	private static final Font FONT = new Font("Arial", Font.PLAIN, 20);

    private static final String CHARSET = "ISO-8859-1";

    private final GameItem[] gameItems;

    private final TextItem statusTextItem;
    
    private final GameItem crossHair;
    
    private final Transformation transformation;
    
    private ShaderProgram shader;
    
    public GameHud(String statusText) throws Exception {
    	transformation = new Transformation();
    	setupShader();
        FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.statusTextItem = new TextItem(statusText, fontTexture);
        this.statusTextItem.getMesh().getMaterial().setAmbientColor(new Vector4f(1.0f, 1.0f, 1.0f, 10f));
        
        Mesh cMesh = OBJLoader.loadMesh("/main/resources/models/quad.obj");
        GLTexture cText = new GLTexture(System.getProperty("user.dir") + "\\src\\main\\resources\\textures\\hairycross.png");
        Material cMat = new Material(cText, 1f);
        //cMat.setAmbientColor(new Vector4f(1, 0, 0, 1));
        cMesh.setMaterial(cMat);
        
        crossHair = new GameItem(cMesh);
        crossHair.setScale(20.0f);
        crossHair.setPosition(100f, 100f, 0f);

        // Create list that holds the items that compose the HUD
        gameItems = new GameItem[]{statusTextItem, crossHair};
    }
    
    private void setupShader() throws Exception {
        shader = new ShaderProgram();
        shader.createVertexShader(Utils.loadResource(Shaders.OpenGL.HUD_VERTEX));
        shader.createFragmentShader(Utils.loadResource(Shaders.OpenGL.HUD_FRAGMENT));
        shader.link();

        // Create uniforms for Ortographic-model projection matrix and base color
        shader.createUniform("projModelMatrix");
        shader.createUniform("color");
        shader.createUniform("hasTexture");
        shader.createUniform("inGame");
    }
    
    public void setStatusText(String statusText) {
        this.statusTextItem.setText(statusText);
    }
    
    public GameItem[] getGameItems() {
        return gameItems;
    }
   
    public void updateSize(Window window) {
        this.statusTextItem.setPosition(10f, window.getHeight() - 50f, 0);
    }
    
    public void updateCrossHair(Window window) {
    	this.crossHair.setPosition((window.getWidth() / 2.0f), (window.getHeight() / 2.0f), 0);
    }

	@Override
	public void input(Window window) {
	}

	@Override
	public void render(Window window) {
		shader.bind();

        Matrix4f ortho = transformation.getOrtho2DProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
        for (GameItem gameItem : getGameItems()) {
            Mesh mesh = gameItem.getMesh();
            // Set orthographic and model matrix for this HUD item
            Matrix4f projModelMatrix = transformation.buildOrthoProjModelMatrix(gameItem, ortho);
            shader.setUniform("projModelMatrix", projModelMatrix);
            shader.setUniform("color", gameItem.getMesh().getMaterial().getAmbientColor());
            shader.setUniform("hasTexture", gameItem.getMesh().getMaterial().isTextured() ? 1 : 0);
            shader.setUniform("inGame", 1);

            // Render the mesh for this HUD item
            mesh.render();
        }

        shader.unbind();
	}

	@Override
	public void cleanup() {
		for (GameItem gameItem : gameItems) {
			for (Mesh mesh : gameItem.getMeshes()) {
				mesh.cleanup();
			}
		}
	}

}
*/