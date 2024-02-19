package hengine.engine.graph.postProcessing;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import hengine.engine.hlib.component.HWindow;

public class ToFBOStep {

	private final PostProcessingBuffer buffer;

	public ToFBOStep(final HWindow window) throws Exception {
		buffer = new PostProcessingBuffer(window);
	}

	public void startRender() {
		buffer.bindFrameBuffer();
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

	public void endRender() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public PostProcessingBuffer getBuffer() {
		return buffer;
	}

	public void cleanUp() {
		buffer.cleanup();
	}
}
