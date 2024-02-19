package hengine.engine.physic;

import org.joml.Vector3f;

public class BoundingSpheres {

	private final Vector3f center;

	private float radius;

	public BoundingSpheres(final Vector3f center, final float radius) {
		this.center = new Vector3f(center);
		this.radius = radius;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(final float radius) {
		this.radius = radius;
	}

	public Vector3f getCenter() {
		return center;
	}

	public void setCenter(final Vector3f center) {
		this.center.set(center);
	}

	public IntersectionData intersectBoundingSpheres(final BoundingSpheres toTest) {
		final float radiusDistance = toTest.getRadius() + radius,
				centerDistance = new Vector3f(center).sub(toTest.getCenter()).length();

		return new IntersectionData(centerDistance < radiusDistance, centerDistance - radiusDistance);
	}
}
