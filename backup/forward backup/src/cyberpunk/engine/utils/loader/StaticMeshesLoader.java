package hengine.engine.utils.loader;

import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_AMBIENT;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_DIFFUSE;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_SPECULAR;
import static org.lwjgl.assimp.Assimp.aiGetErrorString;
import static org.lwjgl.assimp.Assimp.aiGetMaterialColor;
import static org.lwjgl.assimp.Assimp.aiGetMaterialTexture;
import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_CalcTangentSpace;
import static org.lwjgl.assimp.Assimp.aiProcess_DropNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_FindDegenerates;
import static org.lwjgl.assimp.Assimp.aiProcess_FindInstances;
import static org.lwjgl.assimp.Assimp.aiProcess_FindInvalidData;
import static org.lwjgl.assimp.Assimp.aiProcess_GenSmoothNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_GenUVCoords;
import static org.lwjgl.assimp.Assimp.aiProcess_ImproveCacheLocality;
import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_OptimizeGraph;
import static org.lwjgl.assimp.Assimp.aiProcess_OptimizeMeshes;
import static org.lwjgl.assimp.Assimp.aiProcess_RemoveRedundantMaterials;
import static org.lwjgl.assimp.Assimp.aiProcess_SortByPType;
import static org.lwjgl.assimp.Assimp.aiProcess_SplitLargeMeshes;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;
import static org.lwjgl.assimp.Assimp.aiReleaseImport;
import static org.lwjgl.assimp.Assimp.aiReturn_SUCCESS;
import static org.lwjgl.assimp.Assimp.aiTextureType_DIFFUSE;
import static org.lwjgl.assimp.Assimp.aiTextureType_HEIGHT;
import static org.lwjgl.assimp.Assimp.aiTextureType_NONE;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;

import hengine.engine.graph.AbstractTexture;
import hengine.engine.graph.light.Material;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.graph.mesh.SkyboxMesh;
import hengine.engine.utils.Utils;

public class StaticMeshesLoader {

	private final static int flags = aiProcess_FindInstances | aiProcess_FindInvalidData | aiProcess_OptimizeMeshes
			| aiProcess_CalcTangentSpace | aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices
			| aiProcess_ImproveCacheLocality | aiProcess_RemoveRedundantMaterials | aiProcess_SplitLargeMeshes
			| aiProcess_Triangulate | aiProcess_GenUVCoords | aiProcess_SortByPType | aiProcess_FindDegenerates
			| aiProcess_FindInvalidData | aiProcess_DropNormals | aiProcess_OptimizeGraph;

	private static int nmbVertex, nmbTriangles;

	private enum MeshesType {
		SKYBOX, INSTANCED, DEFAULT;
	}

	public static Mesh[] load(final String resourcePath, final String texturesDir) throws Exception {
		final AbstractMesh[] absMeshes = loadAny(resourcePath, texturesDir, MeshesType.DEFAULT, 0);
		final Mesh[] meshes = new Mesh[absMeshes.length];

		for (int i = 0; i < absMeshes.length; i++)
			meshes[i] = (Mesh) absMeshes[i];
		return meshes;
	}

	public static InstancedMesh[] loadInstanced(final String resourcePath, final String texturesDir,
			final int numInstances) throws Exception {
		final AbstractMesh[] absMeshes = loadAny(resourcePath, texturesDir, MeshesType.INSTANCED, numInstances);
		final InstancedMesh[] meshes = new InstancedMesh[absMeshes.length];

		for (int i = 0; i < absMeshes.length; i++)
			meshes[i] = (InstancedMesh) absMeshes[i];

		return meshes;
	}

	public static SkyboxMesh loadSkybox(final String resourcePath, final String texturesDir) throws Exception {
		return (SkyboxMesh) loadAny(resourcePath, texturesDir, MeshesType.SKYBOX, 0)[0];
	}

	private static AbstractMesh[] loadAny(final String resourcePath, final String texturesDir, final MeshesType type,
			final int numInstances) throws Exception {
		nmbTriangles = nmbVertex = 0;
		final AIScene aiScene = aiImportFile(Utils.getPath(resourcePath), flags);

		if (aiScene == null) {
			System.err.println(aiGetErrorString());
			throw new Exception("Error loading model");
		}

		final int numMaterials = aiScene.mNumMaterials();
		final PointerBuffer aiMaterials = aiScene.mMaterials();
		final List<Material> materials = new ArrayList<>();

		for (int i = 0; i < numMaterials; i++) {
			final AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
			processMaterial(aiMaterial, materials, texturesDir);
		}

		final int numMeshes = aiScene.mNumMeshes();
		final PointerBuffer aiMeshes = aiScene.mMeshes();
		final AbstractMesh[] meshes = new AbstractMesh[numMeshes];

		for (int i = 0; i < numMeshes; i++) {
			final AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
			meshes[i] = processMesh(aiMesh, materials, type, numInstances);
		}
		// Libere toute les ressources
		aiReleaseImport(aiScene);

		System.out.println("Loading model : " + resourcePath + " :");
		System.out.println("\t- Number of Mesh (figure) : " + numMeshes);
		System.out.println("\t- Number of Polygon (triangles) : " + nmbTriangles);
		System.out.println("\t- Number of Vertices : " + nmbVertex);

		return meshes;
	}

	protected static void processMaterial(final AIMaterial aiMaterial, final List<Material> materials,
			final String texturesDir) throws Exception {
		final AIColor4D colour = AIColor4D.create();
		final AIString path = AIString.calloc();

		// Diffuse texture
		aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null,
				null, null);

		String textPath = path.dataString();
		AbstractTexture texture = null;

		if (textPath != null && textPath.length() > 0) {
			String textureFile = "";
			if (texturesDir != null && texturesDir.length() > 0)
				textureFile += texturesDir + "/";

			textureFile = (textureFile + textPath).replace("//", "/");
			texture = TextureCache.getTexture(textureFile);
		}

		path.clear();

		// Normal map texture
		aiGetMaterialTexture(aiMaterial, aiTextureType_HEIGHT, 0, path, (IntBuffer) null, null, null, null, null,
				null);

		final String textPathNormals = path.dataString();
		AbstractTexture textureNormals = null;

		if (textPathNormals != null && textPathNormals.length() > 0) {
			String textureFile = "";
			if (texturesDir != null && texturesDir.length() > 0)
				textureFile += texturesDir + "/";

			textureFile = (textureFile + textPath).replace("//", "/");
			textureNormals = TextureCache.getTexture(textureFile);
		}

		// Ambient color
		Vector4f ambient = Material.DEFAULT_COLOUR;
		int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, colour);
		if (result == aiReturn_SUCCESS)
			ambient = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());

		// Diffuse color
		Vector4f diffuse = Material.DEFAULT_COLOUR;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, colour);
		if (result == aiReturn_SUCCESS)
			diffuse = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());

		// Specualar color
		Vector4f specular = Material.DEFAULT_COLOUR;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, colour);
		if (result == aiReturn_SUCCESS)
			specular = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());

		final Material material = new Material(ambient, diffuse, specular, 1.0f);

		material.setTexture(texture);
		material.setNormalMap(textureNormals);

		materials.add(material);

	}

	protected static AbstractMesh processMesh(final AIMesh aiMesh, final List<Material> materials,
			final MeshesType type, final int numInstances) {
		final List<Float> vertices = new ArrayList<>();
		final List<Float> textures = new ArrayList<>();
		final List<Float> normals = new ArrayList<>();
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
