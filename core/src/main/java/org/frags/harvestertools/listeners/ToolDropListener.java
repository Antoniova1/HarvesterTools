package org.frags.harvestertools.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.utils.ToolUtils;

public class ToolDropListener implements Listener {

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack item = e.getItemDrop().getItemStack();

        if (ToolUtils.isTool(item)) {
            //Cancell drop
            e.setCancelled(true);
        }
    }
}
