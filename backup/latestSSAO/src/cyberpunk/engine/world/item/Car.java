package hengine.engine.world.item;

import org.joml.Vector3f;

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

		final Vector3f angleV = frontWheel.getRotation().getEulerAnglesXYZ(new Vector3f());
		//xDiff *= wheelRadius;
		double yDiff = Math.sin(Math.toRadians(angleV.x));
		//yDiff *= wheelRadius;

		//System.out.println(xDiff + " y:" + yDiff);
		frontWheel.getPosition().y -= yDiff * 0.5;
		frontWheel.getPosition().x -= yDiff * 0.5;

		body.updateBox3D();
		frontWheel.updateBox3D();
		backWheel.updateBox3D();
		
		//frontWheel.getPosition().y = befY;
	}
}
