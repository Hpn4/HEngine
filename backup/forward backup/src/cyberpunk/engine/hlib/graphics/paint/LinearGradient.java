package hengine.engine.hlib.graphics.paint;

import static org.lwjgl.nanovg.NanoVG.nvgLinearGradient;

import org.lwjgl.nanovg.NVGPaint;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.utils.Rectangle;

public class LinearGradient implements Paint2 {

	private Rectangle rect;

	private HComponent comp;

	private Color startCol;

	private Color endColor;

	private NVGPaint gradient;

	public LinearGradient(final int x, final int y, final int width, final int height, final Color colStart,
			final Color colEnd) {
		rect = new Rectangle(x, y, x + width, y + height);
		startCol = colStart;
		endColor = colEnd;
		gradient = NVGPaint.create();
	}

	public LinearGradient(final HComponent comp, final Color colStart, final Color colEnd) {
		this.comp = comp;
		startCol = colStart;
		endColor = colEnd;
		gradient = NVGPaint.create();
	}

	public void init(final long ctx, final int x, final int y) {
		if (rect == null) {
			nvgLinearGradient(ctx, comp.getPaintX(), comp.getPaintY(), comp.getPaintWidth(), comp.getPaintHeight(),
					startCol.getColor(), endColor.getColor(), gradient);
		} else
			nvgLinearGradient(ctx, rect.x, rect.y, rect.width, rect.height, startCol.getColor(), endColor.getColor(),
					gradient);
	}

	public void cleanUp(long ctx) {
		startCol.cleanUp(ctx);
		endColor.cleanUp(ctx);
		gradient.free();
	}

	public boolean isColor() {
		return false;
	}

	public NVGPaint getNVGPaint() {
		return gradient;
	}

}
