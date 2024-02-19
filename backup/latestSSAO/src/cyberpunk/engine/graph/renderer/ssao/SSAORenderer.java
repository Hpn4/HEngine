package hengine.engine.graph.renderer.ssao;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import hengine.engine.graph.ShaderProgram;
import hengine.engine.graph.renderer.gbuffer.GBuffer;
import hengine.engine.graph.renderer.postProcessing.step.PostProcessingStep;
import hengine.engine.hlib.component.HWindow;

public class SSAORenderer {

	private final SSAOBuffer buffer;

	private final ShaderProgram ssaoShaderProgram;

	public SSAORenderer(final HWindow window) throws Exception {
		buffer = new SSAOBuffer(window);

		ssaoShaderProgram = new ShaderProgram("ssao");

		ssaoShaderProgram.createUniform("depthText", "normalsText", "noiseText");
		ssaoShaderProgram.createUniform("noiseScale");

		ssaoShaderProgram.createUniform("projectionMatrix", "invProjectionMatrix");

		ssaoShaderProgram.bind();
		final Vector3f[] samples = buffer.getSamples();
		for (int i = 0; i < SSAOBuffer.SAMPLES; i++) {
			ssaoShaderProgram.createUniform("samples[" + i + "]");
			ssaoShaderProgram.setVector3fAtIndex("samples", samples[i], i);
		}

		ssaoShaderProgram.setInt("depthText", 0);
		ssaoShaderProgram.setInt("normalsText", 1);
		ssaoShaderProgram.setInt("noiseText", 2);

		// On divise la résolution de l'ecran par la taille de l'image de bruit soit
		// (4x4)
		final Vector2f scale = new Vector2f(window.getWidth(), window.getHeight());
		ssaoShaderProgram.setVector2f("noiseScale", scale.div(new Vector2f(4)));

		ssaoShaderProgram.createUniform("depthText", "normalsText", "noiseText");

		ssaoShaderProgram.bind();

		ssaoShaderProgram.setInt("depthText", 0);
		ssaoShaderProgram.setInt("normalsText", 1);
		ssaoShaderProgram.setInt("noiseText", 2);

		ssaoShaderProgram.unbind();
	}

	public void render(final GBuffer buf, final Matrix4f projectionMatrix, final Matrix4f invProjectionMatrix) {
		glBindFramebuffer(GL_FRAMEBUFFER, buffer.getFBOId());
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		ssaoShaderProgram.bind();

		ssaoShaderProgram.setMatrix4f("projectionMatrix", projectionMatrix);
		ssaoShaderProgram.setMatrix4f("invProjectionMatrix", invProjectionMatrix);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, buf.getDepthTexture());
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, buf.getTextureIds()[2]);
		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, buffer.getNoiseTextureId());

		PostProcessingStep.quad.render();

		ssaoShaderProgram.unbind();

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public SSAOBuffer getBuffer() {
		return buffer;
	}

	public void cleanup() {
		buffer.cleanup();
		ssaoShaderProgram.cleanup();
	}
}
