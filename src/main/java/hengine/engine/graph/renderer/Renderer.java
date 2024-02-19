package hengine.engine.graph.renderer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_QUERY_RESULT;
import static org.lwjgl.opengl.GL15.glBeginQuery;
import static org.lwjgl.opengl.GL15.glDeleteQueries;
import static org.lwjgl.opengl.GL15.glEndQuery;
import static org.lwjgl.opengl.GL15.glGenQueries;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL33.GL_TIME_ELAPSED;
import static org.lwjgl.opengl.GL33.glGetQueryObjecti64;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import hengine.engine.graph.Camera;
import hengine.engine.graph.FrustumCullingFilter;
import hengine.engine.graph.Options;
import hengine.engine.graph.RendererOption;
import hengine.engine.graph.ShaderProgram;
import hengine.engine.graph.Transformation;
import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.graph.light.PointLight;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.AnimParticleMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.graph.mesh.ParticleMesh;
import hengine.engine.graph.particles.FlowParticleEmitter;
import hengine.engine.graph.particles.IParticleEmitter;
import hengine.engine.graph.renderer.gbuffer.GBuffer;
import hengine.engine.graph.renderer.gbuffer.GBufferRenderer;
import hengine.engine.graph.renderer.postProcessing.PostProcessingRenderer;
import hengine.engine.graph.renderer.postProcessing.step.PostProcessingStep;
import hengine.engine.graph.renderer.shadow.ShadowRenderer;
import hengine.engine.graph.renderer.ssao.SSAORenderer;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.graphics.paint.Color;
import hengine.engine.utils.loader.texture.Texture;
import hengine.engine.world.Scene;
import hengine.engine.world.SceneLight;
import hengine.engine.world.item.GameItem;
import hengine.engine.world.item.SkyBox;

public class Renderer {

	// Number of each lights
	public static final int MAX_POINT_LIGHTS = 100;

	// private static final int MAX_SPOT_LIGHTS = 5;

	private final FrustumCullingFilter frustumFilter;

	// All the master renderer object
	private PostProcessingRenderer postProcessingRenderer;

	private TransparentRenderer transparentRenderer;

	private ShadowRenderer shadowRenderer;

	private GBufferRenderer gbufferRenderer;

	private SSAORenderer ssaoRenderer;

	// All the shader program
	private ShaderProgram lightShaderProgram;

	private ShaderProgram skyBoxShaderProgram;

	private ShaderProgram particlesShaderProgram;

	private ShaderProgram animParticlesShaderProgram;

	// Time query
	public static int timeQuery;

	// Debug info
	public static int drawCalls = 0;

	public static int instancedDrawCalls = 0;

	public static int instancedItem = 0;

	private boolean first;

	public Renderer() {
		frustumFilter = new FrustumCullingFilter();
	}

	public void init(final HWindow window) throws Exception {
		postProcessingRenderer = new PostProcessingRenderer(window);
		transparentRenderer = new TransparentRenderer();
		gbufferRenderer = new GBufferRenderer(window);
		ssaoRenderer = new SSAORenderer(window);
		shadowRenderer = new ShadowRenderer();

		timeQuery = glGenQueries();
		setupSkyBoxShader();
		setupLightShader();
		setupParticlesShader();
		setupAnimParticleSHader();
	}

	private void setupParticlesShader() throws Exception {
		particlesShaderProgram = new ShaderProgram("particles/particles.vert", "particles/rect.geom",
				"particles/particles.frag");

		particlesShaderProgram.createUniform("projectionMatrix");
		particlesShaderProgram.createUniform("diffuseMap");

		particlesShaderProgram.bind();
		particlesShaderProgram.setInt("diffuseMap", 0);
		particlesShaderProgram.unbind();
	}

