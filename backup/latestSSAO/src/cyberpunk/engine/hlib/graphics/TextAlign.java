package hengine.engine.hlib.graphics;

import static org.lwjgl.nanovg.NanoVG.*;

public enum TextAlign {

	BASELINE(NVG_ALIGN_BASELINE), BOTTOM(NVG_ALIGN_BOTTOM), CENTER(NVG_ALIGN_CENTER), LEFT(NVG_ALIGN_LEFT),
	MIDDLE(NVG_ALIGN_MIDDLE), RIGHT(NVG_ALIGN_RIGHT), TOP(NVG_ALIGN_TOP);

	final int align;

	TextAlign(final int align) {
		this.align = align;
	}

	public int getAlign() {
		return align;
	}
}
