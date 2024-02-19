package hengine.engine.graph.shadow;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;

import java.nio.ByteBuffer;

public class ShadowTexture {

	private final int[] ids;

	private final int[] width;

	private final int[] height;

	public ShadowTexture(final int numTextures, final int size, final int pixelFormat) {
		// Genere les id des textures
		ids = new int[numTextures];
		glGenTextures(ids);
		
		width = new int[numTextures];
		height = new int[numTextures];

		// Genere toute les texture et leur propriete
		for (int i = 0; i < numTextures; i++) {
			glBindTexture(GL_TEXTURE_2D, ids[i]);

			// On baisse volontairement la qualité des textures, car plus on est loin moins
			// on voit donc plus la texture d'ombre est eloigné du joueur moins elle a
			// besoin de qualité, a l'oeil nue la différence ne se voit même pas et les
			// perfs augmente enormemement ^^
			final int w = size, h = size;
			glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, w, h, 0, pixelFormat,
					GL_FLOAT, (ByteBuffer) null);
			
			width[i] = w;
			height[i] = h;
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		}
	}

	public int[] getWidth() {
		return width;
	}

	public int[] getHeight() {
		return height;
	}

	public int[] getIds() {
		return ids;
	}

	/**
	 * Delete all the textures associated to this object
	 */
	public void cleanup() {
		glDeleteTextures(ids);
	}
}
