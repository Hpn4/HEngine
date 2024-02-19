package hengine.game.utils;

import static org.lwjgl.assimp.Assimp.aiExportScene;
import static org.lwjgl.assimp.Assimp.aiGetErrorString;
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
import static org.lwjgl.assimp.Assimp.aiReleaseImport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.assimp.AIExportFormatDesc;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;

public class MeshImprover {

	private final static int flags = aiProcess_FindInstances | aiProcess_FindInvalidData | aiProcess_OptimizeMeshes
			| aiProcess_CalcTangentSpace | aiProcess_GenNormals | aiProcess_JoinIdenticalVertices
			| aiProcess_ImproveCacheLocality | aiProcess_RemoveRedundantMaterials | aiProcess_Triangulate
			| aiProcess_GenUVCoords | aiProcess_SortByPType | aiProcess_FindDegenerates | aiProcess_FindInvalidData
			| aiProcess_DropNormals;

	public static void improveMesh(final String filePath, String outFile, final String outFormat)
			throws Exception {
		outFile += "." + outFormat;
		long time = System.nanoTime();
		System.out.println("Loading model : " + filePath + " :");

		final AIScene aiScene = aiImportFile(filePath, flags);

		if (aiScene == null) {
			System.err.println(aiGetErrorString());
			throw new Exception("Error loading model");
		}

		System.out.println("\t- Number of Mesh (figure) : " + aiScene.mNumMeshes());

		final AIMesh aiMesh = AIMesh.create(aiScene.mMeshes().get(0));

		System.out.println("\t- Number of vertices: " + aiMesh.mNumVertices());
		System.out.println("\t- Number of triangles: " + aiMesh.mNumFaces());

		System.out.println("Loaded in " + (System.nanoTime() - time) / 1000000d);
		time = System.nanoTime();

		final Path file = Paths.get(outFile);
		if (!Files.exists(file))
			Files.createFile(file);

		aiExportScene(aiScene, outFormat, outFile, flags);

		System.out.println("Wroted in " + (System.nanoTime() - time) / 1000000d);

		aiReleaseImport(aiScene);
	}

	public static void main(final String[] args) {
		for(int i = 0; i < Assimp.aiGetExportFormatCount(); i++) {
			final AIExportFormatDesc a = Assimp.aiGetExportFormatDescription(i);
			System.out.println(a.fileExtensionString());
			System.out.println("\t - " + a.descriptionString());
			System.out.println("\t - " + a.idString());
		}
		try {
			improveMesh("improved/test.obj", "improved/test", "fbx");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
