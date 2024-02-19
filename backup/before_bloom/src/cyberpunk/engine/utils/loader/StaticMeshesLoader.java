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
import static org.lwjgl.assimp.Assimp.aiProcess_OptimizeGraph;
import static org.lwjgl.assimp.Assimp.aiProcess_OptimizeMeshes;
import static org.lwjgl.assimp.Assimp.aiProcess_RemoveRedundantMaterials;
import static org.lwjgl.assimp.Assimp.aiProcess_SortByPType;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;
import static org.lwjgl.assimp.Assimp.aiReleaseImport;
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

import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.system.MemoryStack;

import hengine.engine.graph.light.Material;
import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.graph.mesh.SkyboxMesh;
import hengine.engine.utils.Utils;
import hengine.engine.utils.loader.texture.AbstractTexture;
import hengine.engine.utils.loader.texture.TextureCache;

public class StaticMeshesLoader {

	private final static int flags = aiProcess_FindInstances | aiProcess_FindInvalidData | aiProcess_OptimizeMeshes
			| aiProcess_CalcTangentSpace | aiProcess_GenNormals | aiProcess_JoinIdenticalVertices
			| aiProcess_ImproveCacheLocality | aiProcess_RemoveRedundantMaterials | aiProcess_Triangulate
			| aiProcess_GenUVCoords | aiProcess_SortByPType | aiProcess_FindDegenerates | aiProcess_FindInvalidData
			| aiProcess_DropNormals | aiProcess_OptimizeGraph;

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
		System.out.println("Loading model : " + resourcePath + " :");

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

		System.out.println("\t- Number of Mesh (figure) : " + numMeshes);
		System.out.println("\t- Number of Polygon (triangles) : " + nmbTriangles);
		System.out.println("\t- Number of Vertices : " + nmbVertex);

		System.out.println("Materials of the model : ");
		for (final AbstractMesh mesh : meshes)
			System.out.println("\t- " + mesh.getMaterial());

		System.out.println();

		return meshes;
	}

	protected static AbstractTexture getTexture(final AIMaterial aiMaterial, final int textureType,
			final String texturesDir) throws Exception {
		final AIString path = AIString.calloc();

		aiGetMaterialTexture(aiMaterial, textureType, 0, path, (IntBuffer) null, null, null, null, null, null);

		String textPath = path.dataString();
		AbstractTexture texture = null;

		if (textPath != null && textPath.length() > 0) {
			String textureFile = "";
			if (texturesDir != null && texturesDir.length() > 0)
				textureFile += texturesDir + "/";

			textureFile = (textureFile + textPath).replace("//", "/");
			texture = TextureCache.getTexture(textureFile);

			final String type = textureType == 1 ? "diffuse"
					: textureType == 2 ? "specular" : (textureType == 5 | textureType == 6) ? "normal" : "emissive";

			System.out.println("\t- " + type + "Map : " + textureFile);
		}

		path.free();

		return texture;
	}

	protected static void processMaterial(final AIMaterial aiMaterial, final List<Material> materials,
			final String texturesDir) throws Exception {
		final AIColor4D colour = AIColor4D.create();

		// Diffuse map texture
		final AbstractTexture diffuseMap = getTexture(aiMaterial, aiTextureType_DIFFUSE, texturesDir);

		// Specular map texture
		final AbstractTexture specularMap = getTexture(aiMaterial, aiTextureType_SPECULAR, texturesDir);
		
		// Emissive map texture
		final AbstractTexture emissiveMap = getTexture(aiMaterial, aiTextureType_EMISSIVE, texturesDir);

		// Normal map texture
		AbstractTexture normalMap = getTexture(aiMaterial, aiTextureType_HEIGHT, texturesDir);

		if (normalMap == null)
			normalMap = getTexture(aiMaterial, aiTextureType_NORMALS, texturesDir);

		// Diffuse color
		Vector4f diffuse = Material.DEFAULT_COLOUR;
		int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, colour);
		if (result == aiReturn_SUCCESS)
			diffuse = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());

		// Specualar color
		Vector4f specular = Material.DEFAULT_COLOUR;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, colour);
		if (result == aiReturn_SUCCESS)
			specular = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());

		// Emissive color
		Vector4f emissive = new Vector4f(0f, 0f, 0f, 1f);
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_EMISSIVE, aiTextureType_NONE, 0, colour);
		if (result == aiReturn_SUCCESS)
			emissive = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());

		// Shininess
		float shininess = 0;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer size = stack.ints(1);
			final FloatBuffer data = stack.floats(1);
			aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_SHININESS, aiTextureType_NONE, 0, data, size);
			shininess = data.get();
		}

		final Material material = new Material(diffuse, specular, emissive, diffuseMap, specularMap, emissiveMap, normalMap, shininess);

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
