package main.engine.graphics;

/**
 * This interface is just to make it so that both OpenGL and Vulkan textures
 * can exist together in the TextureCache
 * @author Christopher
 *
 */
public interface ITexture {
	public void cleanup();
}
