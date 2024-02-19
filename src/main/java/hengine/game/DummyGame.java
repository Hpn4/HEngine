package hengine.game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

import org.joml.Vector3f;

import hengine.engine.IGameLogic;
import hengine.engine.MouseInput;
import hengine.engine.graph.Camera;
import hengine.engine.graph.Options;
import hengine.engine.graph.RendererOption;
import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.graph.light.PointLight;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.graph.particles.FlowParticleEmitter;
import hengine.engine.graph.renderer.Renderer;
import hengine.engine.graph.renderer.postProcessing.step.PostProcessingStep;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.sound.SoundManager;
import hengine.engine.utils.loader.MapLoader;
import hengine.engine.utils.loader.StaticMeshesLoader;
import hengine.engine.utils.loader.texture.TextureCache;
import hengine.engine.world.Scene;
import hengine.engine.world.SceneLight;
import hengine.engine.world.item.Car;
import hengine.engine.world.item.GameItem;
import hengine.engine.world.item.SkyBox;
import hengine.game.ui.MainMenu;

public class DummyGame implements IGameLogic {

	private MainMenu menu;

	private final SoundManager soundM;

	private final Renderer renderer;

	private final Camera camera;

	private Scene scene;

	private float angleInc, lightAngle;

	private boolean firstTime;

	private final RendererOption option;

	private Car car;

	public DummyGame() {
		option = new RendererOption();
		renderer = new Renderer();
		camera = new Camera();
		firstTime = true;
		angleInc = 0;
		lightAngle = 90;
		Options.readOptions();
		soundM = new SoundManager();
	}

	@Override
	public void init(final HWindow window) throws Exception {
		renderer.init(window);
		soundM.init();

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

		scene.addGameItems(MapLoader.loadMap("map.txt"));

		/*
		 * Files.createFile(Paths.get("resources/models/cyberpunk/test.fbx"));
		 * StaticMeshesLoader2.loadAny("models/cyberpunk/scene.gltf",
		 * "models/cyberpunk"); final GameItem item = new
		 * GameItem(StaticMeshesLoader.load("models/cyberpunk/scene.gltf",
		 * "models/cyberpunk")); final AbstractMesh[] m = item.getMeshes(); final
		 * GameItem items = new GameItem(m[1], m[3], m[4], m[6], m[7], m[8], m[9],
		 * m[12], m[13], m[14], m[15]);
		 * 
		 * m[0].cleanup(); m[2].cleanup(); m[5].cleanup(); m[16].cleanup();
		 * 
		 * car = new Car(items, new GameItem(m[10]), new GameItem(m[11]), 1);
		 * 
		 * car.rotate(-90, 0, 90); scene.addGameItems(car.getItems());
		 */

		/**
		 * final GameItem voiture = new
		 * GameItem(StaticMeshesLoader.load("models/cyberpunk/voiture.dae",
		 * "models/cyberpunk")); final GameItem roueArriere = new
		 * GameItem(StaticMeshesLoader.load("models/cyberpunk/arriere.dae",
		 * "models/cyberpunk")); final GameItem roueAvant = new
		 * GameItem(StaticMeshesLoader.load("models/cyberpunk/avant.dae",
		 * "models/cyberpunk"));
		 * 
		 * car = new Car(voiture, roueAvant, roueArriere, 1);
		 * scene.addGameItems(car.getItems());
		 */

		// Fog
		// final Vector3f fogColour = new Vector3f(0.99f, 0.19f, 0.9f);
		// scene.setFog(new Fog(true, fogColour, 0.02f));

		// Setup SkyBox
		SkyBox skyBox = new SkyBox("models/skybox.obj", "skybox/night");
		scene.setSkyBox(skyBox);
		
		Mesh[] mesh = StaticMeshesLoader.load("models/cube2.obj","models/");
		scene.addGameItems(new GameItem(mesh));

		// Setup Lights
		setupLights();

		// Camera pos and orientation
		/*
		 * camera.getPosition().y = 3f; camera.getPosition().z = 0f;
		 * camera.getPosition().x = 0f; camera.getRotation().y = 90f;
		 */

		camera.getPosition().y = 1;
		camera.getPosition().z = 1f;
		camera.getPosition().x = 0;

		camera.getRotation().z = 90f;

		Options.activeSSAO = false;
		Options.renderShadows = true;

		
		 /* int maxParticles = 200; Vector3f particleSpeed = new Vector3f(0f, 0.8f, 0);
		  long ttl = 6000; long creationPeriodMillis = 10; float scale = .5f;
		  
		  final AbstractMesh mesh = new ParticleMesh(maxParticles); final
		  AbstractTexture particleTexture =
		  TextureCache.getTexture("particle/smoke.png", 5, 3); final Material
		  partMaterial = new Material(particleTexture, 1);
		  mesh.setMaterial(partMaterial);
		  
		  final Particle particle = new Particle(mesh, particleSpeed, ttl);
		 
		  particle.setPosition(1, 2, 1); particle.setScale(scale);
		  
		  particleEmitter = new FlowParticleEmitter(particle, maxParticles,
		  creationPeriodMillis); particleEmitter.setActive(true);
		  particleEmitter.setPositionRndRange(new Vector3f(1.5f, 0.5f, 1.5f));
		  particleEmitter.setSpeedRndRange(0.1f); particleEmitter.setScaleRndRange(1f);
		  particleEmitter.setAnimRange(50);
		  
		  particleEmitter.setStartColor(new Vector3f(1, 200 / 255f, 0f));
		  particleEmitter.setEndColor(new Vector3f(1, 20f / 255f, 20f / 255f));
		 
		  scene.addParticleEmitter(particleEmitter);
		  
		  final Particle particle1 = new Particle(mesh, particleSpeed, ttl);
		  particle1.setPosition(1, 3.5f, 1); particle1.setScale(scale);
		  
		  particleEmitter2 = new FlowParticleEmitter(particle1, 200,
		  creationPeriodMillis); particleEmitter2.setActive(true);
		  particleEmitter2.setPositionRndRange(new Vector3f(2.5f, 1.5f, 2.5f));
		  particleEmitter2.setSpeedRndRange(0.5f);
		  particleEmitter2.setScaleRndRange(1f); particleEmitter2.setAnimRange(50);
		  
		  scene.addParticleEmitter(particleEmitter2);*/
		 

		menu = new MainMenu(window);
		window.addComp(menu);
		window.setVisible(true);

		menu.setVisible(false);

		soundM.addSound("or", "feu.ogg", new Vector3f(0, 1, 1), true, false);
	}
	
