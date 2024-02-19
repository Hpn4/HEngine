package hengine.engine.hlib.border;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.graphics.paint.Paint;
import hengine.engine.hlib.utils.Insets;

public class EmptyBorder implements Border {

	public EmptyBorder() {
	}

	public void paintBorder(final HComponent h, final Graphics g, final int x, final int y, final int width,
			final int height) {
	}

	public void setPaint(Paint paint) {
	}

	public Insets getBorderInsets() {
		return new Insets(0);
	}

	public void cleanUp(final long ctx) {
	}
}
