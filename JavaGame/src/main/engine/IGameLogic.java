package main.engine;

import main.engine.graphics.IRenderer;

public interface IGameLogic {

    void init(Window window, Scene scene, IRenderer render) throws Exception;
    
    void input(Window window, Scene scene, long diffTimeMillis);

    void update(double interval, Window window);
    
    void render(Window window, Scene scene, IRenderer renderer);
    
    void cleanup(IRenderer renderer);
}