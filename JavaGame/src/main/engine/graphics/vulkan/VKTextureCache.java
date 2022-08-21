package main.engine.graphics.vulkan;

import main.engine.utility.Cache;

public class VKTextureCache extends Cache<VKTexture> {
	
	public static final VKTextureCache INSTANCE = new VKTextureCache();
	
	private VKTextureCache() {
		super();
	}

	@Override
	public VKTexture get(String key) {
        return cacheMap.get(key);
	}
	
	public VKTexture get(Device device, String key, int format) {
		if (key == null || key.trim().isEmpty()) {
            return null;
        }
		VKTexture texture = get(key);
		if (texture == null) {
			texture = new VKTexture(device, key, format);
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
