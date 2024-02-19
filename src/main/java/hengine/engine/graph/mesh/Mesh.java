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
import static org.lwjgl.opengl.GL15.glGetBufferSubData;
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

/**
 * Contient toute les informations nécessaire pour déssiner une figure Elle
 * s'occupe aussi de charger le VAO de la figure en fonction de toute les listes
 * d'objet passé en parametre
 * 
 * @author Hpn4
 *
 */
public class Mesh extends AbstractMesh {

	public static final int MAX_WEIGHTS = 4;

	/** Le nombre de vertex a désinner */
	protected final int vertexCount;

	private final int posCount;

	private final CollisionBoxMesh boxMesh;

	public Mesh(final float[] positions, final float[] textCoords, final float[] normals, final int[] indices) {
		
		System.out.println("Positions :");
		for(int i = 0; i < positions.length; i+=3) {
			System.out.println(positions[i] + " " + positions[i + 1] + " " + positions[i + 2]);
		}
		
		System.out.println("textCoords :");
		for(int i = 0; i < textCoords.length; i+=2) {
			System.out.println(textCoords[i] + " " + textCoords[i + 1]);
		}
		
		System.out.println("Indices :");
		for(int i = 0; i < indices.length; i+=3) {
			System.out.println(indices[i] + " " + indices[i + 1] + " " + indices[i + 2]);
		}
		
		FloatBuffer posBuffer = null, textCoordsBuffer = null, vecNormalsBuffer = null;
		IntBuffer indicesBuffer = null;

		try {
			vertexCount = indices.length;
			posCount = positions.length;
			vboIdList = new ArrayList<>();

			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);

			// Position VBO
			int vboId = glGenBuffers();
			vboIdList.add(vboId);
			posBuffer = MemoryUtil.memAllocFloat(positions.length);

			final Vector3f min = new Vector3f(Float.MAX_VALUE), max = new Vector3f(Float.MIN_VALUE);
			for (int i = 0, c = positions.length; i < c; i++) {
				final int index = i % 3;
				final float value = positions[i];

				if (value > max.get(index))
					max.setComponent(index, value);
				else if (value < min.get(index))
					min.setComponent(index, value);
			}

			box = new Box3D(min, max);

			posBuffer.put(positions);

			posBuffer.flip();
			posBuffer.position(0);

			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

			// Texture coordinates VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			if (textCoords.length > 0) {
				textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
				textCoordsBuffer.put(textCoords).flip();
			} else
				textCoordsBuffer = MemoryUtil.memAllocFloat(positions.length);

			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(1);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

			// Vertex normals VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			if (normals.length > 0) {
				vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
				vecNormalsBuffer.put(normals).flip();
			} else
				vecNormalsBuffer = MemoryUtil.memAllocFloat(positions.length);

			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(2);
			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

			// Index VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			indicesBuffer = MemoryUtil.memAllocInt(indices.length);

			indicesBuffer.put(indices).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);

			boxMesh = new CollisionBoxMesh(box);
		} finally {
			if (posBuffer != null)
				MemoryUtil.memFree(posBuffer);

			if (textCoordsBuffer != null)
				MemoryUtil.memFree(textCoordsBuffer);

			if (vecNormalsBuffer != null)
				MemoryUtil.memFree(vecNormalsBuffer);

			if (indicesBuffer != null)
				MemoryUtil.memFree(indicesBuffer);
		}
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public void render(final boolean withTexture) {
		initRender(withTexture);

		glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
		Renderer.drawCalls++;

		endRender(withTexture);
	}

	public void renderList(final List<GameItem> gameItems, final Consumer<GameItem> consumer,
			final boolean withTexture) {
		initRender(withTexture);

		for (final GameItem gameItem : gameItems) {
			if (gameItem.isInsideFrustum()) {

				// Met a jour les informations de l'objet
				consumer.accept(gameItem);

				// Dessine l'objet
				glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
				Renderer.drawCalls++;
			}
		}

		endRender(withTexture);
	}

	public FloatBuffer getVertices() {
		final int id = vboIdList.get(0);
		final FloatBuffer buf = MemoryUtil.memAllocFloat(posCount);

		glBindBuffer(GL_ARRAY_BUFFER, id);
		glGetBufferSubData(GL_ARRAY_BUFFER, 0, buf);

		return buf.flip().limit(posCount);
	}

	public CollisionBoxMesh getCollisionBoxMesh() {
		return boxMesh;
	}

	public void cleanup() {
		super.cleanup();
		boxMesh.cleanup();
	}
}
