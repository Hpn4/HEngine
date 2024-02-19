package hengine.engine.physic;

import org.joml.Vector3f;

public class Plane {

	private final Vector3f normal;

	private float distance;

	public Plane(final Vector3f normal, final float distance) {
		this.normal = new Vector3f(normal);
		this.distance = distance;
	}

	public void setNormal(final Vector3f normal) {
		this.normal.set(normal);
	}

	public Vector3f getNormal() {
		return normal;
	}

	public void setDistance(final float distance) {
		this.distance = distance;
	}

	public float getDistance() {
		return distance;
	}
}
