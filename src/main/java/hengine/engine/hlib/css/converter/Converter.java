package hengine.engine.hlib.css.converter;

public abstract class Converter<T> {

	protected T value;

	protected T defaultValue;

	public Converter(final T defaultValue) {
		this.defaultValue = defaultValue;
	}

	public abstract T convert(String string);

	public T getValue() {
		return value;
	}

}
