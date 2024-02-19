package hengine.engine.hlib.css;

import hengine.engine.hlib.border.BevelBorder;
import hengine.engine.hlib.border.Border;
import hengine.engine.hlib.border.Borders;
import hengine.engine.hlib.border.EtchedBorder;
import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.css.converter.BooleanConverter;
import hengine.engine.hlib.css.converter.ColorConverter;
import hengine.engine.hlib.css.converter.InsetsConverter;
import hengine.engine.hlib.css.converter.IntConverter;
import hengine.engine.hlib.css.converter.PaintConverter;
import hengine.engine.hlib.graphics.paint.Color;
import hengine.engine.hlib.graphics.paint.Paint;
import hengine.engine.hlib.utils.Background;
import hengine.engine.hlib.utils.Insets;

public class HComponentStyle {

	private int x;

	private int y;

	private int width;

	private int height;

	private Insets margin;

	private Insets margout;

	private Border border;

	private Background bg;

	private boolean enabled;

	private boolean visible;

	protected final static IntConverter intConv;

	protected final static InsetsConverter insConv;

	protected final static PaintConverter paintConv;

	protected final static BooleanConverter enabledConv;

	protected final static BooleanConverter visibleConv;

	protected final static ColorConverter colorConv;

	static {
		intConv = new IntConverter(0);
		insConv = new InsetsConverter(null);
		paintConv = new PaintConverter(null);
		enabledConv = new BooleanConverter(true);
		visibleConv = new BooleanConverter(false);
		colorConv = new ColorConverter(Color.BLACK);
	}

	public HComponentStyle() {

	}

	public HComponentStyle(String part) {
		defaultValue();
		part = part.replace("\n", "");
		part = part.replace(": ", ":");

		final String[] lines = part.split(";");
		for (String line : lines) {
			final int index = line.indexOf(":");
			final String key = line.substring(0, index);
			line = line.substring(index + 1);

			doLine(key, line);
		}
	}

	protected void defaultValue() {
		// Default value
		x = y = 0;
		margin = new Insets(1);
		margout = new Insets(1);
		enabled = true;
		visible = false;
	}

	protected boolean doLine(final String key, final String line) {
		switch (key) {
		case "x":
			x = intConv.convert(line);
			return true;
		case "y":
			y = intConv.convert(line);
			return true;
		case "width":
			width = intConv.convert(line);
			return true;
		case "height":
			height = intConv.convert(line);
			return true;
		case "padding":
			margin = insConv.convert(line);
			return true;
		case "padding-top":
			margin.top = intConv.convert(line);
			return true;
		case "padding-right":
			margin.right = intConv.convert(line);
			return true;
		case "padding-left":
			margin.left = intConv.convert(line);
			return true;
		case "padding-bottom":
			margin.bot = intConv.convert(line);
			return true;
		case "margin":
			margout = insConv.convert(line);
			return true;
		case "margin-top":
			margout.top = intConv.convert(line);
			return true;
		case "margin-right":
			margout.right = intConv.convert(line);
			return true;
		case "margin-left":
			margout.left = intConv.convert(line);
			return true;
		case "margin-bottom":
			margout.bot = intConv.convert(line);
			return true;

		case "line-border":
			String paint = line;
			int height = 1;
			if (line.contains(" ")) {
				final String[] es = line.split(" ");
				paint = es[0];
				height = intConv.convert(es[1]);
				if (height == 0)
					height = 1;
			}

			Paint p = paintConv.convert(paint);
			if (p == null)
				p = Color.BLACK;

			border = Borders.line(p, height);
			return true;
		case "bevel-border":
			final String[] es = line.split(" ");
			int type;
			if (es[0].toUpperCase().equals("LOWERED"))
				type = BevelBorder.LOWERED;
			else
				type = BevelBorder.RAISED;

			final int size = es.length;
			if (size == 2)
				border = new BevelBorder(type, colorConv.convert(es[1]));
			else if (size == 3)
				border = new BevelBorder(type, colorConv.convert(es[1]), colorConv.convert(es[2]));
			else if (size == 5)
				border = new BevelBorder(type, colorConv.convert(es[1]), colorConv.convert(es[2]),
						colorConv.convert(es[3]), colorConv.convert(es[4]));

			return true;
		case "etched-border":
			final String[] es1 = line.split(" ");
			int type1;
			if (es1[0].toUpperCase().equals("LOWERED"))
				type1 = EtchedBorder.LOWERED;
			else
				type1 = EtchedBorder.RAISED;

			final int size1 = es1.length;
			if (size1 == 2)
				border = new EtchedBorder(type1, colorConv.convert(es1[1]));
			else if (size1 == 3)
				border = new EtchedBorder(type1, colorConv.convert(es1[1]), colorConv.convert(es1[2]));

			return true;

		case "background":
			bg = new Background(paintConv.convert(line));
			System.err.println(bg.getPaint());
			return true;
		case "enabled":
			enabled = enabledConv.convert(line);
			return true;
		case "visible":
			visible = visibleConv.convert(line);
			return true;
		}

		return false;
	}

	public HComponentStyle doPart(final String part) {
		return new HComponentStyle(part);
	}

	public void set(final HComponent h) {
		h.setX(x);
		h.setY(y);

		h.setWidth(width);
		h.setHeight(height);

		h.setMargin(margin);
		h.setMargout(margout);

		h.setBorder(border);
		h.setBackground(bg);

		h.setEnabled(enabled);
		h.setVisible(visible);
	}

	public int getX() {
		return x;
	}

	public void setX(final int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(final int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public Insets getMargin() {
		return margin;
	}

	public void setMargin(final Insets margin) {
		this.margin = margin;
	}

	public Insets getMargout() {
		return margout;
	}

	public void setMargout(final Insets margout) {
		this.margout = margout;
	}

	public Border getBorder() {
		return border;
	}

	public void setBorder(final Border border) {
		this.border = border;
	}

	public Background getBg() {
		return bg;
	}

	public void setBg(final Background bg) {
		this.bg = bg;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}
}
