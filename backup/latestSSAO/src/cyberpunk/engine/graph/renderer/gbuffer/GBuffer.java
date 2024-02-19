package hengine.engine.graph.renderer.gbuffer;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGB10_A2;
import static org.lwjgl.opengl.GL11.GL_RGB8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameterf;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT2;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT3;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;

import hengine.engine.hlib.component.HWindow;

public class GBuffer {

	/**
	 * 4 textures :
	 * 
	 * 0 : Depth texture ( we can reconstruct worldpos)
	 *  1 : Diffuse | Emissive
	 * (diffuse and emissive color are combined) 
	 * 2 : Normals (RGB) and isEmissive
	 * (A) 3 : Shadow (R) and Reflectance (G) and Specular (B)
	 */
	public static final int TOTAL_TEXTURES = 4;

	private final int gBufferId;

	private final int[] textureIds;

	private final int width;

	private final int height;

	public GBuffer(final HWindow window) throws Exception {
		width = window.getWidth();
		height = window.getHeight();

		// Génére le G-Buffer
		gBufferId = glGenFramebuffers();
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gBufferId);

		// Génére les textures
		textureIds = new int[TOTAL_TEXTURES];
		glGenTextures(textureIds);

		// Create textures for depth, diffuse / emissive color, specular color,
		// normal and
		// shadow factor/ reflectance / isEmissive
		for (int i = 0; i < TOTAL_TEXTURES; i++) {
			glBindTexture(GL_TEXTURE_2D, textureIds[i]);

			int attachementType;

			if (i == 0) { // Depth
				attachementType = GL_DEPTH_ATTACHMENT;
				glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT,
						(ByteBuffer) null);
			} else {
				attachementType = GL_COLOR_ATTACHMENT0 + i;
				glTexImage2D(GL_TEXTURE_2D, 0, i == 2 ? GL_RGB10_A2 : GL_RGB8, width, height, 0, GL_RGB, GL_FLOAT,
						(ByteBuffer) null);
			}

			// For sampling
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

			// Attach the the texture to the G-Buffer
			glFramebufferTexture2D(GL_FRAMEBUFFER, attachementType, GL_TEXTURE_2D, textureIds[i], 0);
		}

		try (final MemoryStack stack = MemoryStack.stackPush()) {
			glDrawBuffers(
					stack.ints(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3));
		}

		// On vérifie que tous est bon
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new Exception("Could not create FrameBuffer");

		// Unbind
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getGBufferId() {
		return gBufferId;
	}

	public int[] getTextureIds() {
		return textureIds;
	}

	public int getDepthTexture() {
		return textureIds[0];
	}

	public void cleanup() {
		glDeleteFramebuffers(gBufferId);
		glDeleteTextures(textureIds);
	}
}
