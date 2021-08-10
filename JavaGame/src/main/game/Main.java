package main.game;

import main.engine.GameEngine;
import main.engine.IGameLogic;
import main.engine.Window;
public class Main {
 
    public static void main(String[] args) {
        try {
            IGameLogic gameLogic = new Game();
            Window.WindowOptions opts = new Window.WindowOptions();
            opts.showFps = true;
            GameEngine gameEng = new GameEngine("GAME", opts, gameLogic);
            gameEng.start();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}