package hengine.engine.graph.renderer.postProcessing;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import hengine.engine.graph.RendererOption;
import hengine.engine.graph.renderer.postProcessing.step.BloomStep;
import hengine.engine.graph.renderer.postProcessing.step.PostProcessingStep;
import hengine.engine.graph.renderer.postProcessing.step.ToFBOStep;
import hengine.engine.graph.renderer.postProcessing.step.ToScreenStep;
import hengine.engine.hlib.component.HWindow;

public class PostProcessingRenderer {

	private final ToFBOStep toFBO;
	
	private final ToScreenStep toScreen;
	
	private final BloomStep bloom;
	
	public PostProcessingRenderer(final HWindow window) throws Exception {
		// Contain the final lightning color and the emssive
		toFBO = new ToFBOStep(window);
		
		// The bloom step
		bloom = new BloomStep(window);
		
		// Mix the final lightning color from ToFBOStep and the emissive color of GaussianBlurStep
		toScreen = new ToScreenStep();

		PostProcessingStep.initMesh();
	}

	public ToFBOStep getFirst() {
		return toFBO;
	}

	public void render(final HWindow window, final RendererOption option) {
		
		// Down scale the emissive texture from ToFBOStep
		glViewport(0, 0, window.getWidth(), window.getHeight());
		bloom.render(toFBO);
		
		// Mix the four texture
		glViewport(0, 0, window.getWidth(), window.getHeight());
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, toFBO.getBuffer().getSceneTexture());
		bloom.bindBlurTexture(1); // 0 : the scene texture
		toScreen.render(option.applyExposure);
	}

	public void cleanUp() {
		toFBO.cleanup();
		bloom.cleanup();
		toScreen.cleanup();

		PostProcessingStep.cleanUpMesh();
	}
}
