package org.frags.harvestertools;

import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NMSHandler {

    void replenishCrop(Block block, HarvesterTools plugin);
    List<ItemStack> getDrops(Block block);

    void regenBlock(Block block, HarvesterTools plugin);

}
