package hengine.engine.graph.mesh;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import hengine.engine.graph.Renderer;
import hengine.engine.item.GameItem;

public class InstancedMesh extends Mesh {

	protected static final int FLOAT_SIZE_BYTES = 4;

	protected static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;

	protected static final int VECTOR3F_SIZE_BYTES = 3 * FLOAT_SIZE_BYTES;

	protected static final int MATRIX_SIZE_FLOATS = 4 * 4;

	protected static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;

	protected static final int MATRIX3F_SIZE_FLOATS = 3 * 3;

	protected static final int MATRIX3F_SIZE_BYTES = MATRIX3F_SIZE_FLOATS * FLOAT_SIZE_BYTES;

	protected static final int INSTANCE_SIZE_BYTES = MATRIX_SIZE_BYTES + MATRIX3F_SIZE_BYTES;

	protected static final int INSTANCE_SIZE_FLOATS = MATRIX_SIZE_FLOATS + MATRIX3F_SIZE_FLOATS;

	private final int numInstances;

	private final int instanceDataVBO;

	private FloatBuffer instanceDataBuffer;

	public InstancedMesh(final float[] positions, final float[] textCoords, final float[] normals, final int[] indices,
			final int numInstances) {
		super(positions, textCoords, normals, indices);

		this.numInstances = numInstances;

		glBindVertexArray(vaoId);

		instanceDataVBO = glGenBuffers();
		vboIdList.add(instanceDataVBO);
		instanceDataBuffer = MemoryUtil.memAllocFloat(numInstances * INSTANCE_SIZE_FLOATS);
		glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);

		int start = 3; // O : position, 1 : normal, 2 : texCoord
		int strideStart = 0;

		// Model matrix
		for (int i = 0; i < 4; i++) {
			glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
			glVertexAttribDivisor(start, 1);
			glEnableVertexAttribArray(start);
			start++;
			strideStart += VECTOR4F_SIZE_BYTES;
		}

		// Normal matrix
		for (int i = 0; i < 3; i++) {
			glVertexAttribPointer(start, 3, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
			glVertexAttribDivisor(start, 1);
			start++;
			strideStart += VECTOR3F_SIZE_BYTES;
		}

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		if (instanceDataBuffer != null) {
			MemoryUtil.memFree(instanceDataBuffer);
			instanceDataBuffer = null;
		}
	}

	public void renderListInstanced(final List<GameItem> gameItems) {
		renderListInstanced(gameItems, true);
	}

	public void renderListInstanced(final List<GameItem> gameItems, final boolean withTexture) {
		initRender(withTexture);

		int chunkSize = numInstances;
		int length = gameItems.size();

		for (int i = 0; i < length; i += chunkSize) {
			int end = Math.min(length, i + chunkSize);
			List<GameItem> subList = gameItems.subList(i, end);
			renderChunkInstanced(subList);
		}

		endRender(withTexture);
	}

	private void renderChunkInstanced(final List<GameItem> gameItems) {
		instanceDataBuffer.clear();

		int i = 0;

		final Iterator<GameItem> it = gameItems.iterator();
		while (it.hasNext()) {
			final GameItem gameItem = it.next();

			int buffPos = INSTANCE_SIZE_FLOATS * i;

			// Model matrix
			gameItem.getModelMatrix().get(buffPos, instanceDataBuffer);
			buffPos += MATRIX_SIZE_FLOATS;

			// Normal matrix
			gameItem.getNormalMatrix().get(buffPos, instanceDataBuffer);
			buffPos += MATRIX3F_SIZE_FLOATS;

			i++;
		}

		glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
		glBufferData(GL_ARRAY_BUFFER, instanceDataBuffer, GL_STATIC_DRAW);

		glDrawElementsInstanced(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0, gameItems.size());
		Renderer.drawCalls++;

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
}