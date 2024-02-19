package hengine.engine.item;

import hengine.engine.graph.Transformation;
import hengine.engine.graph.light.Material;
import hengine.engine.graph.mesh.SkyboxMesh;
import hengine.engine.utils.loader.StaticMeshesLoader;
import hengine.engine.utils.loader.texture.TextureCache;
import hengine.engine.utils.loader.texture.TextureCube;

public class SkyBox extends GameItem {

	private static final String[] fileNames = { "right", "left", "top", "bottom", "back", "front" };

	private float blendFactor;

	private boolean reverse;

	public SkyBox(final String objModel, final String textureDir) throws Exception {
		super();
		final SkyboxMesh skyBoxMesh = StaticMeshesLoader.loadSkybox(objModel, "");

		final TextureCube skyBoxTexture = getTextureCube(textureDir),
				secondSkyBoxTexture = getTextureCube("skybox/night/");

		skyBoxMesh.setMaterial(new Material(skyBoxTexture, 0.0f));

		skyBoxMesh.setSecondSky(secondSkyBoxTexture);
		setMesh(skyBoxMesh);

		setScale(80);

		modelMatrix.set(Transformation.buildModelMatrix(this));
	}

	public TextureCube getTextureCube(final String textureDir) throws Exception {
		final String[] files = new String[fileNames.length];
		for (int i = 0; i < fileNames.length; i++)
			files[i] = textureDir + "/" + fileNames[i] + ".png";

		final TextureCube skyBoxTexture = new TextureCube(files);
		TextureCache.putTexture(textureDir, skyBoxTexture);

		return skyBoxTexture;
	}

	public void update(final float interval) {
		setRotation(0, interval / 2, 0);

		final float time = interval / 20;
		blendFactor = blendFactor + (reverse ? -time : time);

		if (blendFactor >= 1)
			reverse = true;
		if (blendFactor <= 0)
			reverse = false;

		modelMatrix.set(Transformation.buildModelMatrix(this));
	}

	public float getBlendFactor() {
		return blendFactor;
	}
}
