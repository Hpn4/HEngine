package hengine.engine.graph.renderer.postProcessing.step;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import hengine.engine.graph.ShaderProgram;
import hengine.engine.hlib.component.HWindow;

public class BloomStep {

	private ShaderProgram gaussianBlurShaderProgram;

	private ShaderProgram downScaleShaderProgram;

	private final DownScaleStep halfResolution;

	private final GaussianBlurStep firstBlur;

	private final DownScaleStep quarterResolution;

	private final GaussianBlurStep secondBlur;

	private final DownScaleStep heightResolution;

	private final GaussianBlurStep thirdBlur;

	public BloomStep(final HWindow window) throws Exception {
		setupGaussianBlurShader();
		setupDownScaleShader();

		int width = window.getWidth(), height = window.getHeight();

		// Premiere passe a 1/2 reso
		width /= 2;
		height /= 2;
		halfResolution = new DownScaleStep(width, height, downScaleShaderProgram);
		firstBlur = new GaussianBlurStep(width, height, 2, gaussianBlurShaderProgram);

		// Deuxieme passe, 1/4 reso
		width /= 2;
		height /= 2;
		quarterResolution = new DownScaleStep(width, height, downScaleShaderProgram);
		secondBlur = new GaussianBlurStep(width, height, 2, gaussianBlurShaderProgram);

		// Troisieme passe, 1/8 reso
		width /= 2;
		height /= 2;
		heightResolution = new DownScaleStep(width, height, downScaleShaderProgram);
		thirdBlur = new GaussianBlurStep(width, height, 2, gaussianBlurShaderProgram);
	}

	private void setupGaussianBlurShader() throws Exception {
		gaussianBlurShaderProgram = new ShaderProgram("effect/gaussian_blur");

		gaussianBlurShaderProgram.createUniform("tex");

		gaussianBlurShaderProgram.createUniform("horizontal");

		gaussianBlurShaderProgram.createUniform("texOff");
	}

	private void setupDownScaleShader() throws Exception {
		downScaleShaderProgram = new ShaderProgram("post/downscale");

		downScaleShaderProgram.createUniform("texture_sampler");
	}

	public void render(final ToFBOStep toFBO) {
		// Down scale to half resolution
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, toFBO.getBuffer().getEmissiveTexture());
		halfResolution.render();

		// First blur
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, halfResolution.getBuffer().getId());
		firstBlur.render();

		// Down scale to quarter resolution
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, halfResolution.getBuffer().getId());
		quarterResolution.render();

		// Second blur
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, quarterResolution.getBuffer().getId());
		secondBlur.render();

		// Down scale to 1/8 resolution
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, quarterResolution.getBuffer().getId());
		heightResolution.render();
		
		// Third blur
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, heightResolution.getBuffer().getId());
		thirdBlur.render();
	}

	public void bindBlurTexture(final int id) {
		firstBlur.bindTexture(id);
		secondBlur.bindTexture(id + 1);
		thirdBlur.bindTexture(id + 2);
	}

	public void cleanup() {
		gaussianBlurShaderProgram.cleanup();
		downScaleShaderProgram.cleanup();

		halfResolution.cleanup();
		firstBlur.cleanup();
		quarterResolution.cleanup();
		secondBlur.cleanup();
		heightResolution.cleanup();
		thirdBlur.cleanup();
	}
}
