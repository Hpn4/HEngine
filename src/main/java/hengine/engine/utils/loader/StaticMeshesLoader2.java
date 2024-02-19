package hengine.engine.utils.loader;

import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_DIFFUSE;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_EMISSIVE;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_SPECULAR;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_SHININESS;
import static org.lwjgl.assimp.Assimp.aiGetErrorString;
import static org.lwjgl.assimp.Assimp.aiGetMaterialColor;
import static org.lwjgl.assimp.Assimp.aiGetMaterialFloatArray;
import static org.lwjgl.assimp.Assimp.aiGetMaterialTexture;
import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_CalcTangentSpace;
import static org.lwjgl.assimp.Assimp.aiProcess_DropNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_FindDegenerates;
import static org.lwjgl.assimp.Assimp.aiProcess_FindInstances;
import static org.lwjgl.assimp.Assimp.aiProcess_FindInvalidData;
import static org.lwjgl.assimp.Assimp.aiProcess_GenNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_GenUVCoords;
import static org.lwjgl.assimp.Assimp.aiProcess_ImproveCacheLocality;
import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_OptimizeMeshes;
import static org.lwjgl.assimp.Assimp.aiProcess_RemoveRedundantMaterials;
import static org.lwjgl.assimp.Assimp.aiProcess_SortByPType;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;
import static org.lwjgl.assimp.Assimp.aiReturn_SUCCESS;
import static org.lwjgl.assimp.Assimp.aiTextureType_DIFFUSE;
import static org.lwjgl.assimp.Assimp.aiTextureType_EMISSIVE;
import static org.lwjgl.assimp.Assimp.aiTextureType_HEIGHT;
import static org.lwjgl.assimp.Assimp.aiTextureType_NONE;
import static org.lwjgl.assimp.Assimp.aiTextureType_NORMALS;
import static org.lwjgl.assimp.Assimp.aiTextureType_SPECULAR;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.system.MemoryStack;

import hengine.engine.graph.light.Material;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.graph.mesh.SkyboxMesh;
import hengine.engine.utils.Utils;
import hengine.engine.utils.loader.texture.ModelTextureCombiner;
import hengine.engine.utils.loader.texture.TextureCache;

public class StaticMeshesLoader2 {

	private final static int flags = aiProcess_FindInstances | aiProcess_FindInvalidData | aiProcess_OptimizeMeshes
			| aiProcess_CalcTangentSpace | aiProcess_GenNormals | aiProcess_JoinIdenticalVertices
			| aiProcess_ImproveCacheLocality | aiProcess_RemoveRedundantMaterials | aiProcess_Triangulate
			| aiProcess_GenUVCoords | aiProcess_SortByPType | aiProcess_FindDegenerates | aiProcess_FindInvalidData
			| aiProcess_DropNormals;

	protected static int nmbVertex, nmbTriangles;

	private enum MeshesType {
		SKYBOX, INSTANCED, DEFAULT;
	}

