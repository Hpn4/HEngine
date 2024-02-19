package hengine.game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import hengine.engine.IGameLogic;
import hengine.engine.MouseInput;
import hengine.engine.graph.AbstractTexture;
import hengine.engine.graph.Camera;
import hengine.engine.graph.Renderer;
import hengine.engine.graph.anime.Animation;
import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.graph.light.Material;
import hengine.engine.graph.light.PointLight;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.ParticleMesh;
import hengine.engine.graph.weather.Fog;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.item.GameItem;
import hengine.engine.item.SkyBox;
import hengine.engine.particles.FlowParticleEmitter;
import hengine.engine.particles.Particle;
import hengine.engine.utils.Box3D;
import hengine.engine.utils.loader.StaticMeshesLoader;
import hengine.engine.utils.loader.TextureCache;
import hengine.engine.world.Scene;
import hengine.engine.world.SceneLight;

public class DummyGame implements IGameLogic {

	private static final float MOUSE_SENSITIVITY = 0.2f;

	private static final float CAMERA_POS_STEP = 0.1f;

	private final Vector3f cameraInc;

	private final Renderer renderer;

	private final Camera camera;

	private Scene scene;

	private float angleInc, lightAngle;

	private Animation animation;

	private FlowParticleEmitter particleEmitter;

	private boolean firstTime;

	public boolean sceneChanged;

	public DummyGame() {
		renderer = new Renderer();
		camera = new Camera();
		cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
		firstTime = true;
		angleInc = 0;
		lightAngle = 90;
	}

	@Override
	public void init(final HWindow window) throws Exception {
		renderer.init(window);

		scene = new Scene();

		final int numInstances = 100;
		final GameItem item = new GameItem(StaticMeshesLoader.loadInstanced("models/cyberpunk/scene.gltf", "models/cyberpunk/", numInstances));
		item.setRotation(-90, 0, 0);
		item.setScale(0.5f);
		
		final GameItem[] items = new GameItem[numInstances];
		
		for(int i = 0; i < numInstances; i++) {
			float sign = Math.random() < 0.5 ? -1f : 1f;
			final float x = (float) (Math.random() * 10 * sign);
			
			sign = Math.random() < 0.5 ? -1f : 1f;
			final float y = (float) (Math.random() * 10 * sign);
			
			sign = Math.random() < 0.5 ? -1f : 1f;
			final float z = (float) (Math.random() * 10 * sign);
			
			items[i] = new GameItem(item);
			items[i].setPosition(x, y, z);
		}
		
		scene.setGameItems(items);

		camera.getRotation().z = 0f;
		camera.getRotation().y = 0.f;

		// Fog
		final Vector3f fogColour = new Vector3f(0.5f, 0.5f, 0.5f);
		scene.setFog(new Fog(true, fogColour, 0.02f));

		scene.setRenderShadows(true);

		// Setup SkyBox
		SkyBox skyBox = new SkyBox("models/skybox.obj", "skybox/day");
		scene.setSkyBox(skyBox);

		// Setup Lights
		setupLights();

		camera.getPosition().y = 3f;
		camera.getPosition().z = 3f;
		camera.getPosition().x = -7f;
		camera.getRotation().x = 15.0f;
		camera.getRotation().y = 400.0f;

		int maxParticles = 200;
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
		this.scene.setParticleEmitters(new FlowParticleEmitter[] { particleEmitter });

		window.setVisible(true);
	}

	private void setupLights() {
		SceneLight sceneLight = new SceneLight();
		scene.setSceneLight(sceneLight);

		// Ambient Light
		sceneLight.setAmbientLight(new Vector3f(.5f, .5f, .5f));
		sceneLight.setSkyBoxLight(new Vector3f(.5f, .5f, .5f));

		// Directional Light
		float lightIntensity = 1.0f;
		Vector3f lightDirection = new Vector3f(1, 1, 0);
		DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
		sceneLight.setDirectionalLight(directionalLight);
	}

