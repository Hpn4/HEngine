package hengine.engine.physic;

import org.joml.Vector3f;

public class Force {

	private final Vector3f force;

	private final Vector3f pointApplication;

	public Force() {
		force = new Vector3f();
		pointApplication = new Vector3f();
	}

	public Force(final Vector3f force, final Vector3f pointA) {
		this.force = new Vector3f(force);
		pointApplication = new Vector3f(pointA);
	}

	public Vector3f getPointApplication() {
		return pointApplication;
	}

	public void setPointApplication(final Vector3f pointA) {
		pointApplication.set(pointA);
	}

	public Vector3f getForce() {
		return force;
	}

	public void setForce(final Vector3f force) {
		this.force.set(force);
	}
}
