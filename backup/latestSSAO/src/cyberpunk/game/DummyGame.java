package hengine.game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import org.joml.Vector3f;

import hengine.engine.IGameLogic;
import hengine.engine.MouseInput;
import hengine.engine.graph.Camera;
import hengine.engine.graph.RendererOption;
import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.graph.light.PointLight;
import hengine.engine.graph.renderer.Renderer;
import hengine.engine.graph.renderer.postProcessing.step.PostProcessingStep;
import hengine.engine.graph.weather.Fog;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.utils.loader.MapLoader;
import hengine.engine.utils.loader.texture.TextureCache;
import hengine.engine.world.Scene;
import hengine.engine.world.SceneLight;
import hengine.engine.world.item.SkyBox;

public class DummyGame implements IGameLogic {

	private final Renderer renderer;

	private final Camera camera;

	private Scene scene;

	private float angleInc, lightAngle;

	private boolean firstTime;

	private final RendererOption option;

	public DummyGame() {
		option = new RendererOption();
		renderer = new Renderer();
		camera = new Camera();
		firstTime = true;
		angleInc = 0;
		lightAngle = 90;
	}

	@Override
	public void init(final HWindow window) throws Exception {
		renderer.init(window);

		scene = new Scene();

		/*
		 * final int instancedMesh = 1000; final GameItem[] items = new
		 * GameItem[instancedMesh]; final InstancedMesh[] loadedMeshes =
		 * StaticMeshesLoader.loadInstanced("models/cyberpunk/scene.gltf",
		 * "models/cyberpunk", instancedMesh);
		 * 
		 * final InstancedMesh[] meshes = new InstancedMesh[3]; meshes[0] =
		 * loadedMeshes[1]; meshes[1] = loadedMeshes[4]; meshes[2] = loadedMeshes[5];
		 * 
		 * final GameItem item = new GameItem(meshes); item.setRotation(-90, 0, 0);
		 * 
		 * for (int i = 0; i < instancedMesh; i++) { float sign = Math.random() < 0.5 ?
		 * -1f : 1f; final float x = (float) (Math.random() * sign * 20);
		 * 
		 * sign = Math.random() < 0.5 ? -1f : 1f; final float y = (float) (Math.random()
		 * * sign * 20);
		 * 
		 * sign = Math.random() < 0.5 ? -1f : 1f; final float z = (float) (Math.random()
		 * * sign * 20);
		 * 
		 * items[i] = new GameItem(item); items[i].setPosition(x, y, z); }
		 * 
		 * scene.setGameItems(items);
		 */

		scene.setGameItems(MapLoader.loadMap("map.txt"));

		/**
		 * final GameItem item = new
		 * GameItem(StaticMeshesLoader.load("models/cyberpunk/scene.gltf",
		 * "models/cyberpunk")); final AbstractMesh[] m = item.getMeshes(); final
		 * GameItem items = new GameItem(m[1], m[3], m[4], m[6], m[7], m[8], m[9],
		 * m[10], m[11], m[12], m[13], m[14], m[15]);
		 * 
		 * items.setRotation(-90, 0, 0); m[0].cleanup(); m[2].cleanup(); m[5].cleanup();
		 * m[16].cleanup();
		 * 
		 * scene.setGameItems(items);
		 */

		// Fog
		final Vector3f fogColour = new Vector3f(0.8f, 0.8f, 0.8f);
		scene.setFog(new Fog(true, fogColour, 0.008f));

		// scene.setRenderShadows(false);

		// Setup SkyBox
		SkyBox skyBox = new SkyBox("models/skybox.obj", "skybox/day");
		scene.setSkyBox(skyBox);

		// Setup Lights
		setupLights();

		// Camera pos and orientation
		/*
		 * camera.getPosition().y = 3f; camera.getPosition().z = 0f;
		 * camera.getPosition().x = 0f; camera.getRotation().y = 90f;
		 */

		camera.getPosition().y = 5f;
		camera.getPosition().z = 10f;
		camera.getPosition().x = -2;

		camera.getRotation().z = 90f;
		
		scene.activeSSAO(false);

		/**int maxParticles = 200;
		Vector3f particleSpeed = new Vector3f(0, 5, 0);
		long ttl = 4000;
		long creationPeriodMillis = 100;
		float range = 0.5f;
		float scale = 1.0f;

		final AbstractMesh mesh = new ParticleMesh(maxParticles);
		AbstractTexture particleTexture = TextureCache.getTexture("particle.png");
		Material partMaterial = new Material(particleTexture, 1);
		mesh.setMaterial(partMaterial);
		Particle particle = new Particle(mesh, particleSpeed, ttl, 100);
		particle.setScale(scale);
		particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodMillis);
		particleEmitter.setActive(true);
		particleEmitter.setPositionRndRange(range);
		particleEmitter.setSpeedRndRange(range);
		particleEmitter.setAnimRange(10);
		this.scene.setParticleEmitters(new FlowParticleEmitter[] { particleEmitter });*/

		window.setVisible(true);
	}

