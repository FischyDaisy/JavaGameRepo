package main.engine.graphics.opengl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;

import main.engine.graphics.ITexture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class GLTexture implements ITexture {
	
	private final int id;

    private final int width;

    private final int height;
    
    private int numRows = 1;

    private int numCols = 1;
    
    private boolean hasTransparencies;
    
    /**
     * Creates an empty texture.
     *
     * @param width Width of the texture
     * @param height Height of the texture
     * @param pixelFormat Specifies the format of the pixel data (GL_RGBA, etc.)
     * @throws Exception
     */
    public GLTexture(int width, int height, int pixelFormat) throws Exception {
        this.id = glGenTextures();
        this.width = width;
        this.height = height;
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexImage2D(GL_TEXTURE_2D, 0, pixelFormat, this.width, this.height, 0, pixelFormat, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }
	
    public GLTexture(String fileName) throws Exception {
    	ByteBuffer buf;
        // Load Texture file
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            buf = stbi_load(fileName, w, h, channels, 4);
            if (buf == null) {
                throw new Exception("Image file [" + fileName  + "] not loaded: " + stbi_failure_reason());
            }
            setHasTransparencies(buf);

            width = w.get();
            height = h.get();
        }

        this.id = createTexture(buf);

        stbi_image_free(buf);
    }
    
    public GLTexture(String fileName, int numCols, int numRows) throws Exception  {
        this(fileName);
        this.numCols = numCols;
        this.numRows = numRows;
    }
    
    public GLTexture(ByteBuffer imageBuffer) throws Exception {
        ByteBuffer buf;
        // Load Texture file
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            buf = stbi_load_from_memory(imageBuffer, w, h, channels, 4);
            if (buf == null) {
                throw new Exception("Image file not loaded: " + stbi_failure_reason());
            }
            setHasTransparencies(buf);

            width = w.get();
            height = h.get();
        }

        this.id = createTexture(buf);

        stbi_image_free(buf);
    }
    
    private int createTexture(ByteBuffer buf) {
        // Create a new OpenGL texture
        int textureId = glGenTextures();
        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
            GL_RGBA, GL_UNSIGNED_BYTE, buf);
        // Generate Mip Map
        glGenerateMipmap(GL_TEXTURE_2D);

        return textureId;
    }
    
    public boolean hasTransparencies() {
        return hasTransparencies;
    }
    
    private void setHasTransparencies(ByteBuffer buf) {
        int numPixels = buf.capacity() / 4;
        int offset = 0;
        hasTransparencies = false;
        for (int i = 0; i < numPixels; i++) {
            int a = (0xFF & buf.get(offset + 3));
            if (a < 255) {
                hasTransparencies = true;
                break;
            }
            offset += 4;
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public int getId() {
        return id;
    }
	
	public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
    
    public int getNumCols() {
        return numCols;
    }

    public int getNumRows() {
        return numRows;
    }
	
    @Override
	public void cleanup() {
        glDeleteTextures(id);
    }
}
