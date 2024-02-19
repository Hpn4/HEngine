package hengine.engine.graph;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import hengine.engine.graph.anime.AnimGameItem;
import hengine.engine.graph.anime.AnimatedFrame;
import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.graph.light.PointLight;
import hengine.engine.graph.light.SpotLight;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.AnimatedMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.mesh.ParticleMesh;
import hengine.engine.graph.postProcessing.PostProcessingRenderer;
import hengine.engine.graph.shadow.ShadowCascade;
import hengine.engine.graph.shadow.ShadowRenderer;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.graphics.paint.Color;
import hengine.engine.item.GameItem;
import hengine.engine.item.SkyBox;
import hengine.engine.particles.IParticleEmitter;
import hengine.engine.world.Scene;
import hengine.engine.world.SceneLight;

public class Renderer {

	public static final int MAX_POINT_LIGHTS = 100;

	private static final int MAX_SPOT_LIGHTS = 5;

	private final ShadowRenderer shadowRenderer;

	private PostProcessingRenderer postProcessingRenderer;

	// private final ZRenderer zRenderer;

	private ShaderProgram sceneShaderProgram;

	private ShaderProgram animSceneShaderProgram;

	private ShaderProgram skyBoxShaderProgram;

	private ShaderProgram particlesShaderProgram;

	public static int drawCalls = 0;

	private final float specularPower;

	private final FrustumCullingFilter frustumFilter;

	private final List<GameItem> filteredItems;

	// private int queries;

	public Renderer() {
		specularPower = 10f;
		shadowRenderer = new ShadowRenderer();
		// zRenderer = new ZRenderer();
		frustumFilter = new FrustumCullingFilter();
		filteredItems = new ArrayList<>();
	}

	public void init(final HWindow window) throws Exception {
		postProcessingRenderer = new PostProcessingRenderer(window);

		// queries = glGenQueries();

		shadowRenderer.init();
		// zRenderer.init(window);
		setupSkyBoxShader();
		setupSceneShader();
		setupParticlesShader();
	}

	public void render(final HWindow window, final Camera camera, final Scene scene, final boolean sceneChanged) {
		// glBeginQuery(GL_TIME_ELAPSED, queries);
		Renderer.drawCalls = 0;
		clear();

		// Matrice utilitaire
		final Matrix4f projectionMatrix = window.getProjectionMatrix(), viewMatrix = camera.getViewMatrix();

		// Met à jour le Frustum que si la scene à changer
		if (sceneChanged) {
			frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
			frustumFilter.filter(scene.getGameMeshes());
			frustumFilter.filter(scene.getGameInstancedMeshes());
			frustumFilter.filter(scene.getAnimatedMeshes());

			// zRenderer.render(projectionMatrix, viewMatrix, scene);

			if (scene.isRenderShadows())
				shadowRenderer.render(window, scene, camera, this);
		}

		glViewport(0, 0, window.getWidth(), window.getHeight());

		// postProcessingRenderer.getFirst().startRender();

		renderSkyBox(projectionMatrix, viewMatrix, scene);

		renderScene(projectionMatrix, viewMatrix, scene, sceneChanged);

		renderParticles(projectionMatrix, viewMatrix, scene);

		renderUI(scene, window);

		// postProcessingRenderer.getFirst().endRender();

		// postProcessingRenderer.render();
		// System.out.println(Renderer.drawCalls);

		// glEndQuery(GL_TIME_ELAPSED);

		// GL43.quer
	}

	private void renderUI(final Scene scene, final HWindow window) {
		final Graphics g = window.getGraphics();
		g.startRendering(window);

		g.setFontSize(40);
		g.setPaint(Color.RED);
		g.drawText(0, window.getHeight() / 2, "Nombre de lumiere : " + scene.getSceneLight().getPointLights().size());

		int i = 0;
		final Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
		for (final InstancedMesh mesh : mapMeshes.keySet())
			for (final GameItem gameItem : mapMeshes.get(mesh))
				if (gameItem.isInsideFrustum())
					i++;

		g.drawText(0, window.getHeight() / 2 + 40, "Nombre de sommet : " + (i * 21858));

		g.endRendering(window);
	}

	private void setupParticlesShader() throws Exception {
		particlesShaderProgram = new ShaderProgram("particles.vert", "particles.geom", "particles.frag");

		particlesShaderProgram.createUniform("viewMatrix", "projectionMatrix");
		particlesShaderProgram.createUniform("texture_sampler");
	}

	private void setupSkyBoxShader() throws Exception {
		skyBoxShaderProgram = new ShaderProgram("sb");

		// Create uniforms for projection matrix
		skyBoxShaderProgram.createUniform("projectionMatrix", "modelViewMatrix");

		skyBoxShaderProgram.createUniform("texture_sampler", "secondSky");
		skyBoxShaderProgram.createUniform("blendFactor");
	}

