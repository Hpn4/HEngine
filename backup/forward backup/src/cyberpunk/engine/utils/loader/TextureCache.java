package hengine.engine.utils.loader;

import java.util.HashMap;
import java.util.Map;

import hengine.engine.graph.AbstractTexture;
import hengine.engine.graph.Texture;

public class TextureCache {

	private static final Map<String, AbstractTexture> texturesMap;

	static {
		texturesMap = new HashMap<>();
	}

	public static AbstractTexture getTexture(final String path) throws Exception {
		AbstractTexture texture = texturesMap.get(path);
		if (texture == null) {
			System.out.println("New texture : " + path);
			texture = new Texture(path);
			texturesMap.put(path, texture);
		}

		return texture;
	}

	public static boolean haveTexture(final String path) {
		return texturesMap.containsKey(path);
	}

	public static AbstractTexture putTexture(final String key, final AbstractTexture value) {
		return texturesMap.put(key, value);
	}

	public static void cleanUp() {
		for (final AbstractTexture texture : texturesMap.values())
			texture.cleanup();
	}
}
