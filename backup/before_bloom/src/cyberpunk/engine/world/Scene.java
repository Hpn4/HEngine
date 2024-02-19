package hengine.engine.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.AnimatedMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.graph.weather.Fog;
import hengine.engine.item.GameItem;
import hengine.engine.item.SkyBox;
import hengine.engine.particles.IParticleEmitter;

public class Scene {

	private final Map<AbstractMesh, List<GameItem>> meshMap;

	private final Map<InstancedMesh, List<GameItem>> instancedMeshMap;

	private final Map<AnimatedMesh, List<GameItem>> animMap;

	private final List<GameItem> gameItems;

	private Fog fog;

	private SkyBox skyBox;

	private SceneLight sceneLight;

	private boolean renderShadows;

	private IParticleEmitter[] particleEmitters;

	public Scene() {
		meshMap = new HashMap<>();
		instancedMeshMap = new HashMap<>();
		animMap = new HashMap<>();
		
		gameItems = new ArrayList<>();

		fog = Fog.NOFOG;
		renderShadows = true;
	}

	public void setGameItems(final GameItem[] gameItems) {
		final int numGameItems = gameItems != null ? gameItems.length : 0;

		for (int i = 0; i < numGameItems; i++) {
			final GameItem gameItem = gameItems[i];
			final AbstractMesh[] meshes = gameItem.getMeshes();
			for (final AbstractMesh mesh : meshes) {

				final boolean instancedMesh = mesh instanceof InstancedMesh,
						animatedMesh = mesh instanceof AnimatedMesh;
				List<GameItem> list = null;

				if (instancedMesh)
					list = instancedMeshMap.get(mesh);
				else if (animatedMesh)
					list = animMap.get(mesh);
				else
					list = meshMap.get(mesh);

				if (list == null) {
					list = new ArrayList<>();
					if (instancedMesh)
						instancedMeshMap.put((InstancedMesh) mesh, list);
					else if (animatedMesh)
						animMap.put((AnimatedMesh) mesh, list);
					else
						meshMap.put(mesh, list);

				}
				list.add(gameItem);

			}

			// gameItems[i].updateBox3D();

			this.gameItems.add(gameItems[i]);

			gameItem.updateBox3D();
		}
	}

	public void cleanup() {
		for (final AbstractMesh mesh : meshMap.keySet())
			mesh.cleanUp();

		for (final Mesh mesh : instancedMeshMap.keySet())
			mesh.cleanUp();

		for (final AnimatedMesh mesh : animMap.keySet())
			mesh.cleanUp();

		if (particleEmitters != null)
			for (final IParticleEmitter particleEmitter : particleEmitters)
				particleEmitter.cleanup();

	}

	public Map<AbstractMesh, List<GameItem>> getGameMeshes() {
		return meshMap;
	}

	public Map<InstancedMesh, List<GameItem>> getGameInstancedMeshes() {
		return instancedMeshMap;
	}

	public Map<AnimatedMesh, List<GameItem>> getAnimatedMeshes() {
		return animMap;
	}
	
	public List<GameItem> getGameItems() {
		return gameItems;
	}
	
	public boolean isRenderShadows() {
		return renderShadows;
	}

	public Fog getFog() {
		return fog;
	}

	public void setFog(final Fog fog) {
		this.fog = fog;
	}

	public SkyBox getSkyBox() {
		return skyBox;
	}

	public void setSkyBox(final SkyBox skyBox) {
		this.skyBox = skyBox;
	}

	public SceneLight getSceneLight() {
		return sceneLight;
	}

	public void setSceneLight(final SceneLight sceneLight) {
		this.sceneLight = sceneLight;
	}

	public void setRenderShadows(boolean renderShadows) {
		this.renderShadows = renderShadows;
	}

	public IParticleEmitter[] getParticleEmitters() {
		return particleEmitters;
	}

	public void setParticleEmitters(IParticleEmitter[] particleEmitters) {
		this.particleEmitters = particleEmitters;
	}
}