	private void setupSceneShader() throws Exception {
		// Create shader
		sceneShaderProgram = new ShaderProgram("scene");

		// Create uniforms for view and projection matrices
		sceneShaderProgram.createUniform("viewMatrix", "projectionMatrix");

		// Create uniforms for texture sampler
		sceneShaderProgram.createUniform("texture_sampler", "normalMap", "zmap");

		// Create uniform for material
		sceneShaderProgram.createMaterialUniform("material");

		// Create lighting related uniforms
		sceneShaderProgram.createUniform("specularPower", "ambientLight");
		sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
		sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
		sceneShaderProgram.createDirectionalLightUniform("directionalLight");

		// Create uniform for fog
		sceneShaderProgram.createFogUniform("fog");

		// Create uniforms for shadow mapping
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++)
			sceneShaderProgram.createUniform("shadowMap_" + i);

		sceneShaderProgram.createUniform("orthoProjectionMatrix", ShadowRenderer.NUM_CASCADES);
		sceneShaderProgram.createUniform("modelNonInstancedMatrix");
		sceneShaderProgram.createUniform("lightViewMatrix", ShadowRenderer.NUM_CASCADES);
		sceneShaderProgram.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
		sceneShaderProgram.createUniform("renderShadow");

		sceneShaderProgram.createUniform("isInstanced");

		sceneShaderProgram.createUniform("selectedNonInstanced");

		// Create shader
		animSceneShaderProgram = new ShaderProgram("scene_animated.vert", null, "scene.frag");

		// Create uniforms for view and projection matrices
		animSceneShaderProgram.createUniform("viewMatrix", "projectionMatrix");

		// Create uniforms for texture sampler
		animSceneShaderProgram.createUniform("texture_sampler", "normalMap", "zmap");

		// Create uniform for material
		animSceneShaderProgram.createMaterialUniform("material");

		// Create lighting related uniforms
		animSceneShaderProgram.createUniform("specularPower", "ambientLight");
		animSceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
		animSceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
		animSceneShaderProgram.createDirectionalLightUniform("directionalLight");

		// Create uniform for fog
		animSceneShaderProgram.createFogUniform("fog");

