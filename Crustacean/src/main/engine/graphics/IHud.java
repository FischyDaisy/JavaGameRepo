package main.engine.graphics;

import main.engine.Window;

public interface IHud {
	
	int BUFFER_INITIAL_SIZE = 4 * 1024;
    int MAX_VERTEX_BUFFER  = 512 * 1024;
    int MAX_ELEMENT_BUFFER = 128 * 1024;
    
    public void input(Window window);
    
    public void render(Window window);

    public void cleanup();
}