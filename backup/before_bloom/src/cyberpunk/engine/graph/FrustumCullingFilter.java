package hengine.engine.graph;

import java.util.List;
import java.util.Map;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.item.GameItem;
import hengine.engine.utils.Box3D;

public class FrustumCullingFilter {

	private final Matrix4f prjViewMatrix;

	private final FrustumIntersection frustumInt;

	public FrustumCullingFilter() {
		prjViewMatrix = new Matrix4f();
		frustumInt = new FrustumIntersection();
	}

	public void updateFrustum(final Matrix4f projMatrix, final Matrix4f viewMatrix) {
		// Calcul la matrice de vue de projection
		prjViewMatrix.set(projMatrix).mul(viewMatrix);

		// Mais a jour l'objet
		frustumInt.set(prjViewMatrix, false);
	}

	public void filter(final Map<? extends AbstractMesh, List<GameItem>> mapMesh) {
		for (final Map.Entry<? extends AbstractMesh, List<GameItem>> entry : mapMesh.entrySet()) {
			List<GameItem> gameItems = entry.getValue();
			filter(gameItems);
		}
	}

	public void filter(final List<GameItem> gameItems) {
		for (final GameItem gameItem : gameItems) {
			final Box3D[] boxes = gameItem.getBox3D();

			for (final Box3D box : boxes)
				gameItem.setInsideFrustum(insideFrustum(box.origin, box.dim));
		}
	}

	public boolean insideFrustum(final Vector3f min, final Vector3f max) {
		return frustumInt.testAab(min, max);
	}
}
