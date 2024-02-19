package hengine.engine.hlib.css.converter;

public class IntConverter extends Converter<Integer> {

	public IntConverter(final int defaultValue) {
		super(defaultValue);
	}

	public Integer convert(final String string) {
		try {
			value = Integer.parseInt(string);
		} catch (final NumberFormatException e) {
			value = defaultValue;
		}
		
		return value;
	}
}
