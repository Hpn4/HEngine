package hengine.engine.utils.loader.texture;

import java.util.HashMap;
import java.util.Map;

public class TextureCache {

	private static final Map<String, AbstractTexture> texturesMap;

	private static final Map<String, ModelTextureCombiner> modelTexturesMap;

	static {
		texturesMap = new HashMap<>();
		modelTexturesMap = new HashMap<>();
	}

	public static AbstractTexture getTexture(final String path) throws Exception {
		AbstractTexture texture = texturesMap.get(path);

		if (texture == null) {
			texture = new Texture(path);
			texturesMap.put(path, texture);
		}

		return texture;
	}
	
	public static AbstractTexture getTexture(final String path, final int cols, final int rows) throws Exception {
		AbstractTexture texture = texturesMap.get(path);

		if (texture == null) {
			texture = new Texture(path, cols, rows);
			texturesMap.put(path, texture);
		}

		return texture;
	}

	public static ModelTextureCombiner getModelTextureCombiner(final String diffusePath, final String normalPath,
			final String specularPath, final String emissivePath, final String textureDir) throws Exception {
		ModelTextureCombiner mtc = modelTexturesMap.get(textureDir);

		if (modelTexturesMap.get(textureDir) == null) {
			mtc = new ModelTextureCombiner(diffusePath, normalPath, specularPath, emissivePath);
			modelTexturesMap.put(textureDir, mtc);
		}

		return mtc;
	}

	public static boolean haveTexture(final String path) {
		return texturesMap.containsKey(path);
	}

	public static AbstractTexture putTexture(final String key, final AbstractTexture value) {
		return texturesMap.put(key, value);
	}

	public static ModelTextureCombiner putModelTexture(final String key, final ModelTextureCombiner value) {
		return modelTexturesMap.put(key, value);
	}

	public static void cleanup() {
		for (final AbstractTexture texture : texturesMap.values())
			texture.cleanup();

		for (final ModelTextureCombiner mtc : modelTexturesMap.values())
			mtc.cleanUp();
	}
}
