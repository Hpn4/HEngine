package hengine.engine.hlib.utils;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.graphics.paint.Paint;

public class Background {

	private Paint paint;

	public Background(final Paint paint) {
		this.paint = paint;
	}

	public void paintBackground(final HComponent c, final Graphics g, final int x, final int y, final int width,
			final int height) {

		g.setPaint(paint);
		g.fillRect(x, y, width, height);
	}

	public Paint getPaint() {
		return paint;
	}

	public void cleanUp(final long ctx) {
		paint.cleanUp(ctx);
	}
}
