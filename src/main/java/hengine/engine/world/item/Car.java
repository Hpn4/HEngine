package hengine.engine.world.item;

public class Car {

	private GameItem body;

	private GameItem frontWheel;

	private GameItem backWheel;

	private float wheelRadius;

	public Car(final GameItem body, final GameItem fWheel, final GameItem bWheel, final float radius) {
		this.body = body;
		frontWheel = fWheel;
		backWheel = bWheel;
		wheelRadius = radius;
	}

	public void setPosition(final float x, final float y, final float z) {
		body.setPosition(x, y, z);
		frontWheel.setPosition(x, y, z);
		backWheel.setPosition(x, y, z);
	}

	public void rotate(final float x, final float y, final float z) {
		body.setRotation(x, y, z);
		frontWheel.setRotation(x, y, z);
		backWheel.setRotation(x, y, z);
	}

	public void addX(final float x) {
		body.getPosition().x += x;
		frontWheel.getPosition().x += x;
		backWheel.getPosition().x += x;

		final double circ = (float) (2 * Math.PI * wheelRadius);
		final double angle = 360 / (circ / x);

		frontWheel.setRotation((float) angle, 0, 0);
		backWheel.setRotation((float) angle, 0, 0);

		body.updateBox3D();
		frontWheel.updateBox3D();
		backWheel.updateBox3D();

		// frontWheel.getPosition().y = befY;
	}

	public GameItem[] getItems() {
		return new GameItem[] { body, frontWheel, backWheel };
	}

	public void cleanup() {
		body.cleanup();
		frontWheel.cleanup();
		backWheel.cleanup();
	}
}
