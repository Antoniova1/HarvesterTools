package org.frags.harvestertools.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.events.ObtainMoneyEvent;
import org.frags.harvestertools.toolsmanagers.ToolManager;

public class ObtainMoneyListener implements Listener {

    private final HarvesterTools plugin;

    public ObtainMoneyListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void obtainMoney(ObtainMoneyEvent e) {

        ToolManager toolManager = e.getToolManager();

        toolManager.setMoney(e.getAmount());

        plugin.getEcon().depositPlayer(e.getPlayer(), e.getAmount());
    }



}
