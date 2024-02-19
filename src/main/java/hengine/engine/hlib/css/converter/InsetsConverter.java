package hengine.engine.hlib.css.converter;

import hengine.engine.hlib.utils.Insets;

public class InsetsConverter extends Converter<Insets> {

	private static final IntConverter intConv;

	static {
		intConv = new IntConverter(1);
	}

	public InsetsConverter(final Insets defaultValue) {
		super(defaultValue);
	}

	public Insets convert(final String string) {
		final Insets insets = new Insets(1);
		final String[] ins = string.split(" ");

		final int size = ins.length;
		final int all = intConv.convert(ins[0]);

		// Si un seul parametre, tous sont mis a cette valeur
		insets.top = insets.bot = insets.right = insets.left = all;

		if (size >= 2) {
			// Si deux parametre, bot et top ensemble. Left et right ensemble
			insets.top = insets.bot = all;
			insets.right = insets.left = intConv.convert(ins[1]);
		}

		if (size >= 3)
			insets.bot = intConv.convert(ins[2]);

		if (size >= 4)
			insets.left = intConv.convert(ins[2]);
		
		value = insets;

		return value;
	}
}
