package org.frags.harvestertools.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frags.harvestertools.HarvesterTools;

public class LeaveListener implements Listener {

    private final HarvesterTools plugin;

    public LeaveListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        plugin.getEssenceManager().unload(e.getPlayer());
    }

}
