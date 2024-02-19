package hengine.engine.utils.loader.texture;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import hengine.engine.utils.Utils;

public class ModelTextureCombiner {

	private AbstractTexture diffuseMap;

	private AbstractTexture normalMap;

	private boolean hasSpecularMap;

	private boolean hasEmissiveMap;

	private boolean isTransparent;

	public ModelTextureCombiner(final String diffusePath, final String normalPath, final String specularPath,
			final String emissivePath) throws Exception {

		if (isNonNull(diffusePath)) {
			if (isNonNull(normalPath))
				loadTexture(diffusePath, normalPath, specularPath, emissivePath);
			else {
				final ByteBuffer buf;
				int width, height;
				boolean isHDR;

				// Load Diffuse Texture and test the transparency
				try (final MemoryStack stack = MemoryStack.stackPush()) {
					final IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), channels = stack.mallocInt(1);

					buf = stbi_load(Utils.getPath(diffusePath), w, h, channels, 4);

					if (buf == null)
						throw new Exception("Image file [" + diffusePath + "] not loaded: " + stbi_failure_reason());

					isHDR = STBImage.stbi_is_hdr_from_memory(buf);

					testTransparency(buf);
					if (isTransparent)
						System.out.println("diffuse map : " + diffusePath);

					width = w.get();
					height = h.get();
				}

				diffuseMap = new Texture(width, height, isHDR, buf);

				stbi_image_free(buf);
			}
		}
	}

	private void loadTexture(final String diffusePath, final String normalPath, final String specularPath,
			final String emissivePath) throws Exception {
		int width, height;
		ByteBuffer buf;
		boolean isHDR = true;

		// Load Diffuse Texture and test the transparency
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), channels = stack.mallocInt(1);

			buf = stbi_load(Utils.getPath(diffusePath), w, h, channels, 4);

			if (buf == null)
				throw new Exception("Image file [" + diffusePath + "] not loaded: " + stbi_failure_reason());

			isHDR = STBImage.stbi_is_hdr_from_memory(buf);

			testTransparency(buf);

			width = w.get();
			height = h.get();
		}

		// On charge la texture de normal
		final ByteBuffer normalTex = readImage(normalPath);

		buf.position(0);
		normalTex.position(0);

		// Si le modele possede une texture emissive
		if (isNonNull(emissivePath)) {
			hasEmissiveMap = true;
			final ByteBuffer emissiveTex = readImage(emissivePath);

			// Si il possede une texture emissive et specular
			if (isNonNull(specularPath)) {
				hasSpecularMap = true;
				final ByteBuffer specularTex = readImage(specularPath);

				while (normalTex.hasRemaining()) {
					final byte specularFactor = specularTex.get();
					// On saute les trois autres canneaux on a besoin que d'un
					specularTex.position(specularTex.position() + 3);

					// Les trois premier canneaux de la normal (RGB)
					normalTex.position(normalTex.position() + 3);

					final byte x = emissiveTex.get(), y = emissiveTex.get(), z = emissiveTex.get();
					emissiveTex.position(emissiveTex.position() + 1); // Alpha channel

					// Text si le pixel est emissif
					final boolean isEmissive = x != 0 && y != 0 && z != 0;

					// Text si le pixel est emissif ou non et définit l'alpha de la normal
					if (isEmissive) {
						buf.put(x);
						buf.put(y);
						buf.put(z);
						// On saute l'alpha
						buf.position(buf.position() + 1);
						normalTex.put((byte) 0); // a = 0 le pixel est emissif
					} else {
						normalTex.put(specularFactor); // a = specularFactor
						buf.position(buf.position() + 4);
					}
				}

				// On libere les ressources
				stbi_image_free(specularTex);
			} else {

				while (normalTex.hasRemaining()) {
					// On saute les trois premiers byte de la normal (RGB)
					normalTex.position(normalTex.position() + 3);

					final byte x = emissiveTex.get(), y = emissiveTex.get(), z = emissiveTex.get();
					// On saute le byte d'alpha
					emissiveTex.position(emissiveTex.position() + 1);

					// Text si le pixel est emissif
					final boolean notEmissive = (x & 0xFF) == 0 && (y & 0xFF) == 0 && (z & 0xFF) == 0;

					if (notEmissive) {
						// On saute l'alpha
						buf.position(buf.position() + 4);
						normalTex.put((byte) 0);
					} else {
						normalTex.put((byte) 255);
						buf.put(x);
						buf.put(y);
						buf.put(z);
						buf.position(buf.position() + 1);
					}
				}
			}

			// On libere les ressources
			emissiveTex.position(0);
			stbi_image_free(emissiveTex);

		}
		// Si le modele contient une texture specular
		else if (isNonNull(specularPath)) {
			hasSpecularMap = true;
			final ByteBuffer specularTex = readImage(specularPath);

			while (normalTex.hasRemaining()) {
				final byte specularFactor = specularTex.get();
				// On saute les trois autres canneaux on a besoin que d'un
				specularTex.position(specularTex.position() + 3);

				// Les trois premier canneaux de la normal (RGB)
				normalTex.position(normalTex.position() + 3);

				normalTex.put(specularFactor); // L'alpha de la normal = specularFactor
			}

			// On libere les ressources
			specularTex.position(0);
			stbi_image_free(specularTex);
		}

		// On reset la position du buffer a 0
		normalTex.position(0);

		// On creer la texture des normals
		normalMap = new Texture(width, height, isHDR, normalTex);

		buf.flip();
		buf.position(0);
		diffuseMap = new Texture(width, height, isHDR, buf);

		// On libere les ressources
		stbi_image_free(normalTex);
		stbi_image_free(buf);
	}

	private boolean isNonNull(final String str) {
		return str != null && str.length() > 0;
	}

	private void testTransparency(final ByteBuffer buf) {
		isTransparent = false;
		int test = 0;
		int min = 256, max = 0;
		while (buf.hasRemaining()) {
			buf.get(); // red
			buf.get(); // green
			buf.get(); // blue
			final int a = buf.get() & 0xFF;
			if(a < 255) {
				min = a < min ? a : min;
				max = a > max ? a : max;
				test++;
			}
			if (a == 0) { // alpha
				//System.err.println("transparent");
				isTransparent = true;
				//break;
			}
		}
		System.err.println(test + " transparent : " + isTransparent);
		System.err.println("min : " + min + " max : " + max);
		buf.position(0);
	}

	private ByteBuffer readImage(final String filePath) throws Exception {
		ByteBuffer buf;

		try (final MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), channels = stack.mallocInt(1);

			buf = stbi_load(Utils.getPath(filePath), w, h, channels, 4);

			if (buf == null)
				throw new Exception("Image file [" + filePath + "] not loaded: " + stbi_failure_reason());
		}

		return buf;
	}

	public AbstractTexture getDiffuseMap() {
		return diffuseMap;
	}

	public AbstractTexture getNormalMap() {
		return normalMap;
	}

	public boolean hasEmissiveMap() {
		return hasEmissiveMap;
	}

	public boolean hasSpecularMap() {
		return hasSpecularMap;
	}

	public boolean isTransparent() {
		return isTransparent;
	}

	public void cleanUp() {
		if (diffuseMap != null)
			diffuseMap.cleanup();

		if (normalMap != null)
			normalMap.cleanup();
	}
}