	FlowParticleEmitter particleEmitter;
	
	FlowParticleEmitter particleEmitter2;

	private void setupLights() {
		final SceneLight sceneLight = new SceneLight();
		scene.setSceneLight(sceneLight);

		// Ambient Light
		sceneLight.setAmbientLight(new Vector3f(.5f, .5f, .5f));
		sceneLight.setSkyBoxLight(new Vector3f(.5f, .5f, .5f));

		// Directional Light
		float lightIntensity = 1.0f;
		final Vector3f lightDirection = new Vector3f(1, 1, 0);
		final DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection,
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
			Options.activeSSAO = !Options.activeSSAO;

		if (window.isKeyReleassed(GLFW_KEY_ESCAPE)) {
			soundM.playSoundSource("or");
			menu.openMenu(window);
		}

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
		if (!menu.isMenuOpen()) {
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

			option.sceneChanged = camera.update(mouseInput, option.sceneChanged);
			option.sceneChanged = true;

			// car.addX(0.01f);
		}

		/**
		 * for (List<GameItem> items : scene.getGameMeshes().values()) for (GameItem
		 * item : items) for (Box3D box : item.getBox3D()) if
		 * (box.isInside(camera.getPosition())) camera.getPosition().set(prevPos);
		 */

		soundM.updateListenerPosition(camera);
		
		//particleEmitter.update(1);
		//particleEmitter2.update(1);
	}

	@Override
	public void render(final HWindow window) {
		if (firstTime) {
			option.sceneChanged = true;
			firstTime = false;
		}

		renderer.render(window, camera, scene, option);

		window.render();
	}

	@Override
	public void cleanup(final HWindow window) {
		Options.writeOptions();
		renderer.cleanup();
		scene.cleanup();

		// car.cleanup();
		TextureCache.cleanup();
		PostProcessingStep.quad.cleanup();
		soundM.cleanup();
	}
}