package hengine.engine.graph.postProcessing;

import hengine.engine.hlib.component.HWindow;

public class PostProcessingRenderer {

	private final ToFBOStep toFBO;

	private final ToScreenStep toScreen;
	
	private final GaussianBlurStep gaussianBlurStep;

	public PostProcessingRenderer(final HWindow window) throws Exception {
		toScreen = new ToScreenStep();
		toFBO = new ToFBOStep(window);
		gaussianBlurStep = new GaussianBlurStep(window);

		PostProcessingStep.initMesh();
	}

	public ToFBOStep getFirst() {
		return toFBO;
	}

	public void render(final HWindow window) {
		// Apply gaussian blur
		//gaussianBlurStep.render(window);
		//gaussianBlurStep.bindTexture();

		toFBO.getBuffer().bindTextures();
		toScreen.render();
	}

	public void cleanUp() {
		toFBO.cleanUp();
		toScreen.cleanUp();

		PostProcessingStep.cleanUpMesh();
	}
}
