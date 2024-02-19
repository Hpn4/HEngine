package hengine.engine.graph;

import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;

public abstract class AbstractTexture {

	protected final int id;

	public AbstractTexture() {
		id = glGenTextures();
	}
	
	public AbstractTexture(final int id) {
		this.id = id;
	}

	public abstract void bind();

	public int getId() {
		return id;
	}

	public void cleanup() {
		glDeleteTextures(id);
	}
}
