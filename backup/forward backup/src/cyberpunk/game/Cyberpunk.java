package hengine.game;


import hengine.engine.GameEngine;
import hengine.engine.IGameLogic;
import hengine.engine.hlib.component.HWindow;

public class Cyberpunk {

    public static void main(String[] args) {
        try {
            IGameLogic gameLogic = new DummyGame();
            HWindow.WindowOptions opts = new HWindow.WindowOptions();
            opts.cullFace = true;
            opts.showFps = true;
            opts.antialiasing = true;
            GameEngine gameEng = new GameEngine("GAME", true, opts, gameLogic);
            gameEng.run();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}
