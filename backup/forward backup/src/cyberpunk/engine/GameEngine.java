package hengine.engine;

import java.util.ArrayList;
import java.util.List;

import hengine.engine.hlib.component.HWindow;
import hengine.engine.utils.Timer;

public class GameEngine implements Runnable {

	public static final int TARGET_FPS = 60;

	public static final int TARGET_UPS = 30;

	private final HWindow window;

	private final Timer timer;

	private final IGameLogic gameLogic;

	private final MouseInput mouseInput;

	private double lastFps;

	private int fps;

	private String windowTitle;

	public GameEngine(final String windowTitle, final boolean vSync, final HWindow.WindowOptions opts,
			final IGameLogic gameLogic) throws Exception {
		this(windowTitle, 0, 0, vSync, opts, gameLogic);
	}

	public GameEngine(final String windowTitle, final int width, final int height, final boolean vSync,
			final HWindow.WindowOptions opts, final IGameLogic gameLogic) throws Exception {
		this.windowTitle = windowTitle;
		window = new HWindow(windowTitle, width, height, vSync, opts);
		mouseInput = new MouseInput();
		this.gameLogic = gameLogic;
		timer = new Timer();
	}

	@Override
	public void run() {
		try {
			init();
			gameLoop();
		} catch (final Exception excp) {
			excp.printStackTrace();
		} finally {
			cleanup();
		}
	}

	protected void init() throws Exception {
		window.init();
		timer.init();
		gameLogic.init(window);
		lastFps = timer.getTime();
		fps = 0;
	}

	protected void gameLoop() {
		float elapsedTime;
		float accumulator = 0f;
		float interval = 1f / TARGET_UPS;

		final List<Integer> timeL = new ArrayList<>();
		boolean running = true;
		while (running && !window.windowShouldClose()) {
			final long time = System.currentTimeMillis();
			elapsedTime = timer.getElapsedTime();
			accumulator += elapsedTime;

			input();

			while (accumulator >= interval) {
				update(interval);
				accumulator -= interval;
			}

			render();

			if (!window.isvSync())
				sync();

			timeL.add((int) (System.currentTimeMillis() - time));

			final int size = timeL.size();
			if (size > 200) {

				int moy = 0;
				for (int i = 0, c = size; i < c; i++)
					moy += timeL.get(i);

				moy /= size;
				System.out.println("temps totale sur 200 loop : " + moy + " ms");
				timeL.clear();
			}
		}
	}

	protected void cleanup() {
		gameLogic.cleanup();
		window.destroy();
	}

	private void sync() {
		final float loopSlot = 1f / TARGET_FPS;
		final double endTime = timer.getLastLoopTime() + loopSlot;

		while (timer.getTime() < endTime) {
			try {
				Thread.sleep(1);
			} catch (final InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	protected void input() {
		window.mouseInput(mouseInput);
		gameLogic.input(window, mouseInput);
	}

	protected void update(final float interval) {
		gameLogic.update(interval, mouseInput, window);
	}

	protected void render() {
		if (timer.getLastLoopTime() - lastFps > 1) {
			lastFps = timer.getLastLoopTime();
			window.setWindowTitle(windowTitle + " - " + fps + " FPS");
			fps = 0;
		}

		fps++;

		gameLogic.render(window);

		window.update();
	}
}
