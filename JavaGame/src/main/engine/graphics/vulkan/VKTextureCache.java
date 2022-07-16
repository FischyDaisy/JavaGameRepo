package main.engine.graphics.vulkan;

import main.engine.EngineProperties;
import main.engine.utility.Cache;
import main.engine.utility.ResourcePaths;

public class VKTextureCache extends Cache<VKTexture> {
	
	private static VKTextureCache instance;
	
	private VKTextureCache() {
		super();
	}
	
	public static synchronized VKTextureCache getInstance() {
		if (instance == null) {
    		instance = new VKTextureCache();
    	}
    	return instance;
	}

	@Override
	public VKTexture get(String key) {
        return cacheMap.get(key);
	}
	
	public VKTexture get(Device device, String key, int format) {
		VKTexture texture = get(key);
		if (texture == null && key.length() > 0) {
			texture = new VKTexture(device, key, format);
			cacheMap.put(key, texture);
		} else if (texture == null && key.length() == 0) {
			String path = ResourcePaths.Textures.DEFAULT_TEXTURE;
			texture = new VKTexture(device, path, format);
			cacheMap.put(path, texture);
		}
		return texture;
	}
	
	@Override
	public void cleanup() {
		cacheMap.forEach((k, v) -> v.cleanup());
		super.cleanup();
	}

}
