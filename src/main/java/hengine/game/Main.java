package hengine.game;

import hengine.engine.GameEngine;
import hengine.engine.IGameLogic;
import hengine.engine.hlib.component.HWindow;

public class Main {

    public static void main(final String[] args) {
        try {
            final HWindow.WindowOptions opts = new HWindow.WindowOptions();

            opts.cullFace = true;
            opts.antialiasing = true;

            final GameEngine gameEng = new GameEngine("GAME", opts, new DummyGame());
            gameEng.run();

        } catch (final Exception excp) {
            excp.printStackTrace();
        }
    }
}
