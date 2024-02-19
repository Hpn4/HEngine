package hengine.engine.hlib.css.converter;

import java.nio.file.Files;
import java.nio.file.Paths;

import hengine.engine.hlib.graphics.paint.Image;

public class ImageConverter extends Converter<Image> {

	public ImageConverter(final String string) {
		super(null);
	}

	public Image convert(final String string) {
		if (Files.exists(Paths.get(string)))
			value = new Image(string);
		else
			value = null;
		
		return value;
	}
}