	public void setupAnimParticleSHader() throws Exception {
		animParticlesShaderProgram = new ShaderProgram("particles/anim.vert", null, "particles/anim.frag");

		animParticlesShaderProgram.createUniform("projectionMatrix");

		for (int i = 0; i < 4; i++)
			animParticlesShaderProgram.createUniform("texCoordOff[" + i + "]");
		animParticlesShaderProgram.createUniform("useColor");

		animParticlesShaderProgram.createUniform("colorStart");
		animParticlesShaderProgram.createUniform("colorFactor");
	}

	private void setupSkyBoxShader() throws Exception {
		skyBoxShaderProgram = new ShaderProgram("sb");

		// Create uniforms for projection matrix
		skyBoxShaderProgram.createUniform("projModelViewMatrix");

		skyBoxShaderProgram.createUniform("texture_sampler", "secondSky");
		skyBoxShaderProgram.createUniform("blendFactor");

		skyBoxShaderProgram.createUniform("screenSize", "ambientLight", "depthText");

		skyBoxShaderProgram.bind();
		int textureBlock = 0;
		skyBoxShaderProgram.setInt("texture_sampler", textureBlock++);
		skyBoxShaderProgram.setInt("secondSky", textureBlock++);

		// Depth texture from gbuffer
		skyBoxShaderProgram.setInt("depthText", textureBlock++);
		skyBoxShaderProgram.unbind();
	}

	private void setupLightShader() throws Exception {
		// Create shader
		lightShaderProgram = new ShaderProgram("light");

		lightShaderProgram.createUniform("invProjectionMatrix");
		lightShaderProgram.createUniform("useSSAO");

		lightShaderProgram.createUniform("depthText");
		lightShaderProgram.createUniform("diffuseText");
		lightShaderProgram.createUniform("normalsText");
		lightShaderProgram.createUniform("shadowText");
		lightShaderProgram.createUniform("ssaoText");

		lightShaderProgram.createDirectionalLightUniform("directionalLight");

		lightShaderProgram.createFogUniform("fog");

		lightShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);

