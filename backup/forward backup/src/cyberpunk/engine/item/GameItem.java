package hengine.engine.item;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import hengine.engine.graph.Transformation;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.utils.Box3D;

public class GameItem {

	private Box3D[] box3ds;

	protected Vector3f position;

	protected Quaternionf rotation;

	protected float scale;

	protected final Matrix4f modelMatrix;

	protected AbstractMesh[] meshes;

	private boolean selected;

	private boolean insideFrustum;

	private boolean stateChange;

	private int textPos;

	public GameItem() {
		modelMatrix = new Matrix4f();
		scale = 1f;
		position = new Vector3f();
		rotation = new Quaternionf();
		selected = false;
		insideFrustum = stateChange = true;
		textPos = 1;
	}

	public GameItem(final GameItem item) {
		position = new Vector3f(item.getPosition());
		rotation = new Quaternionf(item.getRotation());
		modelMatrix = new Matrix4f(item.getModelMatrix());
		scale = item.getScale();
		setMeshes(item.getMeshes());
		selected = item.isSelected();
		textPos = item.getTextPos();
		stateChange = true;
	}

	public GameItem(final AbstractMesh mesh) {
		this();
		setMesh(mesh);
	}

	public GameItem(final AbstractMesh[] meshes) {
		this();
		setMeshes(meshes);
	}

	public AbstractMesh getMesh() {
		return meshes[0];
	}

	public AbstractMesh[] getMeshes() {
		return meshes;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(final float x, final float y, final float z) {
		position.set(x, y, z);
		stateChange = true;
	}

	public Quaternionf getRotation() {
		return rotation;
	}

	public void setRotation(final float x, final float y, final float z) {
		rotation.rotateXYZ((float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
		stateChange = true;
	}

	public void setRotation(final Quaternionf q) {
		rotation.set(q);
		stateChange = true;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(final float scale) {
		this.scale = scale;
		stateChange = true;
	}

	public Matrix4f getModelMatrix() {
		return modelMatrix;
	}

	public Box3D[] getBox3D() {
		return box3ds;
	}

	public void setMesh(final AbstractMesh mesh) {
		meshes = new AbstractMesh[] { mesh };
		box3ds = new Box3D[] { new Box3D(mesh.getBox()) };
	}

	public void setMeshes(final AbstractMesh[] meshes) {
		this.meshes = meshes;

		box3ds = new Box3D[meshes.length];
		for (int i = 0, c = meshes.length; i < c; i++)
			box3ds[i] = new Box3D(meshes[i].getBox());
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(final boolean selected) {
		this.selected = selected;
	}

	public boolean isInsideFrustum() {
		return insideFrustum;
	}

	public void setInsideFrustum(final boolean inside) {
		insideFrustum = inside;
	}

	public boolean isStatChange() {
		return stateChange;
	}

	public void setStateChange(final boolean stateChange) {
		this.stateChange = stateChange;
	}

	public int getTextPos() {
		return textPos;
	}

	public void setTextPos(final int textPos) {
		this.textPos = textPos;
	}

	public void updateBox3D() {
		modelMatrix.set(Transformation.buildModelMatrix(this));

		for (int i = 0, cMeshes = meshes.length; i < cMeshes; i++) {

			final FloatBuffer buf = meshes[i].getVertices();
			final Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE),
					max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
			for (int j = 0, c = buf.limit(); j < c;) {
				Vector4f vec4 = new Vector4f(buf.get(), buf.get(), buf.get(), 1);
				vec4.mul(modelMatrix, vec4);
				j += 3;

				if (vec4.x < min.x)
					min.x = vec4.x;
				if (vec4.y < min.y)
					min.y = vec4.y;
				if (vec4.z < min.z)
					min.z = vec4.z;

				if (vec4.x > max.x)
					max.x = vec4.x;
				if (vec4.y > max.y)
					max.y = vec4.y;
				if (vec4.z > max.z)
					max.z = vec4.z;
			}

			box3ds[i] = new Box3D(min, max);
			MemoryUtil.memFree(buf);
		}
		
		stateChange = false;
	}

	public void cleanUp() {
		if (meshes != null)
			for (final AbstractMesh mesh : meshes)
				mesh.cleanUp();
	}
}