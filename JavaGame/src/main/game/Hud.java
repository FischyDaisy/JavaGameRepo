package main.game;

import java.awt.Font;

import org.joml.Vector4f;

import main.engine.IHud;
import main.engine.Window;
import main.engine.graphics.FontTexture;
import main.engine.items.GameItem;
import main.engine.items.TextItem;

public class Hud implements IHud {

	private static final Font FONT = new Font("Arial", Font.PLAIN, 20);

    private static final String CHARSET = "ISO-8859-1";

    private final GameItem[] gameItems;

    private final TextItem statusTextItem;

    public Hud(String statusText) throws Exception {
        FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.statusTextItem = new TextItem(statusText, fontTexture);
        this.statusTextItem.getMesh().getMaterial().setAmbientColor(new Vector4f(1, 1, 1, 1));

        // Create list that holds the items that compose the HUD
        gameItems = new GameItem[]{statusTextItem};
    }

    public void setStatusText(String statusText) {
        this.statusTextItem.setText(statusText);
    }

    @Override
    public GameItem[] getGameItems() {
        return gameItems;
    }
    
    public void updateSize(Window window) {
    	this.statusTextItem.setPosition(10f, window.getHeight() - 50f, 0);
    }
}