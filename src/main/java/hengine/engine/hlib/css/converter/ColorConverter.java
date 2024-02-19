package hengine.engine.hlib.css.converter;

import hengine.engine.hlib.graphics.paint.Color;

public class ColorConverter extends Converter<Color> {

	private static final IntConverter intConv;
	
	static {
		intConv = new IntConverter(255);
	}
	
	public ColorConverter(final Color defaultValue) {
		super(defaultValue);
	}

	@Override
	public Color convert(String string) {
		string = string.replace(" ", "");

		if (string.startsWith("rgb(")) {
			string = string.substring(4, string.length() - 1);

			final String[] col = string.split(",");
			final int[] v = new int[3];
			for (int i = 0; i < 3; i++)
				v[i] = intConv.convert(col[i]);

			value = new Color(v[0], v[1], v[2]);

		} else if (string.startsWith("rgba(")) {
			string = string.substring(5, string.length() - 1);

			final String[] col = string.split(",");
			final int[] v = new int[4];
			for (int i = 0; i < 4; i++)
				v[i] = intConv.convert(col[i]);

			value = new Color(v[0], v[1], v[2], v[3]);

		} else if (string.startsWith("#")) {
			// On enleve le #
			string = string.substring(1);

			int r, g, b, a = 255;

			// On convertit d'hexadecimal en decimal
			r = Integer.parseInt(string.substring(0, 2), 16);
			g = Integer.parseInt(string.substring(2, 4), 16);
			b = Integer.parseInt(string.substring(4, 6), 16);
			if (string.length() == 8)
				a = Integer.parseInt(string.substring(6, 8), 16);

			value = new Color(r, g, b, a);
		} else {
			string = string.toUpperCase();

			// On verifie si c'est une couleur simple déja enregistré. Sinon on attribut la
			// valeur par défaut
			switch (string) {
			case "ALICEBLUE":
				value = Color.ALICEBLUE;
				break;
			case "BLACK":
				value = Color.BLACK;
				break;
			case "CORNFLOWERBLUE":
				value = Color.CORNFLOWERBLUE;
				break;
			case "DARKGRAY":
				value = Color.DARKGRAY;
				break;
			case "GRAY":
				value = Color.GRAY;
				break;
			case "RED":
				value = Color.RED;
				break;
			case "WHITE":
				value = Color.WHITE;
				break;
			case "YELLOW":
				value = Color.YELLOW;
				break;
			default:
				value = defaultValue;
				break;
			}
		}
		
		return value;
	}
}
