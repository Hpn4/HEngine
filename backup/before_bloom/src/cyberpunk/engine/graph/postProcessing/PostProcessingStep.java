package hengine.engine.graph.postProcessing;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import hengine.engine.graph.ShaderProgram;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.utils.loader.StaticMeshesLoader;

public class PostProcessingStep {

	public static Mesh quad;

	protected ShaderProgram shader;

	private final PostProcessingBuffer buffer;

	public PostProcessingStep(final HWindow window) throws Exception {
		buffer = new PostProcessingBuffer(window);
	}

	protected void startRender() {
		buffer.bindFrameBuffer();
		//glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glClear(GL_COLOR_BUFFER_BIT);
	}
	
	public void render() {
		quad.render();
	}
	
	protected void endRender() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public PostProcessingBuffer getBuffer() {
		return buffer;
	}

	public void cleanUp() {
		if(shader != null)
			shader.cleanup();
		
		buffer.cleanup();
	}
	
	public static void initMesh() throws Exception {
		if (quad == null)
			quad = StaticMeshesLoader.load("models/buffer_pass.obj", "")[0];
	}
	
	public static void cleanUpMesh() {
		quad.cleanUp();
	}
}
