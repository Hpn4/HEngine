package hengine.engine.graph.postProcessing;

import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import hengine.engine.graph.ShaderProgram;
import hengine.engine.hlib.component.HWindow;

public class GaussianBlurStep {

	private final PostProcessingBuffer horizontalBlur;

	private final PostProcessingBuffer verticalBlur;

	private ShaderProgram gaussianBlurShaderProgram;

	public GaussianBlurStep(HWindow window) throws Exception {
		horizontalBlur = new PostProcessingBuffer(window);
		verticalBlur = new PostProcessingBuffer(window);

		setupGaussianBlurShader();
	}

	private void setupGaussianBlurShader() throws Exception {
		gaussianBlurShaderProgram = new ShaderProgram("gaussian_blur");

		gaussianBlurShaderProgram.createUniform("sceneTex");

		gaussianBlurShaderProgram.createUniform("horizontal");

		gaussianBlurShaderProgram.createUniform("screenSize");
	}

	public void bindTexture() {
		verticalBlur.bindTextures(1);
	}

	public void render(final HWindow window) {
		boolean horizontal = true, firstTime = true;
		int amount = 6;

		gaussianBlurShaderProgram.bind();

		gaussianBlurShaderProgram.setInt("sceneTex", 0);
		final int width = window.getWidth(), height = window.getHeight();
		gaussianBlurShaderProgram.setVector2f("screenSize", width, height);
		
		for (int i = 0; i < amount; i++) {

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
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public void cleanUp() {
		horizontalBlur.cleanup();
		verticalBlur.cleanup();

		gaussianBlurShaderProgram.cleanup();
	}
}
