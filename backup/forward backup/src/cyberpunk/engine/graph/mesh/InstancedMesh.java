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

	protected static final int MATRIX_SIZE_FLOATS = 4 * 4;

	protected static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;

	protected static final int INSTANCE_SIZE_BYTES = MATRIX_SIZE_BYTES + FLOAT_SIZE_BYTES;

	protected static final int INSTANCE_SIZE_FLOATS = MATRIX_SIZE_FLOATS + 1;

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

		int start = 3, strideStart = 0;

		// Model matrix
		for (int i = 0; i < 4; i++) {
			glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
			glVertexAttribDivisor(start, 1);
			glEnableVertexAttribArray(start);
			start++;
			strideStart += VECTOR4F_SIZE_BYTES;
		}

		// Selected
		glVertexAttribPointer(start, 1, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
		glVertexAttribDivisor(start, 1);
		glEnableVertexAttribArray(start);
		start++;

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
		initRender();

		int chunkSize = numInstances;
		int length = gameItems.size();
		
		for (int i = 0; i < length; i += chunkSize) {
			int end = Math.min(length, i + chunkSize);
			List<GameItem> subList = gameItems.subList(i, end);
			renderChunkInstanced(subList);
		}

		endRender();
	}

	private void renderChunkInstanced(final List<GameItem> gameItems) {
		instanceDataBuffer.clear();

		int i = 0;

		final Iterator<GameItem> it = gameItems.iterator();
		while (it.hasNext()) {
			final GameItem gameItem = it.next();

			int buffPos = INSTANCE_SIZE_FLOATS * i;

			gameItem.getModelMatrix().get(buffPos, instanceDataBuffer);

			// Selected
			buffPos += MATRIX_SIZE_FLOATS;
			instanceDataBuffer.put(buffPos, gameItem.isSelected() ? 1 : 0);

			i++;
		}

		glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
		glBufferData(GL_ARRAY_BUFFER, instanceDataBuffer, GL_STATIC_DRAW);

		glDrawElementsInstanced(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0, gameItems.size());
		Renderer.drawCalls++;

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
}