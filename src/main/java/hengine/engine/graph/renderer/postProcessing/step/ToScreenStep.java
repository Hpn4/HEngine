package hengine.engine.graph.renderer.postProcessing.step;

import hengine.engine.graph.ShaderProgram;

/**
 * 
 * Cette objet envoie la texture du frame buffer précédent et l'affiche
 * direcement a l'ecran. Il effectue des modifications supplémentaires grace aux
 * shaders "toScreen.vert" et "toScreen.frag"
 * 
 * @author Hpn4
 *
 */
public class ToScreenStep {

	private final ShaderProgram toScreenShaderProgram;

	public ToScreenStep() throws Exception {
		toScreenShaderProgram = new ShaderProgram("post/toScreen");

		toScreenShaderProgram.createUniform("sceneTex");

		toScreenShaderProgram.createUniform("firstBloomTex");
		toScreenShaderProgram.createUniform("secondBloomTex");
		toScreenShaderProgram.createUniform("thirdBloomTex");

		toScreenShaderProgram.createUniform("applyExposure");
		toScreenShaderProgram.createUniform("exposure");

		toScreenShaderProgram.bind();

		toScreenShaderProgram.setInt("sceneTex", 0);
		toScreenShaderProgram.setInt("firstBloomTex", 1);
		toScreenShaderProgram.setInt("secondBloomTex", 2);
		toScreenShaderProgram.setInt("thirdBloomTex", 3);

		toScreenShaderProgram.setFloat("exposure", 1f);

		toScreenShaderProgram.unbind();
	}

	public void render(final boolean applyExposure) {
		toScreenShaderProgram.bind();

		toScreenShaderProgram.setBoolean("applyExposure", applyExposure);

		PostProcessingStep.quad.render();

		toScreenShaderProgram.unbind();
	}

	public void cleanup() {
		toScreenShaderProgram.cleanup();
	}
}
