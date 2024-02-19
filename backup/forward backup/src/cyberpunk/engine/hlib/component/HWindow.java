package hengine.engine.hlib.component;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_REFRESH_RATE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateCursor;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyCursor;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetKeyName;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwMaximizeWindow;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.util.Objects;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryStack;

import hengine.engine.MouseInput;
import hengine.engine.graph.Texture;
import hengine.engine.hlib.event.KeyEvent;
import hengine.engine.hlib.event.MouseEvent;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.graphics.paint.Color;
import hengine.engine.hlib.graphics.paint.ImageCache;
import hengine.engine.hlib.utils.HLib;

public class HWindow extends HContainer {

	/**
	 * Field of View in Radians
	 */
	public static final float FOV = (float) Math.toRadians(60.0f);

	/**
	 * Distance to the near plane
	 */
	public static final float Z_NEAR = 0.001f;

	/**
	 * Distance to the far plane
	 */
	public static final float Z_FAR = 175.f;

	public static int initWidth, initHeight, frameWidth, frameHeight;

	private final Matrix4f projectionMatrix;

	private final String title;

	private WindowOptions opts;

	private MouseEvent event;

	private Graphics g;

	private long windowHandle;

	private long cursor;

	private boolean resized;

	private boolean vSync;

	public HWindow(final String title, final int width, final int height, final boolean vSync,
			final WindowOptions opts) {
		this.title = title;

		frameWidth = initWidth = width;
		frameHeight = initHeight = height;

		this.vSync = vSync;
		resized = false;
		this.opts = opts;
		projectionMatrix = new Matrix4f();
	}

	public void init() throws Exception {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		glfwWindowHint(GLFW_REFRESH_RATE, GLFW_DONT_CARE);

		glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		// glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);

		boolean maximized = false;
		// If no size has been specified set it to maximized state
		if (frameWidth == 0 || frameHeight == 0) {
			// Set up a fixed width and height so window initialization does not fail
			frameWidth = 100;
			frameHeight = 100;
			glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
			maximized = true;
		}

		// Create the window
		windowHandle = glfwCreateWindow(frameWidth, frameHeight, title, NULL, NULL);
		if (windowHandle == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		// Make the OpenGL context current
		glfwMakeContextCurrent(windowHandle);

		// Setup resize callback
		glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
			setResized(true);
			setSize(width, height);
		});

		// Setup a key callback. It will be called every time a key is pressed, repeated
		// or released.
		glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
			KeyEvent.action = action;
			KeyEvent.mods = mods;
			KeyEvent.key = key;
			if (glfwGetKeyName(key, scancode) == null)
				HLib.fireKeyEvent(new KeyEvent((char) -1));

			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			}
		});

		glfwSetCursorPosCallback(windowHandle, (window, xPos, yPos) -> {
			event = new MouseEvent(xPos, yPos);
			fireEvent(event);
		});

		glfwSetMouseButtonCallback(windowHandle, (window, button, actions, mods) -> {
			event = new MouseEvent(button, actions, mods);
			fireEvent(event);
		});

		glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) -> {
			fireEvent(new MouseEvent(xoffset, yoffset, true));
		});

		glfwSetCharCallback(windowHandle, (window, codepoint) -> {
			HLib.fireKeyEvent(new KeyEvent((char) codepoint));
		});

		final GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		if (maximized) {
			glfwMaximizeWindow(windowHandle);

			try (final MemoryStack stack = MemoryStack.stackPush()) {
				final IntBuffer w = stack.ints(1), h = stack.ints(1);
				glfwGetFramebufferSize(windowHandle, w, h);

				setSize(w.get(), h.get());
			}
		} else
			glfwSetWindowPos(windowHandle, (vidmode.width() - frameWidth) / 2, (vidmode.height() - frameHeight) / 2);

		initWidth = vidmode.width();
		initHeight = vidmode.height();

		// Enable v-sync
		if (isvSync())
			glfwSwapInterval(1);
		glfwSwapInterval(0);

		// Make the window visible
		glfwShowWindow(windowHandle);

		GL.createCapabilities();

		// Set the clear color
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_STENCIL_TEST);
		if (opts.showTriangles) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		}

		// Support for transparencies
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		if (opts.cullFace) {
			glEnable(GL_CULL_FACE);
			glCullFace(GL_BACK);
		}

		// Antialiasing
		if (opts.antialiasing) {
			glfwWindowHint(GLFW_SAMPLES, 4);
		}

		g = new Graphics();
		g.init(this);

		setResized(true);
		
		GLUtil.setupDebugMessageCallback(System.err);
	}

	public void mouseInput(final MouseInput mouse) {
		mouse.input(event);
	}

	public void restoreState() {
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_STENCIL_TEST);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		if (opts.cullFace) {
			glEnable(GL_CULL_FACE);
			glCullFace(GL_BACK);
		}
	}

	public void setClearColor(final float r, final float g, final float b, final float alpha) {
		glClearColor(r, g, b, alpha);
	}

	public void update() {
		glfwSwapBuffers(windowHandle);
		glfwPollEvents();
		recalcSize(g);
	}

	public void render() {
		g.startRendering(this);
		g.moveOriginTo(0, 0);
		paint(g);
		g.moveOriginTo(0, 0);
		g.endRendering(this);
	}
	
	public Graphics getGraphics() {
		return g;
	}

	public boolean isKeyPressed(final int keyCode) {
		return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
	}

	public boolean windowShouldClose() {
		return glfwWindowShouldClose(windowHandle);
	}

	public long getWindowHandle() {
		return windowHandle;
	}

	public WindowOptions getOptions() {
		return opts;
	}

	public String getTitle() {
		return title;
	}

	public void setWindowTitle(String title) {
		glfwSetWindowTitle(windowHandle, title);
	}

	public int getWidth() {
		return frameWidth;
	}

	public int getHeight() {
		return frameHeight;
	}

	public boolean isResized() {
		return resized;
	}

	public void setResized(final boolean resized) {
		this.resized = resized;
	}

	public boolean isvSync() {
		return vSync;
	}

	public void setvSync(final boolean vSync) {
		this.vSync = vSync;
	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public Matrix4f updateProjectionMatrix() {
		float aspectRatio = (float) frameWidth / (float) frameHeight;
		return projectionMatrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
	}

	public void setCursor(final Texture image) {
		cursor = glfwCreateCursor(GLFWImage.malloc().set(image.getWidth(), image.getHeight(), image.getImage()), 0, 0);

		if (cursor == NULL)
			throw new RuntimeException("Error creating cursor");

		// Set the cursor on a window
		glfwSetCursor(windowHandle, cursor);
	}

	public void destroy() {
		// Clean texture, font and image of each component
		final long ctx = g.getContext();
		cleanUp(ctx);

		HComponent.cleanUpStatic(ctx);
		Color.cleanUpStatic(ctx);
		ImageCache.cleanUp(ctx);

		g.cleanUp();

		if (cursor != 0)
			glfwDestroyCursor(cursor);
		glfwFreeCallbacks(windowHandle);
		glfwDestroyWindow(windowHandle);
		glfwTerminate();

		Objects.requireNonNull(glfwSetErrorCallback(null)).free();
	}

	public void setPos(final int x, final int y) {
		glfwSetWindowPos(windowHandle, x, y);
	}

	public void setSize(final int w, final int h) {
		super.setSize(w, h);
		frameWidth = w;
		frameHeight = h;
	}

	public static class WindowOptions {

		public boolean cullFace;

		public boolean showTriangles;

		public boolean showFps;

		public boolean antialiasing;
	}
}