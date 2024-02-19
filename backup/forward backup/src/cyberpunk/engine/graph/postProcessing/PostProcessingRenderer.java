package hengine.engine.graph.postProcessing;

import static org.lwjgl.opengl.GL11.GL_DEPTH;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.ArrayList;
import java.util.List;

import hengine.engine.hlib.component.HWindow;

public class PostProcessingRenderer {

	private final List<PostProcessingStep> steps;

	private final ToFBOStep toFBO;

	private final ToScreenStep toScreen;

	public PostProcessingRenderer(final HWindow window) throws Exception {
		steps = new ArrayList<>();
		toScreen = new ToScreenStep();
		toFBO = new ToFBOStep(window);

		//steps.add(new TmpTestStep(window));

		PostProcessingStep.initMesh();
		
	}

	public ToFBOStep getFirst() {
		return toFBO;
	}

	public void render() {
		glDisable(GL_DEPTH);

		toFBO.getBuffer().bindTextures();

		if (!steps.isEmpty())
			for (final PostProcessingStep step : steps) {
				step.render();
				step.getBuffer().bindTextures();
			}

		toScreen.render();

		glEnable(GL_DEPTH);
	}

	public void cleanUp() {
		for (final PostProcessingStep step : steps)
			step.cleanUp();

		toFBO.cleanUp();
		toScreen.cleanUp();

		PostProcessingStep.cleanUpMesh();
	}
}
