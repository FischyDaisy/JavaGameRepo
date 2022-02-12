package main.engine.graphics.opengl;

import static org.lwjgl.opengl.GL20.*;

import java.util.HashMap;
import java.util.Map;

public abstract class GLActivity {
	private final int programId;
	private final Map<String, Integer> uniforms;
	private int computeShaderId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private int geometryShaderId;
    
    public GLActivity() throws Exception {
    	programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
        uniforms = new HashMap<>();
    }
}
