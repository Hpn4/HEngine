package hengine.engine.graph.postProcessing;

import org.joml.Vector2f;

import hengine.engine.graph.ShaderProgram;
import hengine.engine.hlib.component.HWindow;

public class TmpTestStep extends PostProcessingStep {

	public TmpTestStep(HWindow window) throws Exception {
		super(window);

		shader = new ShaderProgram("test.vert", null, "test.frag");
		
		shader.createUniform("texture_sampler");
		shader.createUniform("screenSize");
	}

	public void render() {
		super.startRender();
		shader.bind();

		shader.setInt("texture_sampler", 0);
		shader.setVector2f("screenSize", new Vector2f(HWindow.frameWidth, HWindow.frameHeight));

		super.render();

		shader.unbind();
		
		super.endRender();
	}
}
