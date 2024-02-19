package hengine.engine.graph.mesh;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.function.Consumer;

import hengine.engine.graph.AbstractTexture;
import hengine.engine.graph.light.Material;
import hengine.engine.item.GameItem;
import hengine.engine.utils.Box3D;

public abstract class AbstractMesh {

	/** L'id (pointeur) du VAO (Vertex Array Object) */
	protected int vaoId;

	/**
	 * La liste des ids de toute les Array (postions, normals, indices, textures...)
	 */
	protected List<Integer> vboIdList;

	/** Le materiel utilisé pour cette figure */
	protected Material material;

	protected Box3D box;

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(final Material material) {
		this.material = material;
	}

	public Box3D getBox() {
		return box;
	}

	protected void initRender() {
		final AbstractTexture texture = material != null ? material.getTexture() : null;
		if (texture != null) {
			// Activate first texture bank
			glActiveTexture(GL_TEXTURE0);
			// Bind la texture
			texture.bind();
		}
		final AbstractTexture normalMap = material != null ? material.getNormalMap() : null;
		if (normalMap != null) {
			// Activate second texture bank
			glActiveTexture(GL_TEXTURE1);
			// Bind la texture
			normalMap.bind();
		}

		glBindVertexArray(getVaoId());
		glEnableVertexAttribArray(0);
	}

	public abstract void render();

	protected void endRender() {
		// Restore state
		glBindVertexArray(0);
		glDisableVertexAttribArray(0);

		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public abstract void renderList(final List<GameItem> gameItems, final Consumer<GameItem> consumer);

	public void cleanUp() {
		glDisableVertexAttribArray(0);

		// Delete the VBOs
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		for (int vboId : vboIdList)
			glDeleteBuffers(vboId);

		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoId);
	}

	public int getVaoId() {
		return vaoId;
	}

	public abstract FloatBuffer getVertices();
}