	public static void loadAny(final String resourcePath, final String texturesDir) throws Exception {
		final AIScene aiScene = aiImportFile(Utils.getPath(resourcePath), flags);

		if (aiScene == null) {
			System.err.println(aiGetErrorString());
			throw new Exception("Error loading model");
		}

		// On recup le seule mat intéressant
		final PointerBuffer aiMaterials = aiScene.mMaterials();
		final long ptrMat = aiMaterials.get(1);

		aiMaterials.clear();
		aiMaterials.put(0, ptrMat);
		aiMaterials.position(0);

		aiScene.mMaterials(aiMaterials);

		AIScene.nmNumMaterials(aiScene.address(), 1);

		// On récupère le 10ème element
		final PointerBuffer aiMeshes = aiScene.mMeshes();
		final long ptr = aiScene.mMeshes().get(11);
		
		final int[] recupM = {1, 3, 4, 6, 7, 8, 9, 12,13, 14, 15};
		for(int i : recupM) {
			final AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
			aiMesh.mMaterialIndex(0);
		}

		// On change l'index de la texture
		final AIMesh aiMesh = AIMesh.create(ptr);
		aiMesh.mMaterialIndex(0);

		// On modifie la liste des pointeurs
		final PointerBuffer copy = aiMeshes.duplicate();
		aiMeshes.put(0, copy.get(1));
		aiMeshes.put(1, copy.get(3));
		aiMeshes.put(2, copy.get(4));
		aiMeshes.put(3, copy.get(6));
		aiMeshes.put(4, copy.get(7));
		aiMeshes.put(5, copy.get(8));
		aiMeshes.put(6, copy.get(9));
		aiMeshes.put(7, copy.get(12));
		aiMeshes.put(8, copy.get(13));
		aiMeshes.put(9, copy.get(14));
		aiMeshes.put(10, copy.get(15));
		for(int i = 11; i < 17; i++)
			aiMeshes.put(i, copy.get(15));
		aiMeshes.position(0);

		aiScene.mMeshes(aiMeshes);

		//AIScene.nmNumMeshes(aiScene.address(), 1);
		System.err.println("SALUT" + aiScene.mNumMeshes());

		int a = Assimp.aiExportScene(aiScene, "collada", Utils.getPath(texturesDir + "/voiture.dae"),
				aiProcess_Triangulate);

		System.err.println(a + " " + aiGetErrorString());

		// Libere toute les ressources
		// aiReleaseImport(aiScene);
	}

	public void export() {
		final AIScene scene = AIScene.calloc();

		scene.mRootNode(AINode.calloc());

		final AIMaterial mat = AIMaterial.calloc();

		PointerBuffer aiMats = PointerBuffer.allocateDirect(1);
		aiMats.put(0, mat);
		aiMats.position(0);

		scene.mMaterials(aiMats);
	}

	protected static String getTexturePath(final AIMaterial aiMaterial, final int textureType, final String texturesDir)
			throws Exception {
		final AIString path = AIString.calloc();

		aiGetMaterialTexture(aiMaterial, textureType, 0, path, (IntBuffer) null, null, null, null, null, null);

		final String textPath = path.dataString();
		path.free();

		if (textPath != null && textPath.length() > 0) {
			String textureFile = "";

			if (texturesDir != null && texturesDir.length() > 0)
				textureFile += texturesDir + "/";

			textureFile = (textureFile + textPath).replace("//", "/");

			final String type = textureType == 1 ? "diffuse"
					: textureType == 2 ? "specular" : (textureType == 5 | textureType == 6) ? "normal" : "emissive";

			System.out.println("\t- " + type + "Map : " + textureFile);

			return textureFile;
		}

		return null;
	}

