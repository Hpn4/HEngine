package hengine.engine.world.item;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import hengine.engine.graph.Transformation;
import hengine.engine.utils.loader.texture.AbstractTexture;

public class Decal {

	private final Vector3f position;

	private final Quaternionf rotation;

	private float scale;

	private final Matrix4f modelMatrix;

	private final Matrix4f invModelMatrix;

	private AbstractTexture texture;

	public Decal(final AbstractTexture texture) {
		position = new Vector3f();
		rotation = new Quaternionf();
		scale = 1;

		modelMatrix = new Matrix4f();
		invModelMatrix = new Matrix4f();
		setTexture(texture);
	}

	public Decal(final AbstractTexture texture, final Vector3f position) {
		this.position = new Vector3f(position);
		rotation = new Quaternionf(0, 0, 0, 1);
		scale = 1;

		modelMatrix = new Matrix4f();
		invModelMatrix = new Matrix4f();
		setTexture(texture);
	}

	public Decal(final AbstractTexture texture, final Vector3f position, final Quaternionf rotation,
			final float scale) {
		this.rotation = new Quaternionf(rotation);
		this.position = new Vector3f(position);
		this.scale = scale;

		modelMatrix = new Matrix4f();
		invModelMatrix = new Matrix4f();
		setTexture(texture);
	}

	public AbstractTexture getTexture() {
		return texture;
	}

	public void setTexture(final AbstractTexture texture) {
		this.texture = texture;
	}

	public Matrix4f getModelMatrix() {
		return modelMatrix;
	}

	/**
	 * Met a jour la matrice du modele a partir de ses stats ( position, rotation et
	 * taille ) et génère son inverse.
	 */
	public void updateMatrix() {
		modelMatrix.set(Transformation.buildModelMatrix(position, rotation, scale));
		invModelMatrix.set(modelMatrix).invert();
	}

	public Matrix4f getInvModelMatrix() {
		return invModelMatrix;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(final float x, final float y, final float z) {
		position.set(x, y, z);
	}

	public Quaternionf getRotation() {
		return rotation;
	}

	public void setRotation(final Quaternionf q) {
		rotation.set(q);
	}

	public void setRotation(final float xOffset, final float yOffset, final float zOffset) {
		Transformation.rotate(rotation, xOffset, yOffset, zOffset);
	}

	public float getScale() {
		return scale;
	}

	public void setScale(final float scale) {
		this.scale = scale;
	}
}
