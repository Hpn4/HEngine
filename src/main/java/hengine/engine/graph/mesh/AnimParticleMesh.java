package hengine.engine.graph.mesh;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import hengine.engine.graph.Transformation;
import hengine.engine.graph.light.Material;
import hengine.engine.graph.particles.Particle;
import hengine.engine.graph.renderer.Renderer;
import hengine.engine.utils.Box3D;
import hengine.engine.utils.loader.texture.Texture;
import hengine.engine.world.item.GameItem;

public class AnimParticleMesh extends AbstractMesh {

	private static final int FLOAT_SIZE_BYTES = 4;

	private static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;

	private static final int MATRIX_SIZE_FLOATS = 4 * 4;

	// Matrice de model et de vision + scale + texture offset + ttl ratio
	private static final int INSTANCE_SIZE_FLOATS = MATRIX_SIZE_FLOATS + 1 + 2 + 1;

	private static final int INSTANCE_SIZE_BYTES = INSTANCE_SIZE_FLOATS * FLOAT_SIZE_BYTES;

	private final static float[] positions = { -1.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f,
			0.0f };

	private final static float[] textCoords = { 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f };

	private final static int[] indices = { 0, 1, 2, 0, 2, 3 };

	private final Vector2f[] texCoordsOff = new Vector2f[4];

	private final int numInstances;

	private final int instanceDataVBO;

	private FloatBuffer instanceDataBuffer;

	public AnimParticleMesh(final int numInstances) {
		box = new Box3D(new Vector3f(0, 0, 0), new Vector3f(1, 1, 0));

		this.numInstances = numInstances;

		FloatBuffer posBuffer = null, textCoordsBuffer = null;
		IntBuffer indicesBuffer = null;
		try {
			vboIdList = new ArrayList<>();

			glBindVertexArray(vaoId = glGenVertexArrays());

			int start = 0;

			// ********************** //
			// **** Position VBO **** //
			// ********************** //
			int vboId = glGenBuffers();
			vboIdList.add(vboId);

			posBuffer = MemoryUtil.memAllocFloat(positions.length);
			posBuffer.put(positions);
			posBuffer.flip();
			posBuffer.position(0);

			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(start);
			glVertexAttribPointer(start++, 3, GL_FLOAT, false, 0, 0);

			// ********************************* //
			// **** Texture Coordinates VBO **** //
			// ********************************* //
			vboIdList.add(vboId = glGenBuffers());

			textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
			textCoordsBuffer.put(textCoords).flip();

			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(start);
			glVertexAttribPointer(start++, 2, GL_FLOAT, false, 0, 0);

			// ******************* //
			// **** Index VBO **** //
			// ******************* //
			vboIdList.add(vboId = glGenBuffers());

			indicesBuffer = MemoryUtil.memAllocInt(indices.length);
			indicesBuffer.put(indices).flip();

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

			instanceDataVBO = glGenBuffers();
			vboIdList.add(instanceDataVBO);
			instanceDataBuffer = MemoryUtil.memAllocFloat(numInstances * INSTANCE_SIZE_FLOATS);
			glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);

			// ******************************** //
			// **** Instanced drawing data **** //
			// ******************************** //

			int strideStart = 0;

			// Model matrix
			for (int i = 0; i < 4; i++) {
				glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
				glVertexAttribDivisor(start, 1);
				glEnableVertexAttribArray(start++);

				strideStart += VECTOR4F_SIZE_BYTES;
			}

			// Scale
			glVertexAttribPointer(start, 1, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
			glVertexAttribDivisor(start, 1);
			glEnableVertexAttribArray(start++);

			strideStart += FLOAT_SIZE_BYTES;

			// TexOffset
			glVertexAttribPointer(start, 2, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
			glVertexAttribDivisor(start, 1);
			glEnableVertexAttribArray(start++);

			strideStart += FLOAT_SIZE_BYTES * 2;

			// Ttl ratio
			glVertexAttribPointer(start, 1, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
			glVertexAttribDivisor(start, 1);
			glEnableVertexAttribArray(start);

			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);

		} finally {
			if (posBuffer != null)
				MemoryUtil.memFree(posBuffer);

			if (textCoordsBuffer != null)
				MemoryUtil.memFree(textCoordsBuffer);

			if (indicesBuffer != null)
				MemoryUtil.memFree(indicesBuffer);
		}
	}

	@Override
	public void cleanup() {
		super.cleanup();

		if (instanceDataBuffer != null) {
			MemoryUtil.memFree(instanceDataBuffer);
			instanceDataBuffer = null;
		}
	}

	public void renderListInstanced(final List<Particle> particles, final Matrix4f viewMatrix, final int cols,
			final int rows) {
		initRender(true);

		instanceDataBuffer.clear();

		int i = 0;

		for (final Particle gameItem : particles) {
			int buffPos = INSTANCE_SIZE_FLOATS * i; // L'index mémoire

			// On construit la matrice
			final Matrix4f modelMatrix = Transformation.buildModelMatrix(gameItem);

			// modelMatrix.mul(viewMatrix, modelMatrix);
			// Billboarding
			viewMatrix.transpose3x3(modelMatrix);

			// On construit la matrice de model et de vision en préservant la taille
			final Matrix4f modelViewMatrix = Transformation.buildProjectionViewMatrix(viewMatrix, modelMatrix);
			modelViewMatrix.scale(gameItem.getScale());

			// On injecte dans le buffer
			modelViewMatrix.get(buffPos, instanceDataBuffer);

			buffPos += MATRIX_SIZE_FLOATS;

			// Scale
			instanceDataBuffer.put(buffPos++, gameItem.getScale());

			// TexOffset
			final int col = gameItem.getTextPos() % cols, row = gameItem.getTextPos() / cols;
			final float textXOffset = (float) col / cols, textYOffset = (float) row / rows;

			instanceDataBuffer.put(buffPos++, textXOffset);
			instanceDataBuffer.put(buffPos++, textYOffset);

			// Ttl Ratio
			instanceDataBuffer.put(buffPos, gameItem.getTtlRatio());

			i++;
		}

		final int size = particles.size();

		glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
		glBufferData(GL_ARRAY_BUFFER, instanceDataBuffer, GL_STATIC_DRAW);

		// Il y a 6 indices
		glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0, size);

		Renderer.instancedItem += size;
		Renderer.instancedDrawCalls++;

		glBindBuffer(GL_ARRAY_BUFFER, 0);

		endRender(true);
	}

	public void setMaterial(final Material mat) {
		super.setMaterial(mat);

		final Texture text = (Texture) mat.getDiffuseMap();
		final int cols = text.getNumCols();
		final int rows = text.getNumCols();

		for (int i = 0; i < 4; i++) {
			final Vector2f vec = new Vector2f();

			vec.x = textCoords[i * 2] / cols;
			vec.y = textCoords[i * 2 + 1] / rows;

			texCoordsOff[i] = vec;
		}
	}

	public Vector2f[] getTexCoordsOff() {
		return texCoordsOff;
	}

	public int getNumInstances() {
		return numInstances;
	}

	@Override
	public void render(final boolean withTexture) {
	}

	@Override
	public void renderList(final List<GameItem> gameItems, final Consumer<GameItem> consumer,
			final boolean withTexture) {
	}

	@Override
	public FloatBuffer getVertices() {
		return null;
	}
}
