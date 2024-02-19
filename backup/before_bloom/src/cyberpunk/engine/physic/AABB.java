package hengine.engine.physic;

import org.joml.Vector3f;

public class AABB {

	private final Vector3f min;

	private final Vector3f max;

	public AABB(final Vector3f min, final Vector3f max) {
		this.min = new Vector3f(min);
		this.max = new Vector3f(max);
	}

	public void setMin(final Vector3f min) {
		this.min.set(min);
	}

	public Vector3f getMin() {
		return min;
	}

	public void setMax(final Vector3f max) {
		this.max.set(max);
	}

	public Vector3f getMax() {
		return max;
	}

	public IntersectionData intersectAABB(final AABB other) {

		final Vector3f distances1 = new Vector3f(other.getMin()).sub(max);
		final Vector3f distances2 = new Vector3f(min).sub(other.getMax());
		
		final Vector3f distances = distances1.max(distances2);
		
		final float maxDistances = distances.maxComponent();
		
		return new IntersectionData(maxDistances < 0, maxDistances);
	}
}
