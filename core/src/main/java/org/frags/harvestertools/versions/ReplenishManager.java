package org.frags.harvestertools.versions;

import org.bukkit.block.Block;

public interface ReplenishManager {

    public void replenishCrop(Block block);
    public boolean isFullyGrown(Block block);
}
