package main.engine.items;

import main.engine.EngineProperties;

import java.lang.foreign.*;

public class GameItemManager implements AutoCloseable {

    private static final long GAMEITEM_BUFFER_SIZE = EngineProperties.INSTANCE.getMaxGameItemBuffer();
    private static final long GAMEITEM_LAYOUT_SIZE = GameItem.LAYOUT.byteSize();
    private MemorySegment gameItems;
    //private final MemorySegment gameItemAnimation; Might change animations to be backed by memory segments too
    private Arena arena;
    private long pos;

    /**
     * Creates a new GameItemManager for creating gameItems from a shared {@code MemorySegment}.
     */
    public GameItemManager() {
        arena = Arena.openShared();
        gameItems = arena.allocateArray(GameItem.LAYOUT, GAMEITEM_BUFFER_SIZE);
    }

    /**
     * Creates a {@code GameItem} with the specified modelId
     * @param modelId {@code String} representing the modelId
     * @return GameItem with specified modelId and a unique slice of
     * the managers internal {@code MemorySegment}
     */
    public GameItem createGameItem(String modelId) {
        GameItem item = new GameItem(modelId,
                gameItems.asSlice(pos, GAMEITEM_LAYOUT_SIZE));
        pos += GAMEITEM_LAYOUT_SIZE;
        return item;
    }

    /**
     * Creates a list of GameItems using the list of modelIds
     * @param modelIds {@code String[]} the contains the modelIds of each {@code GameItem}
     * @return {@code GameItem[]} containing items created from the list of modelIds
     */
    public GameItem[] createGameItems(String... modelIds) {
        GameItem[] items =  new GameItem[modelIds.length];
        for (int i = 0; i < modelIds.length; i++) {
            GameItem item = new GameItem(modelIds[i],
                    gameItems.asSlice(pos, GAMEITEM_LAYOUT_SIZE));
            pos += GAMEITEM_LAYOUT_SIZE;
            items[i] = item;
        }
        return items;
    }

    /**
     * Creates a list of GameItems with the specified modelId and count
     * @param modelId {@code String} containing modelId shared amongst the items
     * @param count the number of GameItems to be created
     * @return {@code GameItem[]} containing the specified number of items
     * that have the same modelId
     */
    public GameItem[] createGameItems(String modelId, int count) {
        GameItem[] items =  new GameItem[count];
        for (int i = 0; i < count; i++) {
            GameItem item = new GameItem(modelId,
                    gameItems.asSlice(pos, GAMEITEM_LAYOUT_SIZE));
            pos += GAMEITEM_LAYOUT_SIZE;
            items[i] = item;
        }
        return items;
    }

    /**
     * Sets the position of the internal MemorySegment back to 0, closes the internal Arena,
     * and creates a new Arena and MemorySegment.
     * All previously created GameItems and MemorySegments from this manager should be discarded
     * as the owning Arena will be closed invalidating the backing MemorySegment.
     */
    public void resetBuffer() {
        arena.close();
        arena = Arena.openShared();
        gameItems = arena.allocateArray(GameItem.LAYOUT, GAMEITEM_BUFFER_SIZE);
        pos = 0;
    }

    /**
     * Closes the GameItemManager freeing the MemorySegment backing the GameItems.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        arena.close();
    }
}