		// Create uniforms for shadow mapping
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++)
			animSceneShaderProgram.createUniform("shadowMap_" + i);

		animSceneShaderProgram.createUniform("orthoProjectionMatrix", ShadowRenderer.NUM_CASCADES);
		animSceneShaderProgram.createUniform("lightViewMatrix", ShadowRenderer.NUM_CASCADES);
		animSceneShaderProgram.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
		animSceneShaderProgram.createUniform("renderShadow");

		// Information sur le model
		animSceneShaderProgram.createUniform("jointsMatrix");
		animSceneShaderProgram.createUniform("modelMatrix");
		animSceneShaderProgram.createUniform("selected");
	}

	public void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	}

	private void renderParticles(final Matrix4f projectionMatrix, final Matrix4f viewMatrix, final Scene scene) {
		particlesShaderProgram.bind();

		particlesShaderProgram.setMatrix4f("viewMatrix", viewMatrix);
		particlesShaderProgram.setInt("texture_sampler", 0);
		particlesShaderProgram.setMatrix4f("projectionMatrix", projectionMatrix);

		final IParticleEmitter[] emitters = scene.getParticleEmitters();
		final int numEmitters = emitters != null ? emitters.length : 0;

		glDepthMask(false);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE);

		for (int i = 0; i < numEmitters; i++) {
			final IParticleEmitter emitter = emitters[i];
			final ParticleMesh mesh = (ParticleMesh) emitter.getBaseParticle().getMesh();

			mesh.renderListInstanced(emitter.getParticles(), viewMatrix);
		}

		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDepthMask(true);

		particlesShaderProgram.unbind();
	}

	public void renderSkyBox(final Matrix4f projectionMatrix, final Matrix4f viewMatrix, final Scene scene) {
		final SkyBox skyBox = scene.getSkyBox();

		if (skyBox != null) {
			skyBoxShaderProgram.bind();

			skyBoxShaderProgram.setInt("texture_sampler", 0);
			skyBoxShaderProgram.setInt("secondSky", 1);

			skyBoxShaderProgram.setMatrix4f("projectionMatrix", projectionMatrix);

			final Matrix4f view = new Matrix4f(viewMatrix);

			view.m30(0);
			view.m31(0);
			view.m32(0);

			skyBoxShaderProgram.setMatrix4f("modelViewMatrix", view.mul(skyBox.getModelMatrix()));

			skyBoxShaderProgram.setFloat("blendFactor", skyBox.getBlendFactor());

			skyBox.getMesh().render();

			skyBoxShaderProgram.unbind();
		}
	}

	public void renderScene(final Matrix4f projectionMatrix, final Matrix4f viewMatrix, final Scene scene,
			final boolean sceneChanged) {
		// Met a jour les lumiere
		final SceneLight sceneLight = scene.getSceneLight();

		final DirectionalLight dirLight = sceneLight.getDirectionalLight();
		final List<PointLight> pointLights = sceneLight.getPointLights();
		final List<SpotLight> spotLights = sceneLight.getSpotLights();

		// Charge les ombres
		shadowRenderer.bindTextures(GL_TEXTURE2);
		// zRenderer.bindTextures(GL_TEXTURE5);

		// Pour les texture static
		setupSceneShader(projectionMatrix, viewMatrix, scene, sceneShaderProgram);

		// Met en place les lumiere pour les modele static
		sceneShaderProgram.setDirectionalLight("directionalLight", dirLight);
		sceneShaderProgram.setPointLights("pointLights", pointLights);
		sceneShaderProgram.setSpotLights("spotLights", spotLights);

		renderNonInstancedMeshes(scene);

		renderInstancedMeshes(scene, sceneChanged);

		sceneShaderProgram.unbind();

		// Pour les textures animé
		setupSceneShader(projectionMatrix, viewMatrix, scene, animSceneShaderProgram);

		// Met el place les lumiere pour les modeles animé
		animSceneShaderProgram.setDirectionalLight("directionalLight", dirLight);
		animSceneShaderProgram.setPointLights("pointLights", pointLights);
		animSceneShaderProgram.setSpotLights("spotLights", spotLights);

		renderAnimatedMeshes(scene);

		animSceneShaderProgram.unbind();
	}

	private void setupSceneShader(final Matrix4f projectionMatrix, final Matrix4f viewMatrix, final Scene scene,
			final ShaderProgram shader) {
		shader.bind();

		shader.setMatrix4f("viewMatrix", viewMatrix);
		shader.setMatrix4f("projectionMatrix", projectionMatrix);

		final List<ShadowCascade> shadowCascades = shadowRenderer.getShadowCascades();
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
			final ShadowCascade shadowCascade = shadowCascades.get(i);
			shader.setMatrix4fAtIndex("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix(), i);
			shader.setFloatAtIndex("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
			shader.setMatrix4fAtIndex("lightViewMatrix", shadowCascade.getLightViewMatrix(), i);
		}

		shader.setVector3f("ambientLight", scene.getSceneLight().getAmbientLight());
		shader.setFloat("specularPower", specularPower);

		shader.setFog("fog", scene.getFog());

		int textureBlock = 0;
		shader.setInt("texture_sampler", textureBlock++);
		shader.setInt("normalMap", textureBlock++);

		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++)
			shader.setInt("shadowMap_" + i, textureBlock++);

		shader.setInt("zmap", textureBlock++);

		shader.setBoolean("renderShadow", scene.isRenderShadows());
	}

	private void renderAnimatedMeshes(final Scene scene) {
		// Render each mesh with the associated game Items
		final Map<AnimatedMesh, List<GameItem>> mapMeshes = scene.getAnimatedMeshes();
		for (final AnimatedMesh mesh : mapMeshes.keySet()) {
			animSceneShaderProgram.setMaterial("material", mesh.getMaterial());

			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				animSceneShaderProgram.setBoolean("selected", gameItem.isSelected());
				animSceneShaderProgram.setMatrix4f("modelMatrix", gameItem.getModelMatrix());

				final AnimatedFrame frame = ((AnimGameItem) gameItem).getCurrentAnimation().getCurrentFrame();
				animSceneShaderProgram.setMatrices4f("jointsMatrix", frame.getJointMatrices());
			});
		}
	}

	private void renderNonInstancedMeshes(final Scene scene) {
		sceneShaderProgram.setBoolean("isInstanced", false);

		// Render each mesh with the associated game Items
		final Map<AbstractMesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			sceneShaderProgram.setMaterial("material", mesh.getMaterial());

			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				sceneShaderProgram.setBoolean("selectedNonInstanced", gameItem.isSelected());
				sceneShaderProgram.setMatrix4f("modelNonInstancedMatrix", gameItem.getModelMatrix());
			});
		}
	}

	private void renderInstancedMeshes(final Scene scene, final boolean sceneChanged) {
		sceneShaderProgram.setBoolean("isInstanced", true);

		// Render each mesh with the associated game Items
		final Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
		for (final InstancedMesh mesh : mapMeshes.keySet()) {
			sceneShaderProgram.setMaterial("material", mesh.getMaterial());

			if (sceneChanged) {
				filteredItems.clear();
				for (final GameItem gameItem : mapMeshes.get(mesh))
					if (gameItem.isInsideFrustum())
						filteredItems.add(gameItem);
			}

			mesh.renderListInstanced(filteredItems);
		}
	}

	public void cleanup() {
		if (shadowRenderer != null)
			shadowRenderer.cleanup();

		if (skyBoxShaderProgram != null)
			skyBoxShaderProgram.cleanup();

		if (sceneShaderProgram != null)
			sceneShaderProgram.cleanup();

		if (animSceneShaderProgram != null)
			animSceneShaderProgram.cleanup();

		if (particlesShaderProgram != null)
			particlesShaderProgram.cleanup();
	}
}
