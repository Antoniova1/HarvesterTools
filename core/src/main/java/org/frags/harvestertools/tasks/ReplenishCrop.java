package org.frags.harvestertools.tasks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record ReplenishCrop(Block block, BlockData agedData, long time) implements Comparable<ReplenishCrop> {


    public void restoreCrop() {
        World w = block.getWorld();

        block.setBlockData(agedData, false);
    }

    @Override
    public int compareTo(@NotNull ReplenishCrop o) {
        return Long.compare(time, o.time);
    }
}
