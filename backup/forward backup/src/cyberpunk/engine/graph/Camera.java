package hengine.engine.graph;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

	private final Vector3f position;

	private final Vector3f rotation;

	private final Matrix4f viewMatrix;

	public Camera() {
		position = new Vector3f();
		rotation = new Vector3f();
		viewMatrix = new Matrix4f();
	}

	public Camera(final Vector3f position, final Vector3f rotation) {
		this.position = position;
		this.rotation = rotation;
		viewMatrix = new Matrix4f();
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public Matrix4f updateViewMatrix() {
		return Transformation.updateGenericViewMatrix(position, rotation, viewMatrix);
	}

	/*
	 **********************
	 ****** POSITION ******
	 **********************
	 */
	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(final float x, final float y, final float z) {
		position.set(x, y, z);
	}

	public void translate(final float offsetX, final float offsetY, final float offsetZ) {
		if (offsetZ != 0) {
			position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
			position.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
		}
		if (offsetX != 0) {
			position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
			position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
		}
		position.y += offsetY;
	}

	/*
	 **********************
	 ****** ROTATION ******
	 **********************
	 */
	public Vector3f getRotation() {
		return rotation;
	}

	public void setRotation(final float x, final float y, final float z) {
		rotation.set(x, y, z);
	}

	public void rotate(final float offsetX, final float offsetY, final float offsetZ) {
		rotation.add(offsetX, offsetY, offsetZ);
	}
}