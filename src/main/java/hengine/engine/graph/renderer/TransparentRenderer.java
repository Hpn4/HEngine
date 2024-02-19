package hengine.engine.graph.renderer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import hengine.engine.graph.Options;
import hengine.engine.graph.ShaderProgram;
import hengine.engine.graph.light.PointLight;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.renderer.shadow.ShadowCascade;
import hengine.engine.graph.renderer.shadow.ShadowRenderer;
import hengine.engine.world.Scene;
import hengine.engine.world.SceneLight;
import hengine.engine.world.item.GameItem;

public class TransparentRenderer {

	private ShaderProgram transparentShaderProgram;

	private final ArrayList<GameItem> filteredItems;

	public TransparentRenderer() throws Exception {
		filteredItems = new ArrayList<>();

		setupTransparentShader();
	}

	private void setupTransparentShader() throws Exception {
		transparentShaderProgram = new ShaderProgram("geometry/transparent");

		transparentShaderProgram.createUniform("isInstanced");

		transparentShaderProgram.createUniform("projectionViewMatrix");
		transparentShaderProgram.createUniform("modelNonInstancedMatrix");
		transparentShaderProgram.createUniform("normalNonInstancedMatrix");

		transparentShaderProgram.createUniform("invProjectionMatrix");

		// Create uniforms for texture sampler
		transparentShaderProgram.createUniform("diffuseMap", "normalMap");

		// Create uniform for material
		//transparentShaderProgram.createMaterialUniform("material");

		// Create uniforms for shadow mapping
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++)
			transparentShaderProgram.createUniform("shadowMap_" + i);

		transparentShaderProgram.createUniform("orthoProjLightViewMatrix", ShadowRenderer.NUM_CASCADES);
		transparentShaderProgram.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
		transparentShaderProgram.createUniform("renderShadow");

		transparentShaderProgram.createDirectionalLightUniform("directionalLight");
		transparentShaderProgram.createPointLightListUniform("pointLights", Renderer.MAX_POINT_LIGHTS);

		transparentShaderProgram.createFogUniform("fog");
	}

	public void render(final Matrix4f projectionViewMatrix, final Matrix4f invProjectionMatrix,
			final Matrix4f viewMatrix, final Scene scene, final ShadowRenderer shadowRenderer) {

		// Set settings
		glEnable(GL_BLEND);
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_COLOR);

		// Setup shader's uniform
		transparentShaderProgram.bind();

		transparentShaderProgram.setMatrix4f("projectionViewMatrix", projectionViewMatrix);

		final List<ShadowCascade> shadowCascades = shadowRenderer.getShadowCascades();
		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
			final ShadowCascade shadowCascade = shadowCascades.get(i);
			transparentShaderProgram.setMatrix4fAtIndex("orthoProjLightViewMatrix",
					shadowCascade.getOrthoProjLightViewMatrix(), i);
			transparentShaderProgram.setFloatAtIndex("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
		}

		int textureBlock = 0;
		transparentShaderProgram.setInt("diffuseMap", textureBlock++);
		transparentShaderProgram.setInt("normalMap", textureBlock++);

		for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++)
			transparentShaderProgram.setInt("shadowMap_" + i, textureBlock++);

		transparentShaderProgram.setBoolean("renderShadow", Options.renderShadows);

		// Uniforms for light
		transparentShaderProgram.setMatrix4f("invProjectionMatrix", invProjectionMatrix);

		final SceneLight sceneLight = scene.getSceneLight();
		transparentShaderProgram.setDirectionalLight("directionalLight", sceneLight.getDirectionalLight());

		final List<PointLight> pointLights = sceneLight.getPointLights();
		final int numLights = pointLights.size();
		for (int i = 0; i < numLights; i++) {

			// Get a copy of the point light object and transform its position to view
			// coordinates
			final PointLight currPointLight = new PointLight(pointLights.get(i));
			final Vector4f lightPos = new Vector4f(currPointLight.getPosition(), 1);
			lightPos.mul(viewMatrix);

			currPointLight.getPosition().set(lightPos.x, lightPos.y, lightPos.z);

			transparentShaderProgram.setPointLightAtIndex("pointLights", currPointLight, i);
		}

		transparentShaderProgram.setFog("fog", scene.getFog());

		// Render meshes
		renderTransparentNonInstancedMeshes(scene);
		
		// Render instanced mehes
		renderTransparentInstancedMeshes(scene, viewMatrix);
		
		// Reset settings
		transparentShaderProgram.unbind();
		glDisable(GL_BLEND);
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
	}

	private void renderTransparentNonInstancedMeshes(final Scene scene) {
		transparentShaderProgram.setBoolean("isInstanced", false);

		final Map<AbstractMesh, List<GameItem>> mapMeshes = scene.getTransparentGameMeshes();
		for (final AbstractMesh mesh : mapMeshes.keySet()) {
			transparentShaderProgram.setMaterial("material", mesh.getMaterial());

			mesh.renderList(mapMeshes.get(mesh), (final GameItem gameItem) -> {
				transparentShaderProgram.setMatrix3f("normalNonInstancedMatrix", gameItem.getNormalMatrix());
				transparentShaderProgram.setMatrix4f("modelNonInstancedMatrix", gameItem.getModelMatrix());
			});
		}
	}

	private void renderTransparentInstancedMeshes(final Scene scene, final Matrix4f viewMatrix) {
		transparentShaderProgram.setBoolean("isInstanced", true);

		final Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getTransparentGameInstancedMeshes();
		for (final InstancedMesh mesh : mapMeshes.keySet()) {
			transparentShaderProgram.setMaterial("material", mesh.getMaterial());

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

	public void cleanup() {
		if (transparentShaderProgram != null)
			transparentShaderProgram.cleanup();
	}
}
