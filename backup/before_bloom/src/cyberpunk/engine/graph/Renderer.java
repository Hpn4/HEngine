package hengine.engine.graph;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
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
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendEquation;
import static org.lwjgl.opengl.GL15.glDeleteQueries;
import static org.lwjgl.opengl.GL15.glGenQueries;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import hengine.engine.graph.gbuffer.GBuffer;
import hengine.engine.graph.gbuffer.GBufferRenderer;
import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.graph.light.PointLight;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.graph.mesh.ParticleMesh;
import hengine.engine.graph.postProcessing.PostProcessingRenderer;
import hengine.engine.graph.postProcessing.PostProcessingStep;
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

	// private static final int MAX_SPOT_LIGHTS = 5;

	private final FrustumCullingFilter frustumFilter;

	// All the master renderer object
	private PostProcessingRenderer postProcessingRenderer;

	private final ShadowRenderer shadowRenderer;

	private GBufferRenderer gbufferRenderer;

	// All the shader program
	private ShaderProgram lightShaderProgram;

	private ShaderProgram skyBoxShaderProgram;

	private ShaderProgram particlesShaderProgram;

	private final Matrix4f tmp;

	private final float specularPower;

	private int timeQuery;

	public static int drawCalls = 0;

	public Renderer() {
		specularPower = 5f;
		shadowRenderer = new ShadowRenderer();
		frustumFilter = new FrustumCullingFilter();
		tmp = new Matrix4f();
	}

	public void init(final HWindow window) throws Exception {
		postProcessingRenderer = new PostProcessingRenderer(window);
		gbufferRenderer = new GBufferRenderer(window);

		shadowRenderer.init();

		timeQuery = glGenQueries();

		setupSkyBoxShader();
		setupLightShader();
		setupParticlesShader();
	}

	public void render(final HWindow window, final Camera camera, final Scene scene, final boolean sceneChanged) {
		int error = glGetError();
		if (error != GL_NO_ERROR)
			System.err.println("GL error code : " + error);

		Renderer.drawCalls = 0;

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

		// Matrice utilitaire
		final Matrix4f projectionMatrix = window.getProjectionMatrix(),
				invProjectionMatrix = window.getInvProjectionMatrix(), viewMatrix = camera.getViewMatrix(),
				invViewMatrix = camera.getInvViewMatrix();

		if (sceneChanged) {
			// Met à jour le Frustum et si les objets sont ou non dedans
			frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
			frustumFilter.filter(scene.getGameMeshes());
			frustumFilter.filter(scene.getGameInstancedMeshes());
			frustumFilter.filter(scene.getAnimatedMeshes());

			if (scene.isRenderShadows())
				shadowRenderer.render(window, scene, camera, this);

			// System.out.println("setup + shadow : " + (glGetQueryObjecti64(timeQuery,
			// GL_QUERY_RESULT) / 1000000.0d) + "ms");

			// glBeginQuery(GL_TIME_ELAPSED, timeQuery);

			glViewport(0, 0, window.getWidth(), window.getHeight());

			gbufferRenderer.render(projectionMatrix, invProjectionMatrix, viewMatrix, invViewMatrix, scene,
					shadowRenderer);
		}

		// On vire le depth test et empeche d'écrire dans le buffer de profondeur c
		// useless
		glDepthMask(false);
		glDisable(GL_DEPTH_TEST);

		// On bind le frame buffer
		postProcessingRenderer.getFirst().startRender();

		renderLight(projectionMatrix, invProjectionMatrix, viewMatrix, scene, sceneChanged);

		renderSkyBox(projectionMatrix, viewMatrix, scene);

		renderParticles(projectionMatrix, viewMatrix, scene);

		postProcessingRenderer.getFirst().endRender();

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, gbufferRenderer.getGBuffer().getEmissiveTexture());

		postProcessingRenderer.render(window);

		renderUI(scene, window);

		// On retablie les parametre
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);

		error = glGetError();
		if (error != GL_NO_ERROR)
			System.err.println("GL error code : " + error);
	}

	private void setupParticlesShader() throws Exception {
		particlesShaderProgram = new ShaderProgram("particles.vert", "rect.geom", "particles.frag");

		particlesShaderProgram.createUniform("projectionMatrix");
		particlesShaderProgram.createUniform("diffuseMap");
	}

	private void setupSkyBoxShader() throws Exception {
		skyBoxShaderProgram = new ShaderProgram("sb");

		// Create uniforms for projection matrix
		skyBoxShaderProgram.createUniform("projModelViewMatrix");

		skyBoxShaderProgram.createUniform("texture_sampler", "secondSky");
		skyBoxShaderProgram.createUniform("blendFactor");

		skyBoxShaderProgram.createUniform("screenSize", "ambientLight", "depthText");
	}

	private void setupLightShader() throws Exception {
		// Create shader
		lightShaderProgram = new ShaderProgram("light");

		lightShaderProgram.createUniform("invProjectionMatrix");

		lightShaderProgram.createUniform("depthText");
		lightShaderProgram.createUniform("diffuseText");
		lightShaderProgram.createUniform("specularText");
		lightShaderProgram.createUniform("normalsText");
		lightShaderProgram.createUniform("shadowText");

		lightShaderProgram.createDirectionalLightUniform("directionalLight");
		lightShaderProgram.createUniform("specularPower");

		lightShaderProgram.createFogUniform("fog");

		lightShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
	}

	private void renderUI(final Scene scene, final HWindow window) {
		final Graphics g = window.getGraphics();
		final int y = window.getHeight() / 2;
		g.startRendering(window);

		g.setFontSize(20);
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

		g.drawText(0, y + 40, "Nombre de sommet : " + i);

		g.endRendering(window);
	}

	private void renderParticles(final Matrix4f projectionMatrix, final Matrix4f viewMatrix, final Scene scene) {
		particlesShaderProgram.bind();

		particlesShaderProgram.setInt("diffuseMap", 0);
		particlesShaderProgram.setMatrix4f("projectionMatrix", projectionMatrix);

		final IParticleEmitter[] emitters = scene.getParticleEmitters();
		final int numEmitters = emitters != null ? emitters.length : 0;

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE);

		for (int i = 0; i < numEmitters; i++) {
			final IParticleEmitter emitter = emitters[i];
			final ParticleMesh mesh = (ParticleMesh) emitter.getBaseParticle().getMesh();

			mesh.renderListInstanced(emitter.getParticles(), viewMatrix, true);
		}

		glDisable(GL_BLEND);

		particlesShaderProgram.unbind();
	}

	public void renderSkyBox(final Matrix4f projectionMatrix, final Matrix4f viewMatrix, final Scene scene) {
		final SkyBox skyBox = scene.getSkyBox();

		if (skyBox != null) {
			skyBoxShaderProgram.bind();

			final GBuffer gbuffer = gbufferRenderer.getGBuffer();

			int textureBlock = 0;
			skyBoxShaderProgram.setInt("texture_sampler", textureBlock++);
			skyBoxShaderProgram.setInt("secondSky", textureBlock++);

			// Depth texture from gbuffer
			skyBoxShaderProgram.setInt("depthText", textureBlock);

			glActiveTexture(GL_TEXTURE2);
			glBindTexture(GL_TEXTURE_2D, gbuffer.getDepthTexture());

			// Matrix
			final Matrix4f view = new Matrix4f(viewMatrix);

			view.m30(0);
			view.m31(0);
			view.m32(0);

			tmp.set(projectionMatrix);
			tmp.mul(view).mul(skyBox.getModelMatrix());

			skyBoxShaderProgram.setMatrix4f("projModelViewMatrix", tmp);

			// Fragment shader
			skyBoxShaderProgram.setVector2f("screenSize", gbuffer.getWidth(), gbuffer.getHeight());
			skyBoxShaderProgram.setVector3f("ambientLight", scene.getSceneLight().getAmbientLight());

			skyBoxShaderProgram.setFloat("blendFactor", skyBox.getBlendFactor());

			skyBox.getMesh().render();

			skyBoxShaderProgram.unbind();
		}

	}

	public void renderLight(final Matrix4f projectionMatrix, final Matrix4f invProjectionMatrix,
			final Matrix4f viewMatrix, final Scene scene, final boolean sceneChanged) {
		glEnable(GL_BLEND);
		glBlendEquation(GL_FUNC_ADD);
		glBlendFunc(GL_ONE, GL_ONE);

		final SceneLight sceneLight = scene.getSceneLight();

		final DirectionalLight dirLight = sceneLight.getDirectionalLight();
		final List<PointLight> pointLights = sceneLight.getPointLights();

		lightShaderProgram.bind();

		lightShaderProgram.setDirectionalLight("directionalLight", dirLight);

		lightShaderProgram.setMatrix4f("invProjectionMatrix", invProjectionMatrix);

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

		lightShaderProgram.setFloat("specularPower", specularPower);

		lightShaderProgram.setFog("fog", scene.getFog());

		final int[] textureIds = gbufferRenderer.getGBuffer().getTextureIds();
		for (int i = 0; i < GBuffer.TOTAL_TEXTURES; i++) {
			glActiveTexture(GL_TEXTURE0 + i);
			glBindTexture(GL_TEXTURE_2D, textureIds[i]);
		}

		// Toute les textures
		lightShaderProgram.setInt("depthText", 0);
		lightShaderProgram.setInt("diffuseText", 1);
		lightShaderProgram.setInt("specularText", 2);
		lightShaderProgram.setInt("normalsText", 3);
		lightShaderProgram.setInt("shadowText", 4);

		PostProcessingStep.quad.render();

		lightShaderProgram.unbind();

		glDisable(GL_BLEND);
	}

	public void cleanup() {
		glDeleteQueries(timeQuery);

		if (shadowRenderer != null)
			shadowRenderer.cleanup();

		if (skyBoxShaderProgram != null)
			skyBoxShaderProgram.cleanup();

		if (lightShaderProgram != null)
			lightShaderProgram.cleanup();

		if (particlesShaderProgram != null)
			particlesShaderProgram.cleanup();

		if (gbufferRenderer != null)
			gbufferRenderer.cleanUp();

		if (postProcessingRenderer != null)
			postProcessingRenderer.cleanUp();
	}
}
