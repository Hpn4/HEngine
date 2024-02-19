package hengine.engine.graph.renderer.postProcessing.step;

import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import hengine.engine.graph.ShaderProgram;
import hengine.engine.graph.renderer.postProcessing.PostProcessingBuffer;

public class GaussianBlurStep {

	private final PostProcessingBuffer horizontalBlur;

	private final PostProcessingBuffer verticalBlur;
	
	private final int step;

	private final ShaderProgram gaussianBlurShaderProgram;

	public GaussianBlurStep(final int width, final int height, final int step, final ShaderProgram shader) throws Exception {
		this.step = step;
		gaussianBlurShaderProgram = shader;
		horizontalBlur = new PostProcessingBuffer(width, height);
		verticalBlur = new PostProcessingBuffer(width, height);
	}

	public void bindTexture(final int id) {
		verticalBlur.bindTextures(id);
	}
	
	public int getTextureId() {
		return verticalBlur.getId();
	}

	public void render() {
		boolean horizontal = true, firstTime = true;

		gaussianBlurShaderProgram.bind();

		gaussianBlurShaderProgram.setInt("tex", 0);
		final int width = horizontalBlur.getWidth(), height = horizontalBlur.getHeight();
		
		gaussianBlurShaderProgram.setVector2f("texOff", 1.0f / width, 1.0f / height);
		
		for (int i = 0; i < step; i++) {

			if (firstTime) {
				horizontalBlur.bindFrameBuffer();
			} else if (horizontal) {
				horizontalBlur.bindFrameBuffer();

				verticalBlur.bindTextures();
			} else {
				verticalBlur.bindFrameBuffer();

				horizontalBlur.bindTextures();
			}

			gaussianBlurShaderProgram.setBoolean("horizontal", horizontal);

			PostProcessingStep.quad.render();

			horizontal = !horizontal;
			if (firstTime)
				firstTime = false;
		}
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}

	public void cleanup() {
		horizontalBlur.cleanup();
		verticalBlur.cleanup();

		gaussianBlurShaderProgram.cleanup();
	}
}
