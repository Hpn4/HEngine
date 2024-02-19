package hengine.engine.graph;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import hengine.engine.MouseInput;
import hengine.engine.hlib.component.HWindow;

public class Camera {

	private final Vector3f position;

	private final Vector3f rotation;

	private final Matrix4f viewMatrix;

	private final Matrix4f invViewMatrix;

	private float speed;

	private float sensivity;

	private final Vector3f cameraInc;

	public Camera() {
		position = new Vector3f();
		rotation = new Vector3f();

		viewMatrix = new Matrix4f();
		invViewMatrix = new Matrix4f();

		speed = 0.1f;
		sensivity = 0.15f;

		cameraInc = new Vector3f();
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	
	public Matrix4f getInvViewMatrix() {
		return invViewMatrix;
	}

	public Matrix4f updateViewMatrix() {
		Transformation.updateGenericViewMatrix(position, rotation, viewMatrix);
		invViewMatrix.set(viewMatrix).invert();

		return viewMatrix;
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

	public boolean input(final HWindow window) {
		boolean sceneChanged = false;
		cameraInc.set(0, 0, 0);
		if (window.isKeyPressed(GLFW_KEY_W)) {
			sceneChanged = true;
			cameraInc.z = -1;
		} else if (window.isKeyPressed(GLFW_KEY_S)) {
			sceneChanged = true;
			cameraInc.z = 1;
		}
		if (window.isKeyPressed(GLFW_KEY_A)) {
			sceneChanged = true;
			cameraInc.x = -1;
		} else if (window.isKeyPressed(GLFW_KEY_D)) {
			sceneChanged = true;
			cameraInc.x = 1;
		}
		if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
			sceneChanged = true;
			cameraInc.y = -1;
		} else if (window.isKeyPressed(GLFW_KEY_SPACE)) {
			sceneChanged = true;
			cameraInc.y = 1;
		}

		return sceneChanged;
	}

	public boolean update(final MouseInput mouseInput, final boolean sceneChanged) {
		boolean sceneC = sceneChanged;

		final Vector2f rotVec = mouseInput.getDisplVec();
		rotate(rotVec.x * sensivity, rotVec.y * sensivity, 0);
		sceneC = rotVec.equals(0, 0) ? sceneC : true;

		// Update camera position
		translate(cameraInc.x * speed, cameraInc.y * speed, cameraInc.z * speed);

		updateViewMatrix();

		return sceneC;
	}
}