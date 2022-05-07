package main.engine;

import main.engine.graphics.Renderer;

public interface IGameLogic {
	
	void init(Window window, Scene scene, Renderer renderer) throws Exception;
    
    void input(Window window, Scene scene, long diffTimeMillis);

    void update(double interval, Window window);
    
    void render(Window window, Scene scene);
    
    void cleanup();
}