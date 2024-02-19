package hengine.engine.graph.renderer.gbuffer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_QUERY_RESULT;
import static org.lwjgl.opengl.GL15.glBeginQuery;
import static org.lwjgl.opengl.GL15.glEndQuery;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL33.GL_TIME_ELAPSED;
import static org.lwjgl.opengl.GL33.glGetQueryObjecti64;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import hengine.engine.graph.ShaderProgram;
import hengine.engine.graph.anime.AnimGameItem;
import hengine.engine.graph.anime.AnimatedFrame;
import hengine.engine.graph.light.PointLight;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.AnimatedMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.graph.renderer.Renderer;
import hengine.engine.graph.renderer.shadow.ShadowCascade;
import hengine.engine.graph.renderer.shadow.ShadowRenderer;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.utils.loader.StaticMeshesLoader;
import hengine.engine.world.Scene;
import hengine.engine.world.item.Decal;
import hengine.engine.world.item.GameItem;

public class GBufferRenderer {

	// Le frame buffer ainsi que toute les textures
	private final GBuffer gbuffer;

	// Tout les shader program
	private ShaderProgram gbufferShaderProgram;

	private ShaderProgram animGbufferShaderProgram;

	private ShaderProgram lightCubeShaderProgram;

	private ShaderProgram decalsShaderProgram;

	// Pour les cube de lumiere un simple cube
	private final Mesh decalCube;

	// Tmp matrix
	private final Matrix4f tmp;

	// Le frustum culling pour les objet instancié
	private final List<GameItem> filteredItems;

	public GBufferRenderer(final HWindow window) throws Exception {
		filteredItems = new ArrayList<>();
		gbuffer = new GBuffer(window);
		tmp = new Matrix4f();

		setupGbufferShader();
		setupAnimGbufferShader();
		setupLightCubeShader();
		setupDecalsShader();

		decalCube = StaticMeshesLoader.load("models/cube.obj", "")[0];
	}

	private void setupGbufferShader() throws Exception {
		gbufferShaderProgram = new ShaderProgram("gbuffer");

		// Create uniforms for projectionView matrix
		gbufferShaderProgram.createUniform("projectionViewMatrix");

		// Create uniforms for texture sampler
		gbufferShaderProgram.createUniform("diffuseMap", "normalMap");

		// Create uniform for material
		gbufferShaderProgram.createMaterialUniform("material");

		// Create uniforms for shadow mapping
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++)
			gbufferShaderProgram.createUniform("shadowMap_" + i);

		gbufferShaderProgram.createUniform("orthoProjLightViewMatrix", ShadowRenderer.NUM_CASCADES);
		gbufferShaderProgram.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
		gbufferShaderProgram.createUniform("renderShadow");

		gbufferShaderProgram.createUniform("isInstanced");

		// Create uniform for non instanced model
		gbufferShaderProgram.createUniform("normalNonInstancedMatrix");
		gbufferShaderProgram.createUniform("modelNonInstancedMatrix");
	}

	private void setupAnimGbufferShader() throws Exception {
		animGbufferShaderProgram = new ShaderProgram("gbuffer_animated.vert", null, "gbuffer.frag");

		// Create uniforms for view and projection matrices
		animGbufferShaderProgram.createUniform("projectionViewMatrix");

		// Create uniforms for texture sampler
		animGbufferShaderProgram.createUniform("diffuseMap", "normalMap");

		// Create uniform for material
		animGbufferShaderProgram.createMaterialUniform("material");

		// Create uniforms for shadow mapping
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++)
			animGbufferShaderProgram.createUniform("shadowMap_" + i);

		animGbufferShaderProgram.createUniform("orthoProjLightViewMatrix", ShadowRenderer.NUM_CASCADES);
		animGbufferShaderProgram.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
		animGbufferShaderProgram.createUniform("renderShadow");

		// Information sur le model
		animGbufferShaderProgram.createUniform("jointsMatrix");
		animGbufferShaderProgram.createUniform("normalMatrix");
		animGbufferShaderProgram.createUniform("modelMatrix");
	}

	private void setupLightCubeShader() throws Exception {
		lightCubeShaderProgram = new ShaderProgram("light_cube");

		lightCubeShaderProgram.createUniform("projViewModelMatrix");

		lightCubeShaderProgram.createUniform("lightColor");
	}

	private void setupDecalsShader() throws Exception {
		decalsShaderProgram = new ShaderProgram("decals");

		// Create uniform for model and invert model matrices
		decalsShaderProgram.createUniform("modelMatrix", "invModelMatrix");

		// Create uniform for view and projection matrices and their invert
		decalsShaderProgram.createUniform("viewMatrix", "invViewMatrix");
		decalsShaderProgram.createUniform("projectionMatrix", "invProjectionMatrix");

		// Create uniform for the depth texture and the texture of the decals
		decalsShaderProgram.createUniform("depthTex", "diffuseTex");

		// Create uniform for the screen resolution
		decalsShaderProgram.createUniform("halfPixelOff");
	}

	public double render(final Matrix4f projectionMatrix, final Matrix4f invProjectionMatrix, final Matrix4f viewMatrix,
			final Matrix4f invViewMatrix, final Matrix4f projectionViewMatrix, final Scene scene,
			final ShadowRenderer shadowRenderer, final boolean query) {
		double gbufferTime = 0d;

		if (query)
			glBeginQuery(GL_TIME_ELAPSED, Renderer.timeQuery);

		// Setup
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gbuffer.getGBufferId());
		glClear(GL_DEPTH_BUFFER_BIT);
		glDisable(GL_BLEND);

		renderGeometry(projectionViewMatrix, viewMatrix, scene, shadowRenderer);

		if (query) {
			gbufferTime += endQuery("\tGbuffer setup + Render geometry : ", Renderer.timeQuery);
			glBeginQuery(GL_TIME_ELAPSED, Renderer.timeQuery);
		}

		renderLightCube(projectionViewMatrix, scene);

		if (query) {
			gbufferTime += endQuery("\tRender light cube : ", Renderer.timeQuery);
			glBeginQuery(GL_TIME_ELAPSED, Renderer.timeQuery);
		}

		// renderCube(projectionViewMatrix, scene);

		glDepthMask(false);

		renderDecals(projectionMatrix, invProjectionMatrix, viewMatrix, invViewMatrix, scene);

		glDepthMask(true);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

		if (query) {
			gbufferTime += endQuery("\tRender decals + end gbuffer : ", Renderer.timeQuery);
			System.out.println("\tTotal gbuffer rendering time : " + gbufferTime + "ms");
		}

		return gbufferTime;
	}

	private double endQuery(final String message, final int timeQuery) {
		glEndQuery(GL_TIME_ELAPSED);
		final double time = glGetQueryObjecti64(timeQuery, GL_QUERY_RESULT) / 1000000.0d;
		System.out.println(message + time + "ms");
		return time;
	}

	public void renderCube(final Matrix4f projectionViewMatrix, final Scene scene) {
		lightCubeShaderProgram.bind();

		final Vector3f lightColor = new Vector3f(0.3f, 0.3f, 0.3f);
		final Map<AbstractMesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			final List<GameItem> items = mapMeshes.get(mesh);

			for (final GameItem item : items) {
				final AbstractMesh[] meshes = item.getMeshes();
				for (final AbstractMesh meshe : meshes) {
					projectionViewMatrix.mul(item.getModelMatrix(), tmp);
					lightCubeShaderProgram.setMatrix4f("projViewModelMatrix", tmp);
					lightCubeShaderProgram.setVector3f("lightColor", lightColor);

					((Mesh) meshe).getCollisionBoxMesh().render();
				}
			}
		}

		lightCubeShaderProgram.unbind();
	}

	private void renderLightCube(final Matrix4f projectionViewMatrix, final Scene scene) {
		final List<PointLight> points = scene.getSceneLight().getPointLights();

		lightCubeShaderProgram.bind();

		for (final PointLight point : points) {
			final Vector3f pos = point.getPosition();
			// Buil model matrix
			tmp.translationRotateScale(pos.x, pos.y, pos.z, 0, 0, 0, 0.25f, 0.25f, 0.25f, 0.25f);

			projectionViewMatrix.mul(tmp, tmp);

			lightCubeShaderProgram.setMatrix4f("projViewModelMatrix", tmp);

			lightCubeShaderProgram.setVector3f("lightColor", point.getColor());

			decalCube.render();
		}

		lightCubeShaderProgram.unbind();
	}

	private void renderDecals(final Matrix4f projectionMatrix, final Matrix4f invProjectionMatrix,
			final Matrix4f viewMatrix, final Matrix4f invViewMatrix, final Scene scene) {
		decalsShaderProgram.bind();

		// Set matrices
		decalsShaderProgram.setMatrix4f("projectionMatrix", projectionMatrix);
		decalsShaderProgram.setMatrix4f("viewMatrix", viewMatrix);

		decalsShaderProgram.setMatrix4f("invProjectionMatrix", invProjectionMatrix);
		decalsShaderProgram.setMatrix4f("invViewMatrix", invViewMatrix);

		// Set textures
		decalsShaderProgram.setInt("depthTex", 0);
		decalsShaderProgram.setInt("diffuseTex", 1);

		decalsShaderProgram.setVector2f("halfPixelOff", 0.5f / gbuffer.getWidth(), 0.5f / gbuffer.getHeight());

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, gbuffer.getDepthTexture());

		final List<GameItem> items = scene.getGameItems();
		for (final GameItem item : items) {
			final Decal[] decals = item.getDecals();

			if (decals != null && item.isInsideFrustum()) {

				final int numDecals = decals.length;
				for (int i = 0; i < numDecals; i++) {
					final Decal decal = decals[i];

					decalsShaderProgram.setMatrix4f("modelMatrix", decal.getModelMatrix());
					decalsShaderProgram.setMatrix4f("invModelMatrix", decal.getInvModelMatrix());

					glActiveTexture(GL_TEXTURE1);
					decal.getTexture().bind();

					decalCube.render();
				}
			}
		}

		decalsShaderProgram.unbind();
	}

	private void renderGeometry(final Matrix4f projectionViewMatrix, final Matrix4f viewMatrix, final Scene scene,
			final ShadowRenderer shadowRenderer) {
		// Charge les ombres
		shadowRenderer.bindTextures(GL_TEXTURE0 + AbstractMesh.TEXTURES);

		// Pour les texture static
		setupSceneShader(projectionViewMatrix, scene, gbufferShaderProgram, shadowRenderer);

		renderNonInstancedMeshes(scene);

		renderInstancedMeshes(scene, viewMatrix, projectionViewMatrix);

		gbufferShaderProgram.unbind();

		// Pour les textures animé
		setupSceneShader(projectionViewMatrix, scene, animGbufferShaderProgram, shadowRenderer);

		renderAnimatedMeshes(scene);

		animGbufferShaderProgram.unbind();
	}

	private void setupSceneShader(final Matrix4f projectionViewMatrix, final Scene scene, final ShaderProgram shader,
			final ShadowRenderer shadowRenderer) {
		shader.bind();

		shader.setMatrix4f("projectionViewMatrix", projectionViewMatrix);

		final List<ShadowCascade> shadowCascades = shadowRenderer.getShadowCascades();
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
			final ShadowCascade shadowCascade = shadowCascades.get(i);
			shader.setMatrix4fAtIndex("orthoProjLightViewMatrix", shadowCascade.getOrthoProjLightViewMatrix(), i);
			shader.setFloatAtIndex("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
		}

		int textureBlock = 0;
		shader.setInt("diffuseMap", textureBlock++);
		shader.setInt("normalMap", textureBlock++);

		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++)
			shader.setInt("shadowMap_" + i, textureBlock++);

		shader.setBoolean("renderShadow", scene.isRenderShadows());
	}

	private void renderNonInstancedMeshes(final Scene scene) {
		gbufferShaderProgram.setBoolean("isInstanced", false);

		final Map<AbstractMesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			gbufferShaderProgram.setMaterial("material", mesh.getMaterial());

			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				gbufferShaderProgram.setMatrix3f("normalNonInstancedMatrix", gameItem.getNormalMatrix());
				gbufferShaderProgram.setMatrix4f("modelNonInstancedMatrix", gameItem.getModelMatrix());
			});
		}
	}

	private void renderInstancedMeshes(final Scene scene, final Matrix4f viewMatrix,
			final Matrix4f projectionViewMatrix) {
		gbufferShaderProgram.setBoolean("isInstanced", true);

		final Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
		for (final InstancedMesh mesh : mapMeshes.keySet()) {
			gbufferShaderProgram.setMaterial("material", mesh.getMaterial());

			filteredItems.clear();

			final List<GameItem> items = mapMeshes.get(mesh);
			items.stream().peek(e -> e.updateViewSpaceDepth(viewMatrix)).filter(e -> e.isInsideFrustum()).sorted()
					.forEach(filteredItems::add);
			/**
			 * for(final GameItem item : items) if(item.isInsideFrustum()) {
			 * filteredItems.add(item); }
			 */
			mesh.renderListInstanced(filteredItems);
		}
	}

	private void renderAnimatedMeshes(final Scene scene) {
		final Map<AnimatedMesh, List<GameItem>> mapMeshes = scene.getAnimatedMeshes();
		for (final AnimatedMesh mesh : mapMeshes.keySet()) {
			animGbufferShaderProgram.setMaterial("material", mesh.getMaterial());

			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				animGbufferShaderProgram.setMatrix3f("normalMatrix", gameItem.getNormalMatrix());
				animGbufferShaderProgram.setMatrix4f("modelMatrix", gameItem.getModelMatrix());

				final AnimatedFrame frame = ((AnimGameItem) gameItem).getCurrentAnimation().getCurrentFrame();
				animGbufferShaderProgram.setMatrices4f("jointsMatrix", frame.getJointMatrices());
			});
		}
	}

	public GBuffer getGBuffer() {
		return gbuffer;
	}

	public void cleanup() {
		gbuffer.cleanup();

		decalsShaderProgram.cleanup();
		gbufferShaderProgram.cleanup();
		animGbufferShaderProgram.cleanup();
		lightCubeShaderProgram.cleanup();

		decalCube.cleanup();
	}
}
