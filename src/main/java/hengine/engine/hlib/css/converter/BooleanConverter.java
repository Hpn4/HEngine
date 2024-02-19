package hengine.engine.hlib.css.converter;

public class BooleanConverter extends Converter<Boolean> {

	public BooleanConverter(final Boolean defaultValue) {
		super(defaultValue);
	}

	public Boolean convert(String string) {
		string = string.toUpperCase();

		if (string.equals("TRUE"))
			value = true;
		else if (string.equals("FALSE"))
			value = false;
		else
			value = defaultValue;
		
		return value;
	}
}
