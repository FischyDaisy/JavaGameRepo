package main.engine.graphics;


import java.util.*;

import main.engine.EngineProperties;
import main.engine.graphics.opengl.GLTexture;
import main.engine.graphics.vulkan.Device;
import main.engine.graphics.vulkan.VKTexture;

public class TextureCache {

    private final IndexedLinkedHashMap<String, ITexture> textureMap;

    public TextureCache() {
        textureMap = new IndexedLinkedHashMap<String, ITexture>();
    }

    public void cleanup() {
        textureMap.forEach((k, v) -> v.cleanup());
        textureMap.clear();
    }

    public VKTexture getTexture(Device device, String texturePath, int format) {
        String path = texturePath;
        if (texturePath == null || texturePath.trim().isEmpty()) {
            EngineProperties engProperties = EngineProperties.getInstance();
            path = engProperties.getDefaultTexturePath();
        }
        ITexture texture = textureMap.get(path);
        if (texture == null) {
            texture = new VKTexture(device, path, format);
            textureMap.put(path, texture);
        }
        return (VKTexture) texture;
    }
    
    public GLTexture getTexture(String texturePath, int cols, int rows) throws Exception {
    	String path = texturePath;
        if (texturePath == null || texturePath.trim().isEmpty()) {
            EngineProperties engProperties = EngineProperties.getInstance();
            //path = System.getProperty("user.dir") + "\\" + engProperties.getDefaultTexturePath().replace('/', '\\');
            return null;
        }
        ITexture texture = textureMap.get(path);
        if (texture == null) {
            texture = new GLTexture(path, cols, rows);
            textureMap.put(path, texture);
        }
        return (GLTexture) texture;
    }

    public List<VKTexture> getAsVKList() {
        List<VKTexture> list = new ArrayList<VKTexture>();
        for(ITexture text : textureMap.values()) {
        	list.add((VKTexture) text);
        }
        return list;
    }
    
    public List<GLTexture> getAsGLList() {
        List<GLTexture> list = new ArrayList<GLTexture>();
        for(ITexture text : textureMap.values()) {
        	list.add((GLTexture) text);
        }
        return list;
    }

    public int getPosition(String texturePath) {
        int result = -1;
        if (texturePath != null) {
            result = textureMap.getIndexOf(texturePath);
        }
        return result;
    }
}