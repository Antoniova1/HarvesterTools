package org.frags.harvestertools.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.events.ObtainEssenceEvent;
import org.frags.harvestertools.toolsmanagers.ToolManager;

public class ObtainEssenceListener implements Listener {

    private final HarvesterTools plugin;

    public ObtainEssenceListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onObtainedEssence(ObtainEssenceEvent e) {

        ToolManager toolManager = e.getToolManager();

        toolManager.setMoney(e.getAmount());

        plugin.getEssenceManager().addEssence(e.getPlayer(), e.getAmount());
    }
}