	private void setupLights() {
		final SceneLight sceneLight = new SceneLight();
		scene.setSceneLight(sceneLight);

		// Ambient Light
		sceneLight.setAmbientLight(new Vector3f(.7f, .7f, .7f));
		sceneLight.setSkyBoxLight(new Vector3f(.5f, .5f, .5f));

		// Directional Light
		float lightIntensity = 1.0f;
		final Vector3f lightDirection = new Vector3f(1, 1, 0);
		final DirectionalLight directionalLight = new DirectionalLight(new Vector3f(2, 2, 2), lightDirection,
				lightIntensity);
		sceneLight.setDirectionalLight(directionalLight);

		final Vector3f lightColor = new Vector3f(0.8f, 0.8f, 0.8f);
		int x = -30;
		for (int i = 0; i < 4; i++) {
			final PointLight light = new PointLight(lightColor, new Vector3f(x, 8.8f, -0.5f), 2f);
			x += 20;
			sceneLight.addPointLight(light);
		}
	}

	@Override
	public void input(final HWindow window, final MouseInput mouseInput) {
		boolean sceneChanged = camera.input(window);

		if (window.isKeyPressed(GLFW_KEY_LEFT)) {
			sceneChanged = true;
			angleInc -= 0.05f;
		} else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
			sceneChanged = true;
			angleInc += 0.05f;
		} else
			angleInc = 0;

		if (window.isKeyReleassed(GLFW_KEY_O))
			option.outPutGBUInfo = !option.outPutGBUInfo;

		if (window.isKeyReleassed(GLFW_KEY_E))
			option.applyExposure = !option.applyExposure;

		if (window.isKeyReleassed(GLFW_KEY_F))
			scene.activeSSAO(!scene.isSSAOActive());

		if (window.isKeyReleassed(GLFW_KEY_ESCAPE))
			glfwSetWindowShouldClose(window.getWindowHandle(), true);

		if (window.isKeyPressed(GLFW_KEY_UP)) {
			sceneChanged = true;

			final float r = (float) Math.random(), g = (float) Math.random(), b = (float) Math.random();

			float sign = Math.random() < 0.5 ? -1f : 1f;
			final float x = (float) (Math.random() * 10 * sign);

			sign = Math.random() < 0.5 ? -1f : 1f;
			final float y = (float) (Math.random() * 10 * sign);

			sign = Math.random() < 0.5 ? -1f : 1f;
			final float z = (float) (Math.random() * 10 * sign);

			final PointLight point = new PointLight(new Vector3f(r, g, b), new Vector3f(x, y, z), 1f);

			if (scene.getSceneLight().getPointLights().size() < Renderer.MAX_POINT_LIGHTS)
				scene.getSceneLight().addPointLight(point);
		}

		option.sceneChanged = sceneChanged;
	}

	private Vector3f prevPos = new Vector3f();

	@Override
	public void update(final float interval, final MouseInput mouseInput, final HWindow window) {

		// Update camera position

		prevPos.set(camera.getPosition());
		lightAngle += angleInc;
		// car.addX(0.01f);
		if (lightAngle < 0)
			lightAngle = 0;
		else if (lightAngle > 200)
			lightAngle = 200;

		final float zValue = (float) Math.cos(Math.toRadians(lightAngle)),
				yValue = (float) Math.sin(Math.toRadians(lightAngle));

		final Vector3f lightDirection = scene.getSceneLight().getDirectionalLight().getDirection();

		lightDirection.set(0, yValue, zValue).normalize();
		// float lightAngle = (float) Math.toDegrees(Math.acos(lightDirection.z));

		scene.getSkyBox().update(interval);
		//particleEmitter.update((long) (interval * 1000));

		option.sceneChanged = camera.update(mouseInput, option.sceneChanged);
		option.sceneChanged = true;

		/**for (List<GameItem> items : scene.getGameMeshes().values())
			for (GameItem item : items)
				for (Box3D box : item.getBox3D())
					if (box.isInside(camera.getPosition()))
						camera.getPosition().set(prevPos);*/
	}

	@Override
	public void render(final HWindow window) {
		if (firstTime) {
			option.sceneChanged = true;
			firstTime = false;
		}

		if (window.isResized()) {
			window.updateProjectionMatrix();
			window.setResized(false);
		}

		renderer.render(window, camera, scene, option);

		window.render();
	}

	@Override
	public void cleanup(final HWindow window) {
		renderer.cleanup();
		scene.cleanup();

		TextureCache.cleanup();
		PostProcessingStep.quad.cleanup();
	}
}