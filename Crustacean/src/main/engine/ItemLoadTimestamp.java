package main.engine;

import dev.dominion.ecs.api.Dominion;

public class ItemLoadTimestamp {
    public long gameItemLoadedTimestamp;

    public ItemLoadTimestamp() {
        gameItemLoadedTimestamp = 0L;
    }

    public static ItemLoadTimestamp getTimeStamp(Dominion dominion) {
        return dominion.findEntitiesWith(ItemLoadTimestamp.class).iterator().next().comp();
    }
}
