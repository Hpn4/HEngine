package hengine.engine;

import org.joml.Vector2d;
import org.joml.Vector2f;

import hengine.engine.hlib.event.MouseEvent;

public class MouseInput {

	private final Vector2d previousPos;

	private final Vector2f displVec;
	
	private boolean right;

	public MouseInput() {
		previousPos = new Vector2d(-1, -1);
		displVec = new Vector2f();
	}

	public Vector2f getDisplVec() {
		return displVec;
	}

	public void input(final MouseEvent event) {
		if (event != null) {
			displVec.x = 0;
			displVec.y = 0;
			final int x = event.getX(), y = event.getY();
			if (previousPos.x > 0 && previousPos.y > 0) {
				final double deltax = x - previousPos.x, deltay = y - previousPos.y;
				if (deltax != 0)
					displVec.y = (float) deltax;

				if (deltay != 0)
					displVec.x = (float) deltay;

			}
			previousPos.x = x;
			previousPos.y = y;
			
			right = event.isButton(MouseEvent.BUTTON_RIGHT);
		}
	}
	
	public boolean isRightButtonPressed() {
		return right;
	}
}
