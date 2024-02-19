package hengine.engine.graph;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import hengine.engine.utils.Utils;
import hengine.engine.world.item.GameItem;

public class Transformation {

	private static final Matrix4f tmp = new Matrix4f();
	
	public static Matrix4f updateGenericViewMatrix(final Vector3f position, final Vector3f rotation,
			final Matrix4f matrix) {
		return matrix.rotationX(Utils.toRadians(rotation.x)).rotateY(Utils.toRadians(rotation.y))
				.translate(-position.x, -position.y, -position.z);
	}

	public static Matrix4f buildModelMatrix(final GameItem gameItem) {
		return buildModelMatrix(gameItem.getPosition(), gameItem.getRotation(), gameItem.getScale());
	}

	public static Matrix4f buildModelMatrix(final Vector3f position, final Quaternionf rotation, final float scale) {
		return tmp.translationRotateScale(position, rotation, scale);
	}
	
	public static Quaternionf rotate(final Quaternionf rotation, final float x, final float y, final float z) {
		return rotation.rotateXYZ(Utils.toRadians(x), Utils.toRadians(y), Utils.toRadians(z));
	}
	
	public static Matrix4f buildProjectionViewModelMatrix(final Matrix4f projectionViewMatrix, final Matrix4f modelMatrix) {
		return projectionViewMatrix.mul(modelMatrix, tmp);
	}
	
	public static Matrix4f buildProjectionViewMatrix(final Matrix4f projectionMatrix, final Matrix4f viewMatrix) {
		return projectionMatrix.mul(viewMatrix, tmp);
	}
}