package hengine.engine.graph.shadow;

import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColorMask;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import hengine.engine.graph.Camera;
import hengine.engine.graph.Renderer;
import hengine.engine.graph.ShaderProgram;
import hengine.engine.graph.anime.AnimGameItem;
import hengine.engine.graph.anime.AnimatedFrame;
import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.AnimatedMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.item.GameItem;
import hengine.engine.world.Scene;

public class ShadowRenderer {

	public static final int NUM_CASCADES = 3;

	public static final float[] CASCADE_SPLITS = new float[] { HWindow.Z_FAR / 10, HWindow.Z_FAR / 5, HWindow.Z_FAR };

	private ShadowBuffer shadowBuffer;

	private ShaderProgram depthShaderProgram;

	private ShaderProgram animDepthShaderProgram;

	private final List<ShadowCascade> shadowCascades;

	private final List<GameItem> filteredItems;

	public ShadowRenderer() {
		filteredItems = new ArrayList<>();
		shadowCascades = new ArrayList<>();
	}

	public void init() throws Exception {
		shadowBuffer = new ShadowBuffer();

		setupDepthShader();
		setupAnimDepthShader();

		float zNear = HWindow.Z_NEAR;
		for (int i = 0; i < NUM_CASCADES; i++) {
			final ShadowCascade shadowCascade = new ShadowCascade(zNear, CASCADE_SPLITS[i]);
			shadowCascades.add(shadowCascade);
			zNear = CASCADE_SPLITS[i];
		}
	}

	private void setupDepthShader() throws Exception {
		depthShaderProgram = new ShaderProgram("depth");

		depthShaderProgram.createUniform("isInstanced");
		depthShaderProgram.createUniform("isTransparent");
		depthShaderProgram.createUniform("modelNonInstancedMatrix");
		depthShaderProgram.createUniform("orthoProjLightViewMatrix");
	}

	private void setupAnimDepthShader() throws Exception {
		animDepthShaderProgram = new ShaderProgram("depth_animated.vert", null, "depth.frag");

		animDepthShaderProgram.createUniform("modelMatrix");
		animDepthShaderProgram.createUniform("isTransparent");
		animDepthShaderProgram.createUniform("jointsMatrix");
		animDepthShaderProgram.createUniform("orthoProjLightViewMatrix");
	}

	private void update(final HWindow window, final Matrix4f viewMatrix, final Scene scene) {
		final DirectionalLight directionalLight = scene.getSceneLight().getDirectionalLight();
		for (int i = 0; i < NUM_CASCADES; i++)
			shadowCascades.get(i).update(window, viewMatrix, directionalLight);
	}

	public void render(final HWindow window, final Scene scene, final Camera camera, final Renderer renderer) {
		update(window, camera.getViewMatrix(), scene);

		final ShadowTexture shadowTexture = shadowBuffer.getDepthMapTexture();

		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, shadowBuffer.getDepthMapFBO());
		glViewport(0, 0, shadowTexture.getWidth(), shadowTexture.getHeight());
		glColorMask(false, false, false, false);

		// Render scene for each cascade map
		for (int i = 0; i < NUM_CASCADES; i++) {
			final ShadowCascade shadowCascade = shadowCascades.get(i);
			final Matrix4f orthoProjLightViewMatrix = shadowCascade.getOrthoProjLightViewMatrix();

			depthShaderProgram.bind();

			depthShaderProgram.setMatrix4f("orthoProjLightViewMatrix", orthoProjLightViewMatrix);
			depthShaderProgram.setBoolean("isTransparent", false);

			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowTexture.getIds()[i], 0);
			glClear(GL_DEPTH_BUFFER_BIT);

			renderNonInstancedMeshes(scene);

			renderInstancedMeshes(scene);

			// Unbind
			depthShaderProgram.unbind();

			animDepthShaderProgram.bind();

			animDepthShaderProgram.setMatrix4f("orthoProjLightViewMatrix", orthoProjLightViewMatrix);
			animDepthShaderProgram.setBoolean("isTransparent", false);

			renderAnimatedMeshes(scene);

			// Unbind
			animDepthShaderProgram.unbind();
		}

		glColorMask(true, true, true, true);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}

	private void renderNonInstancedMeshes(final Scene scene) {
		depthShaderProgram.setBoolean("isInstanced", false);

		final Map<AbstractMesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			bindTextures(GL_TEXTURE0);

			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				depthShaderProgram.setMatrix4f("modelNonInstancedMatrix", gameItem.getModelMatrix());
			}, false);
		}
	}

	private void renderInstancedMeshes(final Scene scene) {
		depthShaderProgram.setBoolean("isInstanced", true);

		final Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
		for (final InstancedMesh mesh : mapMeshes.keySet()) {
			filteredItems.clear();

			for (final GameItem gameItem : mapMeshes.get(mesh))
				if (gameItem.isInsideFrustum())
					filteredItems.add(gameItem);

			bindTextures(GL_TEXTURE0);

			mesh.renderListInstanced(filteredItems, false);
		}
	}

	private void renderAnimatedMeshes(final Scene scene) {
		final Map<AnimatedMesh, List<GameItem>> mapMeshes = scene.getAnimatedMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			bindTextures(GL_TEXTURE0);

			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				animDepthShaderProgram.setMatrix4f("modelMatrix", gameItem.getModelMatrix());
				final AnimatedFrame frame = ((AnimGameItem) gameItem).getCurrentAnimation().getCurrentFrame();
				animDepthShaderProgram.setMatrices4f("jointsMatrix", frame.getJointMatrices());
			}, false);
		}
	}

	public List<GameItem> getFilteredItems() {
		return filteredItems;
	}

	public List<ShadowCascade> getShadowCascades() {
		return shadowCascades;
	}

	public void bindTextures(int start) {
		shadowBuffer.bindTextures(start);
	}

	public void cleanup() {
		if (shadowBuffer != null)
			shadowBuffer.cleanup();

		if (depthShaderProgram != null)
			depthShaderProgram.cleanup();

		if (animDepthShaderProgram != null)
			animDepthShaderProgram.cleanup();
	}
}
