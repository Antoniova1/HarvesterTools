package org.frags.harvestertools.nms_1_21_4;

import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.NMSHandler;

import java.util.Collections;
import java.util.List;

public class NMSHandlerImpl implements NMSHandler {

    @Override
    public void replenishCrop(Block block, HarvesterTools plugin) {
        // Implementa según sea necesario
    }

    @Override
    public List<ItemStack> getDrops(Block block) {
        // Implementa según sea necesario
        return Collections.emptyList();
    }

    @Override
    public void regenBlock(Block block, HarvesterTools plugin) {
        // Implementa según sea necesario
    }
}
