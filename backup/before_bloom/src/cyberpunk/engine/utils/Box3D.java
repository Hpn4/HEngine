package hengine.engine.utils;

import org.joml.Vector3f;

public class Box3D {

	public Vector3f origin;

	public Vector3f dim;

	public Box3D(final Box3D box3d) {
		this(box3d.origin, box3d.dim);
	}

	public Box3D(final Vector3f origin, final Vector3f dim) {
		this.origin = new Vector3f(origin);
		this.dim = new Vector3f(dim);
	}

	public boolean isInside(final Vector3f toTest) {
		final float x = toTest.x, y = toTest.y, z = toTest.z;
		if (x >= x() && x < width() && (y >= y() && y < height()) && (z >= z() && z < depth()))
			return true;
		return false;
	}

	public String toString() {
		return "origin : " + origin.toString() + ", end : " + dim.toString();
	}

	public float x() {
		return origin.x;
	}

	public float y() {
		return origin.y;
	}

	public float z() {
		return origin.z;
	}

	public float width() {
		return dim.x;
	}

	public float height() {
		return dim.y;
	}

	public float depth() {
		return dim.z;
	}
}
