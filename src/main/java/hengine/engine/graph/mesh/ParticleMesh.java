package hengine.engine.graph.mesh;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import hengine.engine.graph.Transformation;
import hengine.engine.graph.particles.Particle;
import hengine.engine.graph.renderer.Renderer;
import hengine.engine.utils.Box3D;
import hengine.engine.world.item.GameItem;

public class ParticleMesh extends AbstractMesh {

	private static final int FLOAT_SIZE_BYTES = 4;

	private static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;

	private static final int MATRIX_SIZE_FLOATS = 4 * 4;

	private static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;

	private static final int INSTANCE_SIZE_BYTES = MATRIX_SIZE_BYTES + FLOAT_SIZE_BYTES;

	private static final int INSTANCE_SIZE_FLOATS = MATRIX_SIZE_FLOATS + 1;

	private final int numInstances;

	private final int instanceDataVBO;

	private FloatBuffer instanceDataBuffer;

	public ParticleMesh(final int numInstances) {

		box = new Box3D(new Vector3f(0, 0, 0), new Vector3f(1, 1, 0));
		this.numInstances = numInstances;

		vboIdList = new ArrayList<>();

		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		instanceDataVBO = glGenBuffers();
		vboIdList.add(instanceDataVBO);
		instanceDataBuffer = MemoryUtil.memAllocFloat(numInstances * INSTANCE_SIZE_FLOATS);
		glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);

		int start = 0, strideStart = 0;

		// Model matrix
		for (int i = 0; i < 4; i++) {
			glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
			glVertexAttribDivisor(start, 1);
			glEnableVertexAttribArray(start);
			start++;
			strideStart += VECTOR4F_SIZE_BYTES;
		}

		// Scale
		glVertexAttribPointer(start, 1, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
		glVertexAttribDivisor(start, 1);
		glEnableVertexAttribArray(start);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	@Override
	public void cleanup() {
		super.cleanup();

		if (instanceDataBuffer != null) {
			MemoryUtil.memFree(instanceDataBuffer);
			instanceDataBuffer = null;
		}
	}

	public void renderListInstanced(final List<Particle> gameItems, final Matrix4f viewMatrix,
			final boolean withTexture) {
		initRender(withTexture);

		instanceDataBuffer.clear();

		int i = 0;

		for (final Particle gameItem : gameItems) {
			int buffPos = INSTANCE_SIZE_FLOATS * i; // L'index mémoire

			final Matrix4f modelMatrix = Transformation.buildModelMatrix(gameItem); // On construit la matrice
			viewMatrix.mul(modelMatrix, modelMatrix);

			// Model view Matrix
			modelMatrix.get(buffPos, instanceDataBuffer);

			buffPos += MATRIX_SIZE_FLOATS;

			// Scale
			instanceDataBuffer.put(buffPos, gameItem.getScale());

			i++;
		}

		final int size = gameItems.size();

		glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
		glBufferData(GL_ARRAY_BUFFER, instanceDataBuffer, GL_STATIC_DRAW);

		glDrawArraysInstanced(GL_POINTS, 0, 1, size);

		Renderer.instancedItem += size;
		Renderer.instancedDrawCalls++;

		glBindBuffer(GL_ARRAY_BUFFER, 0);

		endRender(withTexture);
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
