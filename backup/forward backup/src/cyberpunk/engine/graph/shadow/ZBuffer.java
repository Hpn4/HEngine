package hengine.engine.graph.shadow;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
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

import hengine.engine.hlib.component.HWindow;

public class ZBuffer {

	private final int depthMapFBO;

	private final ZTexture depthMap;

	public ZBuffer(final HWindow window) throws Exception {
		// Create a FBO to render the depth map
		depthMapFBO = glGenFramebuffers();

		// Create the depth map textures
		depthMap = new ZTexture(window.getWidth() / 2, window.getHeight() / 2, GL_DEPTH_COMPONENT);

		// Attach the the depth map texture to the FBO
		glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap.getId(), 0);

		// Set only depth
		glDrawBuffer(GL_NONE);
		glReadBuffer(GL_NONE);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			throw new Exception("Could not create FrameBuffer");
		}

		// Unbind
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public ZTexture getDepthMapTexture() {
		return depthMap;
	}

	public int getDepthMapFBO() {
		return depthMapFBO;
	}

	public void bindTextures(int start) {
		glActiveTexture(start);
		glBindTexture(GL_TEXTURE_2D, depthMap.getId());
	}

	public void cleanup() {
		glDeleteFramebuffers(depthMapFBO);
		depthMap.cleanup();
	}

}