		lightShaderProgram.bind();
		// Toute les textures
		lightShaderProgram.setInt("depthText", 0);
		lightShaderProgram.setInt("diffuseText", 1);
		lightShaderProgram.setInt("normalsText", 2);
		lightShaderProgram.setInt("shadowText", 3);
		lightShaderProgram.setInt("ssaoText", 4);
		lightShaderProgram.unbind();
	}

	public void render(final HWindow window, final Camera camera, final Scene scene, final RendererOption option) {
		final boolean outputQuery = option.outPutGBUInfo;
		double GPUTime = 0d;
		drawCalls = instancedDrawCalls = instancedItem = 0;

		if (outputQuery)
			System.out.println("** [ New frame ] **, nmbLight : " + scene.getSceneLight().getPointLights().size());

		// Clear and setup matrices
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);

		// Matrice utilitaire
		final Matrix4f projectionMatrix = window.getProjectionMatrix(),
				invProjectionMatrix = window.getInvProjectionMatrix();
		final Matrix4f viewMatrix = camera.getViewMatrix(), invViewMatrix = camera.getInvViewMatrix();
		final Matrix4f projectionViewMatrix = new Matrix4f(projectionMatrix);

		// On creer la matrice de vue et de projection
		projectionViewMatrix.mul(viewMatrix);

		if (option.sceneChanged) {

			// Met à jour le Frustum et filtre les objets si ils sont ou non dedans
			frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
			frustumFilter.filter(scene.getGameMeshes());
			frustumFilter.filter(scene.getTransparentGameMeshes());
			frustumFilter.filter(scene.getGameInstancedMeshes());
			frustumFilter.filter(scene.getTransparentGameInstancedMeshes());
			frustumFilter.filter(scene.getAnimatedMeshes());

			// On dessine les ombres si l'option est activé
			if (Options.renderShadows) {
				if (outputQuery)
					glBeginQuery(GL_TIME_ELAPSED, timeQuery);

				shadowRenderer.render(window, scene, camera, this);

				if (outputQuery)
					GPUTime += endQuery("\tCascaded shadow map : ");

				// On remet a jour le view port (modifié par les shadow maps)
				glViewport(0, 0, window.getWidth(), window.getHeight());
			}

			GPUTime += gbufferRenderer.render(projectionMatrix, invProjectionMatrix, viewMatrix, invViewMatrix,
					projectionViewMatrix, scene, shadowRenderer, outputQuery);

			// On execute les SSAO si l'option est activé
			if (Options.activeSSAO) {
				if (outputQuery)
					glBeginQuery(GL_TIME_ELAPSED, timeQuery);

				ssaoRenderer.render(gbufferRenderer.getGBuffer(), projectionMatrix, invProjectionMatrix);

				if (outputQuery)
					GPUTime += endQuery("\tSSAO : ");
			}

		}

		if (outputQuery)
			glBeginQuery(GL_TIME_ELAPSED, timeQuery);

		// On vire le depth test et empeche d'écrire dans le buffer de profondeur
		glDepthMask(false);
		glDisable(GL_DEPTH_TEST);

		// On bind le frame buffer
		postProcessingRenderer.getFirst().startRender();

		// Light rendering
		renderLight(projectionMatrix, invProjectionMatrix, viewMatrix, scene, option.sceneChanged);
		GPUTime += endQuery(outputQuery, "\tRecording + light : ");

		// Skybox rendering
		renderSkyBox(projectionMatrix, viewMatrix, scene);
		GPUTime += endQuery(outputQuery, "\tSkybox : ");

		// Transparent meshes rendering
		transparentRenderer.render(projectionViewMatrix, invProjectionMatrix, viewMatrix, scene, shadowRenderer);
		GPUTime += endQuery(outputQuery, "\tTransparent meshes : ");

		// Particles rendering
		renderParticles(projectionMatrix, viewMatrix, scene);
		GPUTime += endQuery(outputQuery, "\tParticles : ");

		// Post processing effect
		postProcessingRenderer.getFirst().endRender();

		postProcessingRenderer.render(window, option);
		GPUTime += endQuery(outputQuery, "\tEnd record + Post processing effect (bloom, gamma, convert) : ");

		// UI rendering
		renderUI(scene, window, camera, option);

		if (outputQuery) {
			GPUTime += endQuery("\tUI : ");

			final double fps = 1000d / GPUTime;
			System.out.println("\tTotal GPU render time : " + GPUTime + "ms, GPU FPS : " + fps);
			System.out.println("\tTotal GPU draw : [ basic draw : " + drawCalls + ", instanced meshes draw : "
					+ instancedItem + " in " + instancedDrawCalls + " instanced draw calls ]");
			System.out.println("** [ End Frame ] **");
		}

		GL11.glFinish();

		// On verifie qu'il n'y a aucune erreur au niveau d'OpenGL
		outPutError("Render : ");
	}

	public void renderLight(final Matrix4f projectionMatrix, final Matrix4f invProjectionMatrix,
			final Matrix4f viewMatrix, final Scene scene, final boolean sceneChanged) {
		// Set settings
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE);

		final SceneLight sceneLight = scene.getSceneLight();

		final DirectionalLight dirLight = sceneLight.getDirectionalLight();
		final List<PointLight> pointLights = sceneLight.getPointLights();

		lightShaderProgram.bind();

		lightShaderProgram.setDirectionalLight("directionalLight", dirLight);

		lightShaderProgram.setMatrix4f("invProjectionMatrix", invProjectionMatrix);
		lightShaderProgram.setBoolean("useSSAO", Options.activeSSAO);

		final int numLights = pointLights.size();
		for (int i = 0; i < numLights; i++) {

			// Get a copy of the point light object and transform its position to view
			// coordinates
			final PointLight currPointLight = new PointLight(pointLights.get(i));
			final Vector4f lightPos = new Vector4f(currPointLight.getPosition(), 1);
			lightPos.mul(viewMatrix);

			currPointLight.getPosition().set(lightPos.x, lightPos.y, lightPos.z);

			lightShaderProgram.setPointLightAtIndex("pointLights", currPointLight, i);
		}

		lightShaderProgram.setFog("fog", scene.getFog());

		final int[] textureIds = gbufferRenderer.getGBuffer().getTextureIds();
		int textureBlocks = 0;
		for (; textureBlocks < GBuffer.TOTAL_TEXTURES; textureBlocks++) {
			glActiveTexture(GL_TEXTURE0 + textureBlocks);
			glBindTexture(GL_TEXTURE_2D, textureIds[textureBlocks]);
		}

		if (Options.activeSSAO) {
			glActiveTexture(GL_TEXTURE0 + textureBlocks);
			glBindTexture(GL_TEXTURE_2D, ssaoRenderer.getBuffer().getAOTextureId());
		}

		PostProcessingStep.quad.render();

		lightShaderProgram.unbind();

		// Reset settings
		glDisable(GL_BLEND);
	}

	public void renderSkyBox(final Matrix4f projectionMatrix, final Matrix4f viewMatrix, final Scene scene) {
		final SkyBox skyBox = scene.getSkyBox();

		skyBoxShaderProgram.bind();

		final GBuffer gbuffer = gbufferRenderer.getGBuffer();

		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, gbuffer.getDepthTexture());

		// Matrice de vision, on met a 0 la position de la camera
		final Matrix4f view = new Matrix4f(viewMatrix);
		view.m30(0);
		view.m31(0);
		view.m32(0);

		skyBoxShaderProgram.setMatrix4f("projModelViewMatrix",
				Transformation.buildProjectionViewMatrix(projectionMatrix, view).mul(skyBox.getModelMatrix()));

		// Fragment shader
		skyBoxShaderProgram.setVector2f("screenSize", gbuffer.getWidth(), gbuffer.getHeight());
		skyBoxShaderProgram.setVector3f("ambientLight", scene.getSceneLight().getAmbientLight());

		skyBoxShaderProgram.setFloat("blendFactor", skyBox.getBlendFactor());

		if (!first) {
			first = true;
			glBindVertexArray(skyBox.getMesh().getVaoId());
			skyBoxShaderProgram.validate();
		}

		skyBox.getMesh().render();

		skyBoxShaderProgram.unbind();
	}

	private void renderParticles(final Matrix4f projectionMatrix, final Matrix4f viewMatrix, final Scene scene) {
		// Set settings
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE);

		particlesShaderProgram.bind();

		particlesShaderProgram.setMatrix4f("projectionMatrix", projectionMatrix);

		List<IParticleEmitter> emitters = scene.getParticleEmitters();
		for (int i = 0; i < emitters.size(); i++) {
			final IParticleEmitter emitter = emitters.get(i);
			final ParticleMesh mesh = (ParticleMesh) emitter.getBaseParticle().getMesh();

			mesh.renderListInstanced(emitter.getParticles(), viewMatrix, true);
		}

		particlesShaderProgram.unbind();

		animParticlesShaderProgram.bind();

		animParticlesShaderProgram.setMatrix4f("projectionMatrix", projectionMatrix);
		emitters = scene.getAnimParticleEmitters();
		for (int i = 0; i < emitters.size(); i++) {
			final IParticleEmitter emitter = emitters.get(i);
			final AnimParticleMesh mesh = (AnimParticleMesh) emitter.getBaseParticle().getMesh();

			final Texture text = (Texture) mesh.getMaterial().getDiffuseMap();
			final int cols = text.getNumCols();
			final int rows = text.getNumRows();

			final Vector2f[] vecs = mesh.getTexCoordsOff();
			for (int j = 0; j < vecs.length; j++)
				animParticlesShaderProgram.setVector2fAtIndex("texCoordOff", vecs[j], j);

			if (emitter instanceof FlowParticleEmitter) {
				final FlowParticleEmitter flowEmit = (FlowParticleEmitter) emitter;
				animParticlesShaderProgram.setVector3f("colorStart", flowEmit.getStartColor());
				animParticlesShaderProgram.setVector3f("colorFactor", flowEmit.getColorFactor());

				animParticlesShaderProgram.setBoolean("useColor", flowEmit.useColorVariation());
			}

			mesh.renderListInstanced(emitter.getParticles(), viewMatrix, cols, rows);
		}

		animParticlesShaderProgram.unbind();

		// Restore settings
		glDisable(GL_BLEND);
	}

	private void renderUI(final Scene scene, final HWindow window, final Camera camera, final RendererOption option) {
		glEnable(GL_STENCIL_TEST);
		glClear(GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

		final Graphics g = window.getGraphics();
		final int y = window.getHeight() / 2;
		g.startRendering(window);

		g.setFontSize(30);
		g.setPaint(Color.RED);
		g.drawText(0, y, "Nombre de lumiere : " + scene.getSceneLight().getPointLights().size());

		int i = 0;
		final Map<AbstractMesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			int a = 0;
			for (final GameItem gameItem : mapMeshes.get(mesh))
				if (gameItem.isInsideFrustum())
					a++;
			i += a * ((Mesh) mesh).getVertexCount();
		}

		final Map<InstancedMesh, List<GameItem>> instancedMeshes = scene.getGameInstancedMeshes();
		for (final InstancedMesh mesh : instancedMeshes.keySet()) {
			int a = 0;
			for (final GameItem gameItem : instancedMeshes.get(mesh))
				if (gameItem.isInsideFrustum())
					a++;
			i += a * mesh.getVertexCount();
		}

		g.drawText(0, y - 25, "Nombre de sommet : " + i);
		g.drawText(0, y + 25, "SSAO Actif (F) : " + Options.activeSSAO);
		g.drawText(0, y + 50, "Apply exposure (E) : " + option.applyExposure);
		g.drawText(0, y + 75, "GPU Output (O) : " + option.outPutGBUInfo);

		g.drawText(0, y + 100, "Camera pos :" + camera.getPosition());

		g.endRendering(window);

		glDisable(GL_STENCIL_TEST);
	}

	private void outPutError(final String message) {
		final int error = glGetError();
		if (error != GL_NO_ERROR)
			System.err.println(message + "GL error code : " + error);
	}

	private double endQuery(final String message) {
		glEndQuery(GL_TIME_ELAPSED);
		final double time = glGetQueryObjecti64(timeQuery, GL_QUERY_RESULT) / 1000000.0d;
		System.out.println(message + time + "ms");
		return time;
	}

	private double endQuery(final boolean outputQuery, final String message) {
		double time = 0;
		if (outputQuery) {
			time = endQuery(message);
			glBeginQuery(GL_TIME_ELAPSED, timeQuery);
		}
		return time;
	}

	public void cleanup() {
		glDeleteQueries(timeQuery);

		if (skyBoxShaderProgram != null)
			skyBoxShaderProgram.cleanup();

		if (lightShaderProgram != null)
			lightShaderProgram.cleanup();

		if (particlesShaderProgram != null)
			particlesShaderProgram.cleanup();

		if (animParticlesShaderProgram != null)
			animParticlesShaderProgram.cleanup();

		if (shadowRenderer != null)
			shadowRenderer.cleanup();

		if (gbufferRenderer != null)
			gbufferRenderer.cleanup();

		if (ssaoRenderer != null)
			ssaoRenderer.cleanup();

		if (postProcessingRenderer != null)
			postProcessingRenderer.cleanUp();

		if (transparentRenderer != null)
			transparentRenderer.cleanup();
	}
}
