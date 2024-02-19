package hengine.engine.world.item;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import hengine.engine.graph.Transformation;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.utils.Box3D;

public class GameItem implements Comparable<GameItem> {

	private Decal[] decals;

	private Box3D[] box3ds;

	protected Vector3f position;

	protected Quaternionf rotation;

	protected float scale;

	private float viewSpaceDepth;

	protected final Matrix4f modelMatrix;

	protected final Matrix3f normalMatrix;

	protected AbstractMesh[] meshes;

	private boolean insideFrustum;

	private boolean stateChange;

	private int textPos;

	public GameItem() {
		modelMatrix = new Matrix4f();
		normalMatrix = new Matrix3f();

		scale = 1f;
		position = new Vector3f();
		rotation = new Quaternionf();
		insideFrustum = stateChange = true;
		textPos = 1;
	}

	public GameItem(final GameItem item) {
		position = new Vector3f(item.getPosition());
		rotation = new Quaternionf(item.getRotation());

		modelMatrix = new Matrix4f(item.getModelMatrix());
		normalMatrix = new Matrix3f(item.getNormalMatrix());

		scale = item.getScale();
		setMeshes(item.getMeshes());

		textPos = item.getTextPos();
		stateChange = true;
	}

	public GameItem(final AbstractMesh mesh) {
		this();
		setMesh(mesh);
	}

	public GameItem(final AbstractMesh... meshes) {
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
		Transformation.rotate(rotation, x, y, z);
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

	public Matrix3f getNormalMatrix() {
		return normalMatrix;
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

	public Decal[] getDecals() {
		return decals;
	}

	public void setDecals(final Decal[] decals) {
		this.decals = decals;

		final int numDecals = decals != null ? decals.length : 0;
		for (int i = 0; i < numDecals; i++)
			decals[i].updateMatrix();
	}

	public void updateBox3D() {
		modelMatrix.set(Transformation.buildModelMatrix(this));

		final Matrix4f tmp = new Matrix4f(modelMatrix);
		tmp.invert();
		tmp.transpose();
		normalMatrix.set(tmp);

		final Vector3f origin = new Vector3f(), dim = new Vector3f();
		for (int i = 0, cMeshes = meshes.length; i < cMeshes; i++) {
			final Box3D box = meshes[i].getBox();
			final Vector3f min = box.origin, max = box.dim;
			origin.set(Float.MAX_VALUE);
			dim.set(Float.MIN_VALUE);
			
			// On creer les 8 point du rectangle à partir de l'origine et de la dimension
			final Vector3f[] pos = new Vector3f[] {
				new Vector3f(min),
				new Vector3f(min.x, min.y, max.z),
				new Vector3f(max.x, min.y, max.z),
				new Vector3f(max.x, min.y, min.z),
				
				new Vector3f(min.x, max.y, min.z),
				new Vector3f(min.x, max.y, max.z),
				new Vector3f(max),
				new Vector3f(max.x, max.y, min.z)
			};
			
			// Pour chacun des point, on le passe en world pose et on extrait le min et ou le max
			for(int j = 0; j < 8; j++) {
				final Vector3f cord = pos[j].mulProject(getModelMatrix());
				
				if(cord.x < origin.x)
					origin.x = cord.x;
				if(cord.y < origin.y)
					origin.y = cord.y;
				if(cord.z < origin.z)
					origin.z = cord.z;
				
				if(cord.x > dim.x)
					dim.x = cord.x;
				if(cord.y > dim.y)
					dim.y = cord.y;
				if(cord.z > dim .z)
					dim.z = cord.z;
			}
		
			// On construit la nouvelle box de l'objet
			box3ds[i] = new Box3D(origin, dim);
		}

		stateChange = false;
	}
	public void cleanup() {
		if (meshes != null)
			for (final AbstractMesh mesh : meshes)
				mesh.cleanup();
	}

	public void updateViewSpaceDepth(final Matrix4f viewMatrix) {
		viewSpaceDepth = new Vector4f(box3ds[0].origin, 1).mul(viewMatrix).z;
	}

	public float getViewSpaceDepth() {
		return viewSpaceDepth;
	}

	@Override
	public int compareTo(final GameItem item) {
		return Float.compare(item.getViewSpaceDepth(), viewSpaceDepth);
	}
}