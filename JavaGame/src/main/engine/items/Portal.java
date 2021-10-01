package main.engine.items;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import main.engine.graphics.Material;
import main.engine.graphics.Transformation;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.opengl.FrameBuffer;
import main.engine.graphics.opengl.Mesh;

public class Portal extends GameItem {
	
	public static final int MAX_RECURSION = 25;
	
	private final FrameBuffer frameBuffer;
	
	private Warp dest;
	
	public Portal(Mesh mesh) throws Exception {
		super(mesh);
		frameBuffer = new FrameBuffer();
		Material pMat = new Material(frameBuffer.getTexture());
		this.getMesh().setMaterial(pMat);
		dest = null;
	}
	
	public Portal(Mesh[] meshes) throws Exception {
		super(meshes);
		frameBuffer = new FrameBuffer();
		dest = null;
	}
	
	public static Camera createCamera(Vector3f position, Vector3f rotation) {
		return new Camera(position, rotation);
	}
	
	public Warp getWarp() {
		return dest;
	}
	
	public void setWarp(Warp w) {
		dest = w;
	}
	
	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}
	
	public static void connect(Portal a, Portal b, Transformation transformation) {
		a.setWarp(new Warp(a, b));
		b.setWarp(new Warp(b, a));
	}
	
	public record Warp(Portal fromPortal, Portal toPortal) {}
}