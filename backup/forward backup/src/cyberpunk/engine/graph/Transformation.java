package hengine.engine.graph;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import hengine.engine.item.GameItem;

public class Transformation {

	private static final Matrix4f modelMatrix = new Matrix4f();

	public static Matrix4f updateGenericViewMatrix(final Vector3f position, final Vector3f rotation,
			final Matrix4f matrix) {
		return matrix.rotationX((float) Math.toRadians(rotation.x)).rotateY((float) Math.toRadians(rotation.y))
				.translate(-position.x, -position.y, -position.z);
	}

	public static Matrix4f buildModelMatrix(final GameItem gameItem) {
		return buildModelMatrix(gameItem.getPosition(), gameItem.getRotation(), gameItem.getScale());
	}

	public static Matrix4f buildModelMatrix(final Vector3f position, final Quaternionf rotation, final float scale) {
		return modelMatrix.translationRotateScale(position.x, position.y, position.z, rotation.x, rotation.y,
				rotation.z, rotation.w, scale, scale, scale);
	}
}