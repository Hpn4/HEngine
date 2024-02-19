package hengine.engine.graph.shadow;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import hengine.engine.graph.Transformation;
import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.hlib.component.HWindow;

public class ShadowCascade {

	private static final int FRUSTUM_CORNERS = 8;

	private final Matrix4f projViewMatrix;

	private final Matrix4f orthoProjMatrix;

	private final Matrix4f lightViewMatrix;
	
	private final Matrix4f orthoProjLightViewMatrix;

	private final Vector3f[] frustumCorners;

	private final float zNear;

	private final float zFar;

	private final Vector4f tmpVec;

	public ShadowCascade(final float zNear, final float zFar) {
		this.zNear = zNear;
		this.zFar = zFar;

		projViewMatrix = new Matrix4f();
		orthoProjMatrix = new Matrix4f();
		lightViewMatrix = new Matrix4f();
		orthoProjLightViewMatrix = new Matrix4f();
		frustumCorners = new Vector3f[FRUSTUM_CORNERS];

		for (int i = 0; i < FRUSTUM_CORNERS; i++)
			frustumCorners[i] = new Vector3f();

		tmpVec = new Vector4f();
	}

	public Matrix4f getLightViewMatrix() {
		return lightViewMatrix;
	}

	public Matrix4f getOrthoProjMatrix() {
		return orthoProjMatrix;
	}
	
	public Matrix4f getOrthoProjLightViewMatrix() {
		return orthoProjLightViewMatrix;
	}

	public void update(final HWindow window, final Matrix4f viewMatrix, final DirectionalLight light) {
		// Build projection view matrix for this cascade
		final float aspectRatio = (float) window.getWidth() / (float) window.getHeight();
		projViewMatrix.setPerspective(HWindow.FOV, aspectRatio, zNear, zFar);
		projViewMatrix.mul(viewMatrix);

		final Vector3f centroid = new Vector3f();
		
		// On calcul la position des sommet du frustum par la matrice de projection
		float maxZ = Float.MIN_VALUE;
		float minZ = Float.MAX_VALUE;
		for (int i = 0; i < FRUSTUM_CORNERS; i++) {
			final Vector3f corner = frustumCorners[i];
			corner.set(0, 0, 0);
			projViewMatrix.frustumCorner(i, corner);

			centroid.add(corner);
			minZ = Math.min(minZ, corner.z);
			maxZ = Math.max(maxZ, corner.z);
		}

		// On fait la moyenne (il y a 8 angle dans un frustum)
		centroid.div(FRUSTUM_CORNERS);

		// Go back from the centroid up to max.z - min.z in the direction of light
		final Vector3f lightDirection = light.getDirection();
		final Vector3f lightPosInc = new Vector3f(lightDirection);

		final float distance = maxZ - minZ;
		lightPosInc.mul(distance);

		final Vector3f lightPosition = new Vector3f(centroid);
		lightPosition.add(lightPosInc);

		updateLightViewMatrix(lightDirection, lightPosition);

		updateLightProjectionMatrix();
		
		orthoProjLightViewMatrix.set(orthoProjMatrix).mul(lightViewMatrix);
	}

	private void updateLightViewMatrix(final Vector3f lightDirection, final Vector3f lightPosition) {
		float lightAngleX = (float) Math.toDegrees(Math.acos(lightDirection.z));
		float lightAngleY = (float) Math.toDegrees(Math.asin(lightDirection.x));
		float lightAngleZ = 0;
		Transformation.updateGenericViewMatrix(lightPosition, new Vector3f(lightAngleX, lightAngleY, lightAngleZ),
				lightViewMatrix);
	}

	private void updateLightProjectionMatrix() {
		// On calcul les dimensions du frustum, le max et le min pour chacun des axes
		float minX = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxZ = Float.MIN_VALUE;

		for (int i = 0; i < FRUSTUM_CORNERS; i++) {
			final Vector3f corner = frustumCorners[i];
			tmpVec.set(corner, 1);
			tmpVec.mul(lightViewMatrix);

			minX = Math.min(tmpVec.x, minX);
			maxX = Math.max(tmpVec.x, maxX);
			minY = Math.min(tmpVec.y, minY);
			maxY = Math.max(tmpVec.y, maxY);
			minZ = Math.min(tmpVec.z, minZ);
			maxZ = Math.max(tmpVec.z, maxZ);
		}

		final float distz = maxZ - minZ;

		orthoProjMatrix.setOrtho(minX, maxX, minY, maxY, 0, distz);
	}
}
