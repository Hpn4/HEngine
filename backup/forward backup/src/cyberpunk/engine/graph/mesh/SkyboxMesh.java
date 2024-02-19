package hengine.engine.graph.mesh;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
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

import hengine.engine.graph.AbstractTexture;
import hengine.engine.graph.Renderer;
import hengine.engine.graph.TextureCube;
import hengine.engine.item.GameItem;
import hengine.engine.utils.Box3D;

/**
 * Contient toute les informations nécessaire pour déssiner une figure Elle
 * s'occupe aussi de charger le VAO de la figure en fonction de toute les listes
 * d'objet passé en parametre
 * 
 * @author Hpn4
 *
 */
public class SkyboxMesh extends AbstractMesh {

	/** Le nombre de vertex a désinner */
	private final int vertexCount;

	private TextureCube secondSky;

	public SkyboxMesh(final float[] positions, final int[] indices) {
		FloatBuffer posBuffer = null;
		IntBuffer indicesBuffer = null;

		box = new Box3D(new Vector3f(0), new Vector3f(0));
		try {
			vertexCount = indices.length;
			vboIdList = new ArrayList<>();

			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);

			// Position VBO
			int vboId = glGenBuffers();
			vboIdList.add(vboId);

			posBuffer = MemoryUtil.memAllocFloat(positions.length);
			posBuffer.put(positions);
			posBuffer.flip();
			posBuffer.position(0);

			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

			// Index VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);

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

	public void setSecondSky(final TextureCube sky) {
		secondSky = sky;
	}

	public TextureCube getSecondSky() {
		return secondSky;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	protected void initRender() {
		final AbstractTexture texture = material != null ? material.getTexture() : null;
		if (texture != null) {
			// Activate first texture bank
			glActiveTexture(GL_TEXTURE0);
			// Bind la texture
			texture.bind();
		}
		if (secondSky != null) {
			// Activate second texture bank
			glActiveTexture(GL_TEXTURE1);
			// Bind la texture
			secondSky.bind();
		}

		glBindVertexArray(getVaoId());
		glEnableVertexAttribArray(0);
	}

	public void render() {
		initRender();

		glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
		Renderer.drawCalls++;

		endRender();
	}

	public void renderList(final List<GameItem> gameItems, final Consumer<GameItem> consumer) {
	}

	@Override
	public FloatBuffer getVertices() {
		return null;
	}
}
