package main.engine.utility.physUtils;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.bulletphysics.collision.shapes.UniformScalingShape;
import com.bulletphysics.linearmath.MatrixUtil;

import main.engine.graphics.Transformation;

/**
 * Transform represents translation and rotation (rigid transform). Scaling and
 * shearing is not supported.<p>
 * 
 * You can use local shape scaling or {@link UniformScalingShape} for static rescaling
 * of collision objects.
 * 
 * @author jezek2
 */
public class Transform {
	
	//protected BulletStack stack;

	/** Rotation matrix of this Transform. */
	public final Quaternionf rotation;
	
	/** Translation vector of this Transform. */
	public final Vector3f position;
	
	public final Transformation transform;

	public Transform() {
		position = new Vector3f();
		rotation = new Quaternionf();
		transform = new Transformation();
	}
	
	public Transform(Vector3f position, Quaternionf rotation) {
		this();
		this.position.set(position);
		this.rotation.set(rotation);
	}

	public Transform(Matrix3f mat) {
		this();
		rotation.setFromUnnormalized(mat);
	}

	public Transform(Matrix4f mat) {
		this();
		set(mat);
	}

	public Transform(Transform tr) {
		this();
		set(tr);
	}
	
	public void set(Transform tr) {
		this.position.set(tr.position);
		this.rotation.set(tr.rotation);
	}
	
	public void set(Matrix3f mat) {
		rotation.setFromUnnormalized(mat);
		position.set(0.0f, 0.0f, 0.0f);
	}

	public void set(Matrix4f mat) {
		rotation.setFromUnnormalized(mat);
		mat.transformPosition(position);
	}
	
	public Vector3f transform(Vector3f v) {
		return v.rotate(rotation).sub(position);
	}

	public void setIdentity() {
		rotation.identity();
		position.set(0.0f, 0.0f, 0.0f);
	}
	
	public void inverse() {
		rotation.invert();
		new Vector3f(0.0f, 0.0f, 0.0f).sub(position, position);
	}

	public void inverse(Transform tr) {
		set(tr);
		inverse();
	}
	
	public void mul(Transform tr) {
		Vector3f vec = new Vector3f(tr.position);
		transform(vec);

		rotation.mul(tr.rotation);
		position.set(vec);
	}

	public void mul(Transform tr1, Transform tr2) {
		Vector3f vec = new Vector3f(tr2.position);
		tr1.transform(vec);

		rotation.set(tr1.rotation.mul(tr2.rotation));
		position.set(vec);
	}
	
	public void invXform(Vector3f inVec, Vector3f out) {
		inVec.sub(position, out);

		out.rotate(rotation.invert());
	}
	
	public Quaternionf getRotation() {
		return rotation;
	}
	
	public void setRotation(Quaternionf q) {
		rotation.set(q);
	}
	
	public void setFromOpenGLMatrix(float[] m) {
		//MatrixUtil.setFromOpenGLSubMatrix(basis, m);
		//origin.set(m[12], m[13], m[14]);
	}

	public void getOpenGLMatrix(float[] m) {
		//MatrixUtil.getOpenGLSubMatrix(basis, m);
		//m[12] = origin.x;
		//m[13] = origin.y;
		//m[14] = origin.z;
		//m[15] = 1f;
	}

	public Matrix4f getMatrix(Matrix4f out) {
		
		out.set(transform.buildModelMatrix(position, rotation, new Vector3f(1.0f, 1.0f, 1.0f)));
		return out;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Transform)) return false;
		Transform tr = (Transform)obj;
		return rotation.equals(tr.rotation) && position.equals(tr.position);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 41 * hash + rotation.hashCode();
		hash = 41 * hash + position.hashCode();
		return hash;
	}
	
}