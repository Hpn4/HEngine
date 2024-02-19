package hengine.engine.graph.renderer.shadow;

import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColorMask;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

import hengine.engine.graph.Camera;
import hengine.engine.graph.ShaderProgram;
import hengine.engine.graph.Transformation;
import hengine.engine.graph.anime.AnimGameItem;
import hengine.engine.graph.anime.AnimatedFrame;
import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.AnimatedMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.renderer.Renderer;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.world.Scene;
import hengine.engine.world.item.GameItem;

public class ShadowRenderer {

	public static final int NUM_CASCADES = 3;

	public static final float[] CASCADE_SPLITS = new float[] { HWindow.Z_FAR / 10, HWindow.Z_FAR / 5, HWindow.Z_FAR };

	private ShadowBuffer shadowBuffer;

	private ShaderProgram depthShaderProgram;

	private ShaderProgram animDepthShaderProgram;

	private ShaderProgram alphaDepthShaderProgram;

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
		setupAlphaDepthShader();

		float zNear = HWindow.Z_NEAR;
		for (int i = 0; i < NUM_CASCADES; i++)
			shadowCascades.add(new ShadowCascade(zNear, zNear = CASCADE_SPLITS[i]));
	}

	private void setupDepthShader() throws Exception {
		depthShaderProgram = new ShaderProgram("depth");

		depthShaderProgram.createUniform("isInstanced");
		depthShaderProgram.createUniform("orthoProjLightViewModelNonInstancedMatrix");
	}

	private void setupAnimDepthShader() throws Exception {
		animDepthShaderProgram = new ShaderProgram("depth_animated.vert", null, "depth.frag");

		animDepthShaderProgram.createUniform("jointsMatrix");
		animDepthShaderProgram.createUniform("orthoProjLightViewModelMatrix");
	}

	private void setupAlphaDepthShader() throws Exception {
		alphaDepthShaderProgram = new ShaderProgram("depth_alpha");

		alphaDepthShaderProgram.createUniform("isInstanced");
		alphaDepthShaderProgram.createUniform("orthoProjLightViewModelNonInstancedMatrix");
		alphaDepthShaderProgram.createUniform("diffuseMap");
	}

	private void update(final HWindow window, final Matrix4f viewMatrix, final Scene scene) {
		final DirectionalLight directionalLight = scene.getSceneLight().getDirectionalLight();
		for (int i = 0; i < NUM_CASCADES; i++)
			shadowCascades.get(i).update(window, viewMatrix, directionalLight);
	}

	public void render(final HWindow window, final Scene scene, final Camera camera, final Renderer renderer) {
		update(window, camera.getViewMatrix(), scene);

		final ShadowTexture shadowTexture = shadowBuffer.getDepthMapTexture();

		// On bind le frame buffer, met a jour le viewport et empeche d'ecrire les
		// couleurs, c'est useless
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, shadowBuffer.getDepthMapFBO());
		glViewport(0, 0, shadowTexture.getWidth(), shadowTexture.getHeight());
		glColorMask(false, false, false, false);

		// Render scene for each cascade map
		for (int i = 0; i < NUM_CASCADES; i++) {
			final ShadowCascade shadowCascade = shadowCascades.get(i);
			final Matrix4f orthoProjLightViewMatrix = shadowCascade.getOrthoProjLightViewMatrix();

			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowTexture.getIds()[i], 0);
			glClear(GL_DEPTH_BUFFER_BIT);

			// Basic object
			depthShaderProgram.bind();

			renderNonInstancedMeshes(scene, orthoProjLightViewMatrix);

			renderInstancedMeshes(scene, orthoProjLightViewMatrix, shadowCascade.getFrustum());

			depthShaderProgram.unbind();

			// Animated object
			animDepthShaderProgram.bind();

			renderAnimatedMeshes(scene, orthoProjLightViewMatrix);

			animDepthShaderProgram.unbind();

			// Transparent object
			alphaDepthShaderProgram.bind();

			renderTransparentNonInstancedMeshes(scene, orthoProjLightViewMatrix);

			renderTransparentInstancedMeshes(scene, orthoProjLightViewMatrix, shadowCascade.getFrustum());

			alphaDepthShaderProgram.unbind();
		}

		// On rétablie les couleurs et unbind le framebuffer
		glColorMask(true, true, true, true);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}

	private void renderNonInstancedMeshes(final Scene scene, final Matrix4f orthoProjLightViewMatrix) {
		depthShaderProgram.setBoolean("isInstanced", false);

		final Map<AbstractMesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				depthShaderProgram.setMatrix4f("orthoProjLightViewModelNonInstancedMatrix", Transformation
						.buildProjectionViewModelMatrix(orthoProjLightViewMatrix, gameItem.getModelMatrix()));
			}, false);
		}
	}

	/**
	 * private int a = 0, i = 0, j = 0;
	 */

	private void renderInstancedMeshes(final Scene scene, final Matrix4f orthoProjLightViewMatrix,
			final FrustumIntersection frustum) {
		depthShaderProgram.setBoolean("isInstanced", true);

		final Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
		for (final InstancedMesh mesh : mapMeshes.keySet()) {
			filteredItems.clear();

			final List<GameItem> items = mapMeshes.get(mesh);
			items.stream().peek(e -> e.updateViewSpaceDepth(orthoProjLightViewMatrix)).filter(e -> e.isInsideFrustum())
					.filter(e -> frustum.testAab(e.getBox3D()[0].origin, e.getBox3D()[0].dim)).sorted()
					.forEach(filteredItems::add);

			/**
			 * Permet de voire combien d'objet sont filtré a chaque fois a = i = j = 0;
			 * items.stream().peek(e -> e.updateViewSpaceDepth(projectionViewMatrix)).peek(e
			 * -> a++).filter(e -> e.isInsideFrustum()).peek(e -> i++).filter(e ->
			 * frustum.testAab(e.getBox3D()[0].origin, e.getBox3D()[0].dim)).peek(e ->
			 * j++).sorted().forEach(filteredItems::add);
			 * 
			 * System.out.println("a : " + a + "i : " + i + " , j : " + j);
			 */

			mesh.renderListInstanced(filteredItems, false, orthoProjLightViewMatrix);
		}
	}

	private void renderAnimatedMeshes(final Scene scene, final Matrix4f orthoProjLightViewMatrix) {
		final Map<AnimatedMesh, List<GameItem>> mapMeshes = scene.getAnimatedMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				// On calcul et transfert la matrice
				animDepthShaderProgram.setMatrix4f("orthoProjLightViewModelMatrix", Transformation
						.buildProjectionViewModelMatrix(orthoProjLightViewMatrix, gameItem.getModelMatrix()));

				final AnimatedFrame frame = ((AnimGameItem) gameItem).getCurrentAnimation().getCurrentFrame();
				animDepthShaderProgram.setMatrices4f("jointsMatrix", frame.getJointMatrices());
			}, false);
		}
	}

	private void renderTransparentNonInstancedMeshes(final Scene scene, final Matrix4f orthoProjLightViewMatrix) {
		alphaDepthShaderProgram.setBoolean("isInstanced", false);

		final Map<AbstractMesh, List<GameItem>> mapMeshes = scene.getTransparentGameMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {

			// Init Rendering
			if (mesh.getMaterial().getDiffuseMap() != null) {
				glActiveTexture(GL_TEXTURE0);
				mesh.getMaterial().getDiffuseMap().bind();
			}

			alphaDepthShaderProgram.setInt("diffuseMap", 0);

			// Render
			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				alphaDepthShaderProgram.setMatrix4f("orthoProjLightViewModelNonInstancedMatrix", Transformation
						.buildProjectionViewModelMatrix(orthoProjLightViewMatrix, gameItem.getModelMatrix()));

			}, false);

			// End rendering
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, 0);
		}
	}

	private void renderTransparentInstancedMeshes(final Scene scene, final Matrix4f orthoProjLightViewMatrix,
			final FrustumIntersection frustum) {
		alphaDepthShaderProgram.setBoolean("isInstanced", true);

		final Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getTransparentGameInstancedMeshes();
		for (final InstancedMesh mesh : mapMeshes.keySet()) {
			filteredItems.clear();

			final List<GameItem> items = mapMeshes.get(mesh);
			items.stream().peek(e -> e.updateViewSpaceDepth(orthoProjLightViewMatrix)).filter(e -> e.isInsideFrustum())
					.filter(e -> frustum.testAab(e.getBox3D()[0].origin, e.getBox3D()[0].dim)).sorted()
					.forEach(filteredItems::add);

			// Init Rendering
			if (mesh.getMaterial().getDiffuseMap() != null) {
				glActiveTexture(GL_TEXTURE0);
				mesh.getMaterial().getDiffuseMap().bind();
			}

			alphaDepthShaderProgram.setInt("diffuseMap", 0);

			// Render
			mesh.renderListInstanced(filteredItems, false, orthoProjLightViewMatrix);

			// End rendering
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, 0);
		}
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

		if (alphaDepthShaderProgram != null)
			alphaDepthShaderProgram.cleanup();
	}
}
