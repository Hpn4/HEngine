package hengine.engine.graph.renderer.ssao;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import hengine.engine.hlib.component.HWindow;

public class SSAOBuffer {

	protected final static byte SAMPLES = 64;

	private final Vector3f[] samples;

	private final int noiseId;

	private final int ssaoFBO;

	private final int ssaoTex;

	public SSAOBuffer(final HWindow window) {
		samples = new Vector3f[SAMPLES];

		for (int i = 0; i < SAMPLES; i++) {
			// * 2.0 - 1.0 pour convertir les coordonnés en screeen space
			final Vector3f vec = new Vector3f((float) (Math.random() * 2.0 - 1.0), (float) (Math.random() * 2.0 - 1.0),
					(float) Math.random());

			vec.normalize();
			vec.mul((float) Math.random());
			float scale = (float) (i / SAMPLES);

			// Pour que les samples soit plus regroupés au niveau du centre au lieu que ce
			// soit totalement aleatoire
			scale = lerp(0.1f, 1.0f, scale * scale);
			vec.mul(scale);

			samples[i] = vec;
		}

		// On creer la texture de "bruit"
		noiseId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, noiseId);

		try (final MemoryStack stack = MemoryStack.stackPush()) {
			final FloatBuffer buf = stack.mallocFloat(16 * 3);
			for (int i = 0; i < 16; i++) {
				final Vector3f vec = new Vector3f((float) (Math.random() * 2.0 - 1.0),
						(float) (Math.random() * 2.0 - 1.0), 0f);
				vec.normalize();
				vec.get(buf);
			}

			buf.flip();
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 4, 4, 0, GL_RGB, GL_FLOAT, buf);
		}

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		// On repete la texture, car c'est une 4x4 mais on veut quel s'applique sur tout
		// l'ecrans
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		// Le FBO
		ssaoFBO = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, ssaoFBO);

		// La texture d'occlusion ambiante
		ssaoTex = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, ssaoTex);

		// On stock que dans le rouge car ce sont des nuances de gris donc pas besoin de
		// plus de cannaux
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, window.getWidth(), window.getHeight(), 0, GL_RED, GL_FLOAT,
				(FloatBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, ssaoTex, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	private float lerp(final float a, final float b, final float f) {
		return a + f * (b - a);
	}

	public void cleanup() {
		glDeleteFramebuffers(ssaoFBO);
		glDeleteTextures(ssaoTex);
		glDeleteTextures(noiseId);
	}

	public Vector3f[] getSamples() {
		return samples;
	}

	public int getNoiseTextureId() {
		return noiseId;
	}

	public int getAOTextureId() {
		return ssaoTex;
	}

	public int getFBOId() {
		return ssaoFBO;
	}
}
