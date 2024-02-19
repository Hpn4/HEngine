package hengine.engine.hlib.border;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.graphics.paint.Paint;
import hengine.engine.hlib.utils.Insets;

public interface Border {

	void paintBorder(final HComponent h, final Graphics g, final int x, final int y, final int width, final int height);

	void setPaint(final Paint paint);

	void cleanUp(final long ctx);

	Insets getBorderInsets();
}
