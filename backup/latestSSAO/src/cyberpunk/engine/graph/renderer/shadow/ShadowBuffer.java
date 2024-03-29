package hengine.engine.graph.renderer.shadow;

import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

public class ShadowBuffer {

	public static final int SHADOW_MAP_SIZE = 2048;

	private final int depthMapFBO;

	private final ShadowTexture depthMap;

	public ShadowBuffer() throws Exception {
		// Create a FBO to render the depth map
		depthMapFBO = glGenFramebuffers();

		// Create the depth map textures
		depthMap = new ShadowTexture(ShadowRenderer.NUM_CASCADES, SHADOW_MAP_SIZE);

		// Attach the the depth map texture to the FBO
		glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap.getIds()[0], 0);

		// Set only depth
		glDrawBuffer(GL_NONE);
		glReadBuffer(GL_NONE);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new Exception("Could not create FrameBuffer");

		// Unbind
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public ShadowTexture getDepthMapTexture() {
		return depthMap;
	}

	public int getDepthMapFBO() {
		return depthMapFBO;
	}

	public void bindTextures(final int start) {
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
			glActiveTexture(start + i);
			glBindTexture(GL_TEXTURE_2D, depthMap.getIds()[i]);
		}
	}

	public void cleanup() {
		glDeleteFramebuffers(depthMapFBO);
		depthMap.cleanup();
	}

}