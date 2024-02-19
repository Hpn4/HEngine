package hengine.game;

import hengine.engine.GameEngine;
import hengine.engine.IGameLogic;
import hengine.engine.hlib.component.HWindow;

public class Cyberpunk {

	public static void main(final String[] args) {
		try {
			final IGameLogic gameLogic = new DummyGame();
			final HWindow.WindowOptions opts = new HWindow.WindowOptions();

			opts.cullFace = true;
			opts.showFps = true;
			opts.antialiasing = true;

			final GameEngine gameEng = new GameEngine("GAME", true, opts, gameLogic);
			gameEng.run();

		} catch (final Exception excp) {
			excp.printStackTrace();
		}
	}
}
