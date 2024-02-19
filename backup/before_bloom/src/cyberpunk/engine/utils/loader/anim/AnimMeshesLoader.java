package hengine.engine.utils.loader.anim;

import static org.lwjgl.assimp.Assimp.aiGetErrorString;
import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_FixInfacingNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_GenSmoothNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_LimitBoneWeights;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;
import static org.lwjgl.assimp.Assimp.aiReleaseImport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIQuaternion;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVectorKey;
import org.lwjgl.assimp.AIVertexWeight;

import hengine.engine.graph.anime.AnimGameItem;
import hengine.engine.graph.anime.AnimatedFrame;
import hengine.engine.graph.anime.Animation;
import hengine.engine.graph.light.Material;
import hengine.engine.graph.mesh.AnimatedMesh;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.utils.Utils;
import hengine.engine.utils.loader.StaticMeshesLoader;

public class AnimMeshesLoader extends StaticMeshesLoader {

	public static AnimGameItem loadAnimGameItem(final String resourcePath, final String texturesDir) throws Exception {
		return loadAnimGameItem(resourcePath, texturesDir, aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices
				| aiProcess_Triangulate | aiProcess_FixInfacingNormals | aiProcess_LimitBoneWeights);
	}

	public static AnimGameItem loadAnimGameItem(final String resourcePath, final String texturesDir, final int flags)
			throws Exception {
		final AIScene aiScene = aiImportFile(Utils.getPath(resourcePath), flags);

		if (aiScene == null) {
			System.out.println(aiGetErrorString());
			throw new Exception("Error loading model : " + resourcePath);
		}

		// Parcours la liste et recuperer tous les materiaux (textures, couleur....)
		final PointerBuffer aiMaterials = aiScene.mMaterials();
		final List<Material> materials = new ArrayList<>();
		for (int i = 0, numMat = aiScene.mNumMaterials(); i < numMat; i++) {
			final AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
			processMaterial(aiMaterial, materials, texturesDir);
		}

		final List<Bone> boneList = new ArrayList<>();
		final int numMeshes = aiScene.mNumMeshes();
		final PointerBuffer aiMeshes = aiScene.mMeshes();
		final AnimatedMesh[] meshes = new AnimatedMesh[numMeshes];
		for (int i = 0; i < numMeshes; i++) {
			AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
			AnimatedMesh mesh = processMesh(aiMesh, materials, boneList);
			meshes[i] = mesh;
		}

		final AINode aiRootNode = aiScene.mRootNode();
		final Matrix4f rootTransfromation = toMatrix(aiRootNode.mTransformation());
		final Node rootNode = processNodesHierarchy(aiRootNode, null);

		final Map<String, Animation> animations = processAnimations(aiScene, boneList, rootNode, rootTransfromation);

		final AnimGameItem item = new AnimGameItem(meshes, animations);

		aiReleaseImport(aiScene);
		return item;
	}

	private static void buildTransFormationMatrices(final AINodeAnim aiNodeAnim, final Node node) {
		final AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys(), scalingKeys = aiNodeAnim.mScalingKeys();

		final AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

		for (int i = 0, numFrames = aiNodeAnim.mNumPositionKeys(); i < numFrames; i++) {
			AIVector3D vec = positionKeys.get(i).mValue();

			final Matrix4f transfMat = new Matrix4f().translate(vec.x(), vec.y(), vec.z());

			final AIQuaternion aiQuat = rotationKeys.get(i).mValue();
			final Quaternionf quat = new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
			transfMat.rotate(quat);

			if (i < aiNodeAnim.mNumScalingKeys()) {
				vec = scalingKeys.get(i).mValue();
				transfMat.scale(vec.x(), vec.y(), vec.z());
			}

			node.addTransformation(transfMat);
		}
	}

	private static List<AnimatedFrame> buildAnimationFrames(final List<Bone> boneList, final Node rootNode,
			final Matrix4f rootTransformation) {

		final int numFrames = rootNode.getAnimationFrames();
		final List<AnimatedFrame> frameList = new ArrayList<>();

		for (int i = 0; i < numFrames; i++) {
			final AnimatedFrame frame = new AnimatedFrame();
			frameList.add(frame);

			final int numBones = boneList.size();
			for (int j = 0; j < numBones; j++) {
				final Bone bone = boneList.get(j);
				final Node node = rootNode.findByName(bone.getBoneName());
				Matrix4f boneMatrix = Node.getParentTransforms(node, i);
				boneMatrix.mul(bone.getOffsetMatrix());
				boneMatrix = new Matrix4f(rootTransformation).mul(boneMatrix);
				frame.setMatrix(j, boneMatrix);
			}
		}

		return frameList;
	}

	private static Map<String, Animation> processAnimations(final AIScene aiScene, final List<Bone> boneList,
			final Node rootNode, final Matrix4f rootTransformation) {
		final Map<String, Animation> animations = new HashMap<>();

		// Process all animations
		final int numAnimations = aiScene.mNumAnimations();
		final PointerBuffer aiAnimations = aiScene.mAnimations();

		for (int i = 0; i < numAnimations; i++) {
			final AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));

			// Calculate transformation matrices for each node
			final int numChanels = aiAnimation.mNumChannels();
			final PointerBuffer aiChannels = aiAnimation.mChannels();

