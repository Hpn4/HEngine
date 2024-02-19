package hengine.engine.physic;

public class IntersectionData {

	private final boolean isCollide;

	private final float distance;

	public IntersectionData(final boolean isCollide, final float distance) {
		this.isCollide = isCollide;
		this.distance = distance;
	}

	public boolean isCollide() {
		return isCollide;
	}

	public float getDistance() {
		return distance;
	}
}
