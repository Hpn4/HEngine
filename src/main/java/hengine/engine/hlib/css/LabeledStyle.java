package hengine.engine.hlib.css;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.component.Labeled;
import hengine.engine.hlib.css.converter.IntConverter;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.graphics.paint.Color;
import hengine.engine.hlib.graphics.paint.Paint;

public class LabeledStyle extends HComponentStyle {

	private Paint fg;

	private int fontSize;

	protected static final IntConverter fontConv;

	static {
		fontConv = new IntConverter(20);
	}

	public LabeledStyle() {

	}

	public LabeledStyle(final String part) {
		super(part);
	}

	protected void defaultValue() {
		super.defaultValue();
		fg = Color.BLACK;
		fontSize = Graphics.FONT_SIZE;
	}

	public boolean doLine(final String key, final String line) {
		if (!super.doLine(key, line)) {
			if (key.equals("color")) {
				fg = paintConv.convert(line);
				if (fg == null)
					fg = Color.BLACK;
				return true;
			} else if (key.equals("font-size"))
				fontSize = fontConv.convert(line);
		}

		return false;
	}

	public HComponentStyle doPart(final String part) {
		return new LabeledStyle(part);
	}

	public void set(final HComponent comp) {
		super.set(comp);

		final Labeled l = (Labeled) comp;
		l.setForeground(fg);
		l.setFontHeight(fontSize);
	}

	public Paint getFg() {
		return fg;
	}

	public void setFg(final Paint fg) {
		this.fg = fg;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(final int fontSize) {
		this.fontSize = fontSize;
	}

}
