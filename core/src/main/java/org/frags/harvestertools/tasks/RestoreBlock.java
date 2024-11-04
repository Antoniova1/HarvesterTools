package org.frags.harvestertools.tasks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public record RestoreBlock(Block block, Material newMaterial, long time) implements Comparable<RestoreBlock> {

    public void restoreCrop() {
        block.setType(newMaterial, false);
    }

    @Override
    public int compareTo(@NotNull RestoreBlock o) {
        return Long.compare(time, o.time);
    }
}
