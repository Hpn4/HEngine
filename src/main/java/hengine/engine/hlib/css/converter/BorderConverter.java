package hengine.engine.hlib.css.converter;

import hengine.engine.hlib.graphics.paint.Color;
import hengine.engine.hlib.graphics.paint.Paint;

public class BorderConverter extends Converter<Paint> {

	private final static ImageConverter image;

	private final static ColorConverter color;

	static {
		image = new ImageConverter(null);
		color = new ColorConverter(Color.WHITE);
	}

	public BorderConverter(final Paint defaultValue) {
		super(defaultValue);
	}

	public Paint convert(final String string) {		
		if (string.startsWith("img("))
			value = image.convert(string.substring(5, string.length() - 2));
		else
			value = color.convert(string);
		
		return value;
	}
}
