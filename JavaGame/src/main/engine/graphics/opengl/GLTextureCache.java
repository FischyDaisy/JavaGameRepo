package main.engine.graphics.opengl;

import main.engine.EngineProperties;
import main.engine.utility.Cache;

public class GLTextureCache extends Cache<GLTexture> {
	
	private static GLTextureCache instance;
	
	private GLTextureCache() {
		super();
	}
	
	public static synchronized GLTextureCache getInstance() {
		if (instance == null) {
    		instance = new GLTextureCache();
    	}
    	return instance;
	}

	@Override
	public GLTexture get(String key) {
		String path = key;
        if (key == null || key.trim().isEmpty()) {
            EngineProperties engProperties = EngineProperties.getInstance();
            //path = System.getProperty("user.dir") + "\\" + engProperties.getDefaultTexturePath().replace('/', '\\');
            return null;
        }
		return cacheMap.get(path);
	}
	
	public GLTexture get(String key, int cols, int rows) throws Exception {
		GLTexture texture = get(key);
		if (texture == null && key.length() > 0) {
			texture = new GLTexture(key, cols, rows);
            cacheMap.put(key, texture);
		}
		return texture;
	}
	
	@Override
	public void cleanup() {
		cacheMap.forEach((k, v) -> v.cleanup());
		super.cleanup();
	}
}
