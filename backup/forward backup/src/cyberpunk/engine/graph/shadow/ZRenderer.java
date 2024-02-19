package hengine.engine.graph.shadow;

import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import hengine.engine.graph.ShaderProgram;
import hengine.engine.graph.anime.AnimGameItem;
import hengine.engine.graph.anime.AnimatedFrame;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.AnimatedMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.item.GameItem;
import hengine.engine.world.Scene;

public class ZRenderer {

	private ShaderProgram depthShaderProgram;

	private ShaderProgram animDepthShaderProgram;

	private ZBuffer zBuffer;

	private final List<GameItem> filteredItems;

	public ZRenderer() {
		filteredItems = new ArrayList<>();
	}

	public void init(final HWindow window) throws Exception {
		zBuffer = new ZBuffer(window);

		setupDepthShader();
	}

	public void bindTextures(int start) {
		zBuffer.bindTextures(start);
	}

	private void setupDepthShader() throws Exception {
		depthShaderProgram = new ShaderProgram("depth");

		depthShaderProgram.createUniform("isInstanced");
		depthShaderProgram.createUniform("modelNonInstancedMatrix");
		depthShaderProgram.createUniform("lightViewMatrix");
		depthShaderProgram.createUniform("orthoProjectionMatrix");

		// Shader pour modele animé
		animDepthShaderProgram = new ShaderProgram("depth_animated.vert", null, "depth.frag");

		animDepthShaderProgram.createUniform("modelMatrix");
		animDepthShaderProgram.createUniform("lightViewMatrix");
		animDepthShaderProgram.createUniform("jointsMatrix");
		animDepthShaderProgram.createUniform("orthoProjectionMatrix");
	}

	public void render(final Matrix4f projectionMatrix, final Matrix4f viewMatrix, final Scene scene) {
		glBindFramebuffer(GL_FRAMEBUFFER, zBuffer.getDepthMapFBO());
		glClear(GL_DEPTH_BUFFER_BIT);

		final ZTexture zTexture = zBuffer.getDepthMapTexture();

		// Met a jour le viewport en fonction de la taile de chacune des tectures
		glViewport(0, 0, zTexture.getWidth(), zTexture.getHeight());

		depthShaderProgram.bind();

		depthShaderProgram.setMatrix4f("orthoProjectionMatrix", projectionMatrix);
		depthShaderProgram.setMatrix4f("lightViewMatrix", viewMatrix);

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, zTexture.getId(), 0);
		glClear(GL_DEPTH_BUFFER_BIT);

		renderNonInstancedMeshes(scene);

		renderInstancedMeshes(scene);

		// Unbind
		depthShaderProgram.unbind();

		animDepthShaderProgram.bind();

		animDepthShaderProgram.setMatrix4f("orthoProjectionMatrix", projectionMatrix);
		animDepthShaderProgram.setMatrix4f("lightViewMatrix", viewMatrix);

		renderAnimatedMeshes(scene);

		// Unbind
		animDepthShaderProgram.unbind();

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	private void renderAnimatedMeshes(final Scene scene) {
		final Map<AnimatedMesh, List<GameItem>> mapMeshes = scene.getAnimatedMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			bindTextures(GL_TEXTURE2);

			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				animDepthShaderProgram.setMatrix4f("modelMatrix", gameItem.getModelMatrix());
				final AnimatedFrame frame = ((AnimGameItem) gameItem).getCurrentAnimation().getCurrentFrame();
				animDepthShaderProgram.setMatrices4f("jointsMatrix", frame.getJointMatrices());
			});
		}
	}

	private void renderNonInstancedMeshes(final Scene scene) {
		depthShaderProgram.setBoolean("isInstanced", false);

		final Map<AbstractMesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			bindTextures(GL_TEXTURE2);

			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				depthShaderProgram.setMatrix4f("modelNonInstancedMatrix", gameItem.getModelMatrix());
			});
		}
	}

	private void renderInstancedMeshes(final Scene scene) {
		depthShaderProgram.setBoolean("isInstanced", true);

		final Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
		for (final InstancedMesh mesh : mapMeshes.keySet()) {
			filteredItems.clear();

			for (GameItem gameItem : mapMeshes.get(mesh))
				if (gameItem.isInsideFrustum())
					filteredItems.add(gameItem);

			bindTextures(GL_TEXTURE2);

			mesh.renderListInstanced(filteredItems);
		}
	}

	public void cleanup() {

		if (depthShaderProgram != null)
			depthShaderProgram.cleanup();

		if (animDepthShaderProgram != null)
			animDepthShaderProgram.cleanup();
	}

	public List<GameItem> getFilteredItems() {
		return filteredItems;
	}
}
