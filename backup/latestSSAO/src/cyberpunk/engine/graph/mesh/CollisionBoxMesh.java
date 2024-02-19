package hengine.engine.graph.mesh;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import hengine.engine.graph.renderer.Renderer;
import hengine.engine.utils.Box3D;
import hengine.engine.world.item.GameItem;

public class CollisionBoxMesh extends AbstractMesh {

	private final int vertexCount;
	public CollisionBoxMesh(final Box3D box) {
		FloatBuffer posBuffer = null;
		IntBuffer indicesBuffer = null;

		try {
			vboIdList = new ArrayList<>();

			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);

			// Position VBO
			int vboId = glGenBuffers();
			vboIdList.add(vboId);

			final Vector3f min = box.origin, max = box.dim;
			final float[] pos2 = new float[] {
					min.x, min.y, max.z,
					max.x, min.y, max.z,
					max.x, max.y, max.z,
					min.x, max.y, max.z,
					
					min.x, min.y, min.z,
					max.x, min.y, min.z,
					max.x, max.y, min.z,
					min.x, max.y, min.z
			};
			posBuffer = MemoryUtil.memAllocFloat(pos2.length);
			posBuffer.put(pos2).flip();
			System.out.println(box);

			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

			// Index VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			
			final int[] indices = new int[] {
					0, 1, 2, 2, 3, 0,
			        3, 2, 6, 6, 7, 3,
			        7, 6, 5, 5, 4, 7,
			        4, 0, 3, 3, 7, 4,
			        0, 1, 5, 5, 4, 0,
			        1, 5, 6, 6, 2, 1
			};
			
			vertexCount = indices.length;
			indicesBuffer = MemoryUtil.memAllocInt(indices.length);
			indicesBuffer.put(indices).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		} finally {
			if (posBuffer != null)
				MemoryUtil.memFree(posBuffer);

			if (indicesBuffer != null)
				MemoryUtil.memFree(indicesBuffer);
		}
	}

	@Override
	public void render(boolean withTexture) {
		initRender(withTexture);
		
		glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
		Renderer.drawCalls++;
		
		endRender(withTexture);
	}

	@Override
	public void renderList(List<GameItem> gameItems, Consumer<GameItem> consumer, boolean withTexture) {
		initRender(withTexture);

		for (final GameItem gameItem : gameItems) {
			if (gameItem.isInsideFrustum()) {

				// Met a jour les informations de l'objet
				consumer.accept(gameItem);

				// Dessine l'objet
				glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
				Renderer.drawCalls++;
			}
		}

		endRender(withTexture);
	}

	@Override
	public FloatBuffer getVertices() {
		// TODO Auto-generated method stub
		return null;
	}

}
