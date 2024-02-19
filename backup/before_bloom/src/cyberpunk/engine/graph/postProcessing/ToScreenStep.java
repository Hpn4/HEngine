package hengine.engine.graph.postProcessing;

import hengine.engine.graph.ShaderProgram;
import hengine.engine.hlib.component.HWindow;

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
		toScreenShaderProgram = new ShaderProgram("toScreen");

		toScreenShaderProgram.createUniform("sceneTex");
		toScreenShaderProgram.createUniform("bloomBlurTex");
		toScreenShaderProgram.createUniform("exposure");
		toScreenShaderProgram.createUniform("screenSize");
	}

	public void render() {
		toScreenShaderProgram.bind();

		toScreenShaderProgram.setInt("sceneTex", 0);
		toScreenShaderProgram.setInt("bloomBlurTex", 1);
		toScreenShaderProgram.setFloat("exposure", 1f);
		toScreenShaderProgram.setVector2f("screenSize", HWindow.frameWidth, HWindow.frameHeight);

		PostProcessingStep.quad.render();

		toScreenShaderProgram.unbind();
	}

	public void cleanUp() {
		toScreenShaderProgram.cleanup();
	}
}
