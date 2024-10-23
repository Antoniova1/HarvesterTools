package org.frags.harvestertools.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.events.ObtainEssenceEvent;

public class ObtainEssenceListener implements Listener {

    private final HarvesterTools plugin;

    public ObtainEssenceListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onObtainedEssence(ObtainEssenceEvent e) {
        plugin.getEssenceManager().addEssence(e.getPlayer(), e.getAmount());
    }
}
