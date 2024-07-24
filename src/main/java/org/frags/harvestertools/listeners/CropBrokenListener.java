package org.frags.harvestertools.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.frags.harvestertools.HarvesterTools;

public class CropBrokenListener implements Listener {

    private final HarvesterTools plugin;

    public CropBrokenListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCropBroken(BlockBreakEvent e) {
        if (!(e.getBlock() instanceof Ageable))
            return;

    }
}
