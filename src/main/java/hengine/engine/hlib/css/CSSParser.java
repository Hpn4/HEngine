package hengine.engine.hlib.css;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import hengine.engine.hlib.component.HComponent;

public class CSSParser {

	private static final HashMap<String, HComponentStyle> styles;

	static {
		final HComponentStyle style = new HComponentStyle();
		styles = new HashMap<>();

		styles.put("component", style);
		styles.put("container", style);

		styles.put("labeled", new LabeledStyle());
		styles.put("label", new LabeledStyle());
	}
	private HashMap<String, CSSData> data;

	public CSSParser(final String file) {
		try {
			final String content = Files.readString(Paths.get(file));
			data = new HashMap<>();

			final String[] section = content.split("}");

			for (int i = 0; i < section.length; i++) {
				String part = section[i];
				String key = part.substring(0, part.indexOf("{"));

				part = part.replace("\t", "");

				// On vire les sauts de lignes et les espaces
				key = key.replace("\n", "");
				key = key.replace(" ", "");

				// Si il y a un event, on le récupere
				CSSEvent event = CSSEvent.DEFAULT;
				if (key.contains(":")) {
					final int index = key.indexOf(":");
					final String pseudo = key.substring(index + 1).toUpperCase();
					key = key.substring(0, index);

					event = CSSEvent.valueOf(pseudo);
				}

				// Si il y a une condition on l'enregistre
				String condition = null;
				if (key.contains("[")) {
					final int index = key.indexOf("[");
					condition = key.substring(index + 1, key.indexOf("]"));
					key = key.substring(0, index);
				}
				
				part = part.substring(part.indexOf("{") + 1);

				final HComponentStyle style = styles.get(key).doPart(part);
				final CSSData cssData = new CSSData(event, condition, style);
				data.put(key, cssData);
			}

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void applyStyle(final HComponent comp) {
		final String[] herits = comp.getStyleKey().split("\\.");
		for (final String herit : herits) {
			final CSSData cssData = data.get(herit);

			if (cssData != null && cssData.match(comp)) {
				cssData.getStyle().set(comp);
				System.out.println(cssData.getStyle().getClass());
			}
		}
	}
}