			for (int j = 0; j < numChanels; j++) {
				final AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(j));
				final String nodeName = aiNodeAnim.mNodeName().dataString();
				final Node node = rootNode.findByName(nodeName);

				buildTransFormationMatrices(aiNodeAnim, node);
			}

			final List<AnimatedFrame> frames = buildAnimationFrames(boneList, rootNode, rootTransformation);
			final Animation animation = new Animation(aiAnimation.mName().dataString(), frames,
					aiAnimation.mDuration());

			animations.put(animation.getName(), animation);
		}
		return animations;
	}

	private static void processBones(final AIMesh aiMesh, final List<Bone> boneList, final List<Integer> boneIds,
			final List<Float> weights) {
		final Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();

		final int numBones = aiMesh.mNumBones();
		final PointerBuffer aiBones = aiMesh.mBones();

		for (int i = 0; i < numBones; i++) {
			final int id = boneList.size();
			final AIBone aiBone = AIBone.create(aiBones.get(i));
			final Bone bone = new Bone(id, aiBone.mName().dataString(), toMatrix(aiBone.mOffsetMatrix()));

			boneList.add(bone);

			final int numWeights = aiBone.mNumWeights();
			final AIVertexWeight.Buffer aiWeights = aiBone.mWeights();

			for (int j = 0; j < numWeights; j++) {
				final AIVertexWeight aiWeight = aiWeights.get(j);
				final VertexWeight vw = new VertexWeight(bone.getBoneId(), aiWeight.mVertexId(), aiWeight.mWeight());
				List<VertexWeight> vertexWeightList = weightSet.get(vw.getVertexId());

				if (vertexWeightList == null) {
					vertexWeightList = new ArrayList<>();
					weightSet.put(vw.getVertexId(), vertexWeightList);
				}
				vertexWeightList.add(vw);
			}
		}

		final int numVertices = aiMesh.mNumVertices();
		for (int i = 0; i < numVertices; i++) {
			final List<VertexWeight> vertexWeightList = weightSet.get(i);
			final int size = vertexWeightList != null ? vertexWeightList.size() : 0;

			for (int j = 0; j < Mesh.MAX_WEIGHTS; j++) {
				if (j < size) {
					final VertexWeight vw = vertexWeightList.get(j);
					weights.add(vw.getWeight());
					boneIds.add(vw.getBoneId());
				} else {
					weights.add(0.0f);
					boneIds.add(0);
				}
			}
		}
	}

	private static AnimatedMesh processMesh(final AIMesh aiMesh, final List<Material> materials,
			final List<Bone> boneList) {
		final List<Float> vertices = new ArrayList<>(), textures = new ArrayList<>(), normals = new ArrayList<>(),
				weights = new ArrayList<>();

		final List<Integer> indices = new ArrayList<>(), boneIds = new ArrayList<>();

		processVertices(aiMesh, vertices);
		processNormals(aiMesh, normals);
		processTextCoords(aiMesh, textures);
		processIndices(aiMesh, indices);
		processBones(aiMesh, boneList, boneIds, weights);

		final AnimatedMesh mesh = new AnimatedMesh(Utils.listToArray(vertices), Utils.listToArray(textures),
				Utils.listToArray(normals), Utils.listIntToArray(indices), Utils.listIntToArray(boneIds),
				Utils.listToArray(weights));

		Material material;
		final int materialIndex = aiMesh.mMaterialIndex();

		if (materialIndex >= 0 && materialIndex < materials.size())
			material = materials.get(materialIndex);
		else
			material = new Material();

		mesh.setMaterial(material);

		return mesh;
	}

	private static Node processNodesHierarchy(final AINode aiNode, final Node parentNode) {
		final String nodeName = aiNode.mName().dataString();
		final Node node = new Node(nodeName, parentNode);

		final int numChildren = aiNode.mNumChildren();
		final PointerBuffer aiChildren = aiNode.mChildren();
		for (int i = 0; i < numChildren; i++) {
			final AINode aiChildNode = AINode.create(aiChildren.get(i));
			final Node childNode = processNodesHierarchy(aiChildNode, node);
			node.addChild(childNode);
		}

		return node;
	}

	// Convertie une AIMatrix4x4 (Assimp) en Matrix4f (JOML)
	private static Matrix4f toMatrix(final AIMatrix4x4 aiMatrix4x4) {
		final Matrix4f result = new Matrix4f();
		result.m00(aiMatrix4x4.a1());
		result.m10(aiMatrix4x4.a2());
		result.m20(aiMatrix4x4.a3());
		result.m30(aiMatrix4x4.a4());
		result.m01(aiMatrix4x4.b1());
		result.m11(aiMatrix4x4.b2());
		result.m21(aiMatrix4x4.b3());
		result.m31(aiMatrix4x4.b4());
		result.m02(aiMatrix4x4.c1());
		result.m12(aiMatrix4x4.c2());
		result.m22(aiMatrix4x4.c3());
		result.m32(aiMatrix4x4.c4());
		result.m03(aiMatrix4x4.d1());
		result.m13(aiMatrix4x4.d2());
		result.m23(aiMatrix4x4.d3());
		result.m33(aiMatrix4x4.d4());

		return result;
	}
}