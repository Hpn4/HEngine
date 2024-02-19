package hengine.engine.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.AnimatedMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.graph.particles.IParticleEmitter;
import hengine.engine.graph.weather.Fog;
import hengine.engine.world.item.GameItem;
import hengine.engine.world.item.SkyBox;

public class Scene {

	private final Map<AbstractMesh, List<GameItem>> meshMap;

	private final Map<AbstractMesh, List<GameItem>> transparentMeshMap;

	private final Map<InstancedMesh, List<GameItem>> instancedMeshMap;

	private final Map<InstancedMesh, List<GameItem>> transparentInstancedMeshMap;

	private final Map<AnimatedMesh, List<GameItem>> animMap;

	private final List<GameItem> gameItems;

	private Fog fog;

	private SkyBox skyBox;

	private SceneLight sceneLight;

	private final List<IParticleEmitter> particleEmitters;

	private final List<IParticleEmitter> animParticleEmitters;

	public Scene() {
		meshMap = new HashMap<>();
		transparentMeshMap = new HashMap<>();
		instancedMeshMap = new HashMap<>();
		transparentInstancedMeshMap = new HashMap<>();
		animMap = new HashMap<>();

		gameItems = new ArrayList<>();

		particleEmitters = new ArrayList<>();
		animParticleEmitters = new ArrayList<>();

		fog = Fog.NOFOG;
	}

	public void addGameItems(final GameItem... gameItems) {
		final int numGameItems = gameItems != null ? gameItems.length : 0;

		for (int i = 0; i < numGameItems; i++) {
			final GameItem gameItem = gameItems[i];
			final AbstractMesh[] meshes = gameItem.getMeshes();
			for (final AbstractMesh mesh : meshes) {

				final boolean instancedMesh = mesh instanceof InstancedMesh,
						animatedMesh = mesh instanceof AnimatedMesh;
				final boolean isTransparent = mesh.getMaterial().isTransparent();
				List<GameItem> list = null;

				if (isTransparent) {
					if (instancedMesh)
						list = transparentInstancedMeshMap.get(mesh);
					else
						list = transparentMeshMap.get(mesh);
				} else if (instancedMesh)
					list = instancedMeshMap.get(mesh);
				else if (animatedMesh)
					list = animMap.get(mesh);
				else
					list = meshMap.get(mesh);

				if (list == null) {
					list = new ArrayList<>();
					if (isTransparent) {
						if (instancedMesh)
							transparentInstancedMeshMap.put((InstancedMesh) mesh, list);
						else
							transparentMeshMap.put(mesh, list);
					} else if (instancedMesh)
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

	public void sort(final Matrix4f viewMatrix) {
		for (final List<GameItem> list : instancedMeshMap.values()) {
			for (final GameItem item : list)
				item.updateViewSpaceDepth(viewMatrix);
			list.sort(null);
		}

	}

	public void cleanup() {
		for (final AbstractMesh mesh : meshMap.keySet())
			mesh.cleanup();

		for (final AbstractMesh mesh : transparentMeshMap.keySet())
			mesh.cleanup();

		for (final Mesh mesh : instancedMeshMap.keySet())
			mesh.cleanup();

		for (final Mesh mesh : transparentInstancedMeshMap.keySet())
			mesh.cleanup();

		for (final AnimatedMesh mesh : animMap.keySet())
			mesh.cleanup();

		for (final IParticleEmitter particleEmitter : particleEmitters)
			particleEmitter.cleanup();

		for (final IParticleEmitter particleEmitter : animParticleEmitters)
			particleEmitter.cleanup();

	}

	public Map<AbstractMesh, List<GameItem>> getGameMeshes() {
		return meshMap;
	}

	public Map<AbstractMesh, List<GameItem>> getTransparentGameMeshes() {
		return transparentMeshMap;
	}

	public Map<InstancedMesh, List<GameItem>> getGameInstancedMeshes() {
		return instancedMeshMap;
	}

	public Map<InstancedMesh, List<GameItem>> getTransparentGameInstancedMeshes() {
		return transparentInstancedMeshMap;
	}

	public Map<AnimatedMesh, List<GameItem>> getAnimatedMeshes() {
		return animMap;
	}

	public List<GameItem> getGameItems() {
		return gameItems;
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

	public List<IParticleEmitter> getParticleEmitters() {
		return particleEmitters;
	}

	public void addParticleEmitter(final IParticleEmitter particleEmitter) {
		particleEmitters.add(particleEmitter);
	}

	public List<IParticleEmitter> getAnimParticleEmitters() {
		return animParticleEmitters;
	}

	public void addAnimParticleEmitter(final IParticleEmitter particleEmitter) {
		animParticleEmitters.add(particleEmitter);
	}
}
