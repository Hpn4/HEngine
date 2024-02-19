package hengine.engine.utils.loader.texture;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import hengine.engine.utils.Utils;

/**
 * Class stockant en memoire l'image préciser dans le constructeur. Cette class
 * contient egalement l'id (le pointeur de l'image), et ses dimensions.
 * 
 * @author Hpn4
 *
 */
public class TextureCube extends AbstractTexture {

	private final int width[];

	private final int height[];

	public TextureCube(final String[] fileNames) throws Exception {
		super();

		final int numTextures = fileNames.length;

		width = new int[numTextures];
		height = new int[numTextures];

		// Load Texture file
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), channels = stack.mallocInt(1);

			bind();

			for (int i = 0; i < numTextures; i++) {
				final ByteBuffer buf = stbi_load(Utils.getPath(fileNames[i]), w, h, channels, 4);

				if (buf == null)
					throw new Exception("Image file [" + fileNames[i] + "] not loaded: " + stbi_failure_reason());

				final int we = w.get(), he = h.get();
				width[i] = we;
				height[i] = he;

				// Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
				glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

				// Upload the texture data
				glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, we, he, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);

				stbi_image_free(buf);
				
				w.position(0);
				h.position(0);
				channels.position(0);
			}
			
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		}
	}

	public int[] getWidth() {
		return width;
	}

	public int[] getHeight() {
		return height;
	}

	public void bind() {
		glBindTexture(GL_TEXTURE_CUBE_MAP, id);
	}
}