	@Override
	public void input(final HWindow window, final MouseInput mouseInput) {
		sceneChanged = false;
		cameraInc.set(0, 0, 0);
		if (window.isKeyPressed(GLFW_KEY_W)) {
			sceneChanged = true;
			cameraInc.z = -1;
		} else if (window.isKeyPressed(GLFW_KEY_S)) {
			sceneChanged = true;
			cameraInc.z = 1;
		}
		if (window.isKeyPressed(GLFW_KEY_A)) {
			sceneChanged = true;
			cameraInc.x = -1;
		} else if (window.isKeyPressed(GLFW_KEY_D)) {
			sceneChanged = true;
			cameraInc.x = 1;
		}
		if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
			sceneChanged = true;
			cameraInc.y = -1;
		} else if (window.isKeyPressed(GLFW_KEY_SPACE)) {
			sceneChanged = true;
			cameraInc.y = 1;
		}
		if (window.isKeyPressed(GLFW_KEY_LEFT)) {
			sceneChanged = true;
			angleInc -= 0.05f;
		} else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
			sceneChanged = true;
			angleInc += 0.05f;
		} else
			angleInc = 0;
		
		if (window.isKeyPressed(GLFW_KEY_UP)) {
			sceneChanged = true;

			final float r = (float) Math.random(), g = (float) Math.random(), b = (float) Math.random();

			float sign = Math.random() < 0.5 ? -1f : 1f;
			final float x = (float) (Math.random() * 10 * sign);

			sign = Math.random() < 0.5 ? -1f : 1f;
			final float y = (float) (Math.random() * 10 * sign);

			sign = Math.random() < 0.5 ? -1f : 1f;
			final float z = (float) (Math.random() * 10 * sign);

			final float intensity = (float) Math.random();
			final PointLight point = new PointLight(new Vector3f(r, g, b), new Vector3f(x, y, z), intensity);

			if (scene.getSceneLight().getPointLights().size() < Renderer.MAX_POINT_LIGHTS)
				scene.getSceneLight().addPointLight(point);
		}

		if (window.isKeyPressed(GLFW_KEY_Q)) {
			sceneChanged = true;
			animation.nextFrame();
		}
	}

	@Override
	public void update(final float interval, final MouseInput mouseInput, final HWindow window) {
		// Update camera based on mouse
		if (mouseInput.isRightButtonPressed()) {
			final Vector2f rotVec = mouseInput.getDisplVec();
			camera.rotate(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
			sceneChanged = true;
		}

		// Update camera position
		final Vector3f prevPos = new Vector3f(camera.getPosition());
		camera.translate(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

		for (List<GameItem> items : scene.getGameMeshes().values())
			for (GameItem item : items)
				for (Box3D box : item.getBox3D())
					if (box.isInside(camera.getPosition()))
						camera.setPosition(prevPos.x, prevPos.y, prevPos.z);

		lightAngle += angleInc;
		if (lightAngle < 0)
			lightAngle = 0;
		else if (lightAngle > 180)
			lightAngle = 180;

		if (angleInc != 0) {
			float zValue = (float) Math.cos(Math.toRadians(lightAngle));
			float yValue = (float) Math.sin(Math.toRadians(lightAngle));
			final Vector3f lightDirection = scene.getSceneLight().getDirectionalLight().getDirection();
			lightDirection.x = 0;
			lightDirection.y = yValue;
			lightDirection.z = zValue;
			lightDirection.normalize();
			// float lightAngle = (float) Math.toDegrees(Math.acos(lightDirection.z));
		}

		scene.getSkyBox().update(interval);
		particleEmitter.update((long) (interval * 1000));
		camera.updateViewMatrix();
	}

	@Override
	public void render(final HWindow window) {
		if (firstTime) {
			sceneChanged = true;
			firstTime = false;
		}

		if (window.isResized()) {
			window.updateProjectionMatrix();
			window.setResized(false);
		}

		renderer.render(window, camera, scene, sceneChanged);

		window.render();
	}

	@Override
	public void cleanup() {
		renderer.cleanup();
		scene.cleanup();

		TextureCache.cleanUp();
	}
}