	protected static void processMaterial(final AIMaterial aiMaterial, final List<Material> materials,
			final String texturesDir, final int i) throws Exception {
		final AIColor4D colour = AIColor4D.create();

		// Diffuse map texture
		final String diffusePath = getTexturePath(aiMaterial, aiTextureType_DIFFUSE, texturesDir);

		// Specular map texture
		final String specularPath = getTexturePath(aiMaterial, aiTextureType_SPECULAR, texturesDir);

		// Emissive map texture
		final String emissivePath = getTexturePath(aiMaterial, aiTextureType_EMISSIVE, texturesDir);

		// Normal map texture
		String normalPath = getTexturePath(aiMaterial, aiTextureType_HEIGHT, texturesDir);

		if (normalPath == null)
			normalPath = getTexturePath(aiMaterial, aiTextureType_NORMALS, texturesDir);

		// Diffuse color
		Vector4f diffuse = Material.DEFAULT_COLOUR;
		int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, colour);
		if (result == aiReturn_SUCCESS)
			diffuse = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());

		// Specualar color
		float specular = 1;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, colour);
		if (result == aiReturn_SUCCESS)
			specular = colour.r(); // Specular is in grey scale so we preserve only one channel

		// Emissive color
		Vector3f emissive = Material.EMISSIVE_DEFAULT_COLOUR;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_EMISSIVE, aiTextureType_NONE, 0, colour);
		if (result == aiReturn_SUCCESS)
			emissive = new Vector3f(colour.r(), colour.g(), colour.b());

		// Shininess
		float shininess = 0;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer size = stack.ints(1);
			final FloatBuffer data = stack.floats(1);
			aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_SHININESS, aiTextureType_NONE, 0, data, size);
			shininess = data.get();
		}

		Material material;
		if (diffusePath != null) {
			final ModelTextureCombiner mtc = TextureCache.getModelTextureCombiner(diffusePath, normalPath, specularPath,
					emissivePath, texturesDir + i);
			material = new Material(diffuse, specular, emissive, mtc.getDiffuseMap(), mtc.getNormalMap(), shininess,
					mtc.isTransparent(), mtc.hasSpecularMap(), mtc.hasEmissiveMap());
		} else
			material = new Material(diffuse, specular, emissive, null, null, shininess, false, false, false);

		materials.add(material);
	}

	public static Material createMaterial(final String diffusePath) {
		ModelTextureCombiner mtc = null;
		try {
			mtc = TextureCache.getModelTextureCombiner(diffusePath, null, null, null, diffusePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Material(mtc.getDiffuseMap(), 0.4f);
	}

	protected static AbstractMesh processMesh(final AIMesh aiMesh, final List<Material> materials,
			final MeshesType type, final int numInstances) {
		final List<Float> vertices = new ArrayList<>(), textures = new ArrayList<>(), normals = new ArrayList<>();
		final List<Integer> indices = new ArrayList<>();

		processVertices(aiMesh, vertices);
		processNormals(aiMesh, normals);
		processTextCoords(aiMesh, textures);
		processIndices(aiMesh, indices);

		nmbVertex += vertices.size();

		AbstractMesh mesh = null;
		switch (type) {

		case DEFAULT:
			mesh = new Mesh(Utils.listToArray(vertices), Utils.listToArray(textures), Utils.listToArray(normals),
					Utils.listIntToArray(indices));
			break;
		case INSTANCED:
			mesh = new InstancedMesh(Utils.listToArray(vertices), Utils.listToArray(textures),
					Utils.listToArray(normals), Utils.listIntToArray(indices), numInstances);
			break;
		case SKYBOX:
			mesh = new SkyboxMesh(Utils.listToArray(vertices), Utils.listIntToArray(indices));
		}

		Material material;
		int materialIdx = aiMesh.mMaterialIndex();
		if (materialIdx >= 0 && materialIdx < materials.size())
			material = materials.get(materialIdx);
		else
			material = new Material();

		mesh.setMaterial(material);

		return mesh;
	}

	protected static void processVertices(final AIMesh aiMesh, final List<Float> vertices) {
		final AIVector3D.Buffer aiVertices = aiMesh.mVertices();
		while (aiVertices.remaining() > 0) {
			final AIVector3D aiVertex = aiVertices.get();
			vertices.add(aiVertex.x());
			vertices.add(aiVertex.y());
			vertices.add(aiVertex.z());
		}
	}

	protected static void processNormals(final AIMesh aiMesh, final List<Float> normals) {
		final AIVector3D.Buffer aiNormals = aiMesh.mNormals();
		while (aiNormals != null && aiNormals.remaining() > 0) {
			final AIVector3D aiNormal = aiNormals.get();
			normals.add(aiNormal.x());
			normals.add(aiNormal.y());
			normals.add(aiNormal.z());
		}
	}

	protected static void processTextCoords(final AIMesh aiMesh, final List<Float> textures) {
		final AIVector3D.Buffer textCoords = aiMesh.mTextureCoords(0);
		final int numTextCoords = textCoords != null ? textCoords.remaining() : 0;
		for (int i = 0; i < numTextCoords; i++) {
			final AIVector3D textCoord = textCoords.get();
			textures.add(textCoord.x());
			textures.add(1 - textCoord.y());
		}
	}

	protected static void processIndices(final AIMesh aiMesh, final List<Integer> indices) {
		final int numFaces = aiMesh.mNumFaces();
		final AIFace.Buffer aiFaces = aiMesh.mFaces();

		nmbTriangles += numFaces;

		for (int i = 0; i < numFaces; i++) {
			final AIFace aiFace = aiFaces.get(i);
			final IntBuffer buffer = aiFace.mIndices();

			while (buffer.remaining() > 0)
				indices.add(buffer.get());
		}
	}
}
