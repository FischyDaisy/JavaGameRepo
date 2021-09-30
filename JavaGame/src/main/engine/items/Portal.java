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
	
	private Warp front, back;
	
	public Portal(Mesh mesh) throws Exception {
		super(mesh);
		frameBuffer = new FrameBuffer();
		Material pMat = new Material(frameBuffer.getTexture());
		this.getMesh().setMaterial(pMat);
		front = new Warp(this);
		back = new Warp(this);
	}
	
	public Portal(Mesh[] meshes) throws Exception {
		super(meshes);
		frameBuffer = new FrameBuffer();
		front = new Warp(this);
		back = new Warp(this);
	}
	
	public static Camera createCamera(Vector3f position, Vector3f rotation) {
		return new Camera(position, rotation);
	}
	
	public static Matrix4f updateCameraViewMatrix(Camera pCam, Warp warp) {
		return pCam.updateViewMatrixEuler().mul(warp.getDelta());
	}
	
	public Warp getFront() {
		return front;
	}
	
	public Warp getBack() {
		return back;
	}
	
	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}
	
	public static void connect(Portal a, Portal b, Transformation transformation) {
		connect(a.getFront(), b.getBack(), transformation);
		connect(b.getFront(), a.getBack(), transformation);
	}
	
	protected static void connect(Warp a, Warp b, Transformation transformation) {
		a.setToPortal(b.getFromPortal());
		b.setToPortal(a.getFromPortal());
		
		a.setDelta(transformation.buildModelMatrix(a.getFromPortal()).mul(transformation.buildLocalModelMatrix(b.getFromPortal())));
		b.setDelta(transformation.buildModelMatrix(b.getFromPortal()).mul(transformation.buildLocalModelMatrix(a.getFromPortal())));
		
		a.setDeltaInv(b.getDelta());
		b.setDeltaInv(a.getDelta());
	}
	
	public class Warp {
		private Matrix4f delta, deltaInv;
		private Portal fromPortal, toPortal;
		
		public Warp(Portal fromPortal) {
			delta = new Matrix4f();
			deltaInv = new Matrix4f();
			this.fromPortal = fromPortal;
			toPortal = null;
		}
		
		public Portal getFromPortal() {
			return fromPortal;
		}
		
		public void setFromPortal(Portal p) {
			fromPortal = p;
		}
		
		public Portal getToPortal() {
			return toPortal;
		}
		
		public void setToPortal(Portal p) {
			toPortal = p;
		}
		
		public Matrix4f getDelta() {
			return delta;
		}
		
		public Matrix4f getInvDelta() {
			return deltaInv;
		}
		
		public void setDelta(Matrix4f m) {
			delta = m;
			//deltaInv = delta.invert();
		}
		
		public void setDeltaInv(Matrix4f m) {
			deltaInv = m;
		}
	}
}