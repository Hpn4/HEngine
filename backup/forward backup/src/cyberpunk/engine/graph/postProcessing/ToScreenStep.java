package hengine.engine.graph.postProcessing;

import org.joml.Vector2f;

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
		toScreenShaderProgram = new ShaderProgram("toScreen.vert", null, "toScreen.frag");

		toScreenShaderProgram.createUniform("texture_sampler");
		toScreenShaderProgram.createUniform("screenSize");
	}

	public void render() {
		toScreenShaderProgram.bind();

		toScreenShaderProgram.setInt("texture_sampler", 0);
		toScreenShaderProgram.setVector2f("screenSize", new Vector2f(HWindow.frameWidth, HWindow.frameHeight));

		PostProcessingStep.quad.render();

		toScreenShaderProgram.unbind();
	}

	public void cleanUp() {
		toScreenShaderProgram.cleanup();
	}
}
