package hengine.engine.graph.renderer.postProcessing.step;

import static org.lwjgl.opengl.GL11.glViewport;

import hengine.engine.graph.ShaderProgram;

public class DownScaleStep extends PostProcessingStep {

	public DownScaleStep(final int width, final int height, final ShaderProgram shader) throws Exception {
		super(width, height);
		
		this.shader = shader;
	}

	public void render() {
		startRender();

		shader.bind();

		shader.setInt("texture_sampler", 0);

		glViewport(0, 0, buffer.getWidth(), buffer.getHeight());

		super.render();

		shader.unbind();

		endRender();
	}

}