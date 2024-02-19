package hengine.engine.utils.loader.texture;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGetTexImage;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import hengine.engine.utils.Utils;

/**
 * Class stockant en memoire l'image préciser dans le constructeur. Cette class
 * contient egalement l'id (le pointeur de l'image), et ses dimensions.
 * 
 * @author Hpn4
 *
 */
public class Texture extends AbstractTexture {

	private final int width;

	private final int height;

	private int numRows;

	private int numCols;

	protected Texture(final String fileName) throws Exception {
		super();
		ByteBuffer buf;

		// Load Texture file
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer w = stack.mallocInt(1);
			final IntBuffer h = stack.mallocInt(1);
			final IntBuffer channels = stack.mallocInt(1);

			buf = stbi_load(Utils.getPath(fileName), w, h, channels, 4);

			if (buf == null)
				throw new Exception("Image file [" + fileName + "] not loaded: " + stbi_failure_reason());

			width = w.get();
			height = h.get();
		}

		createTexture(buf);

		stbi_image_free(buf);
	}

	public Texture(final String fileName, final int numCols, final int numRows) throws Exception {
		this(fileName);
		this.numCols = numCols;
		this.numRows = numRows;
	}

	private void createTexture(final ByteBuffer buf) {
		// Bind the texture
		bind();

		// Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		// Upload the texture data
		// Linear space
		// glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
		
		// srgb ( gamma correction )
		glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB_ALPHA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf); 
		//glTexImage2D(GL_TEXTURE_2D, 0, GL_COMPRESSED_SRGB_ALPHA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf); 
		
		// Generate Mip Map
		glGenerateMipmap(GL_TEXTURE_2D);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	}

	/**
	 * NE PAS OUBLIER DE LIBERER LE BUFFER MemoryUtil.memFree(buff)
	 * 
	 * @return
	 */
	public ByteBuffer getImage() {
		final ByteBuffer px = MemoryUtil.memAlloc(width * height * 4);

		bind();
		glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, px);
		return px;
	}

	public int getNumCols() {
		return numCols;
	}

	public int getNumRows() {
		return numRows;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void bind() {
		glBindTexture(GL_TEXTURE_2D, id);
	}
}
