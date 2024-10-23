package org.frags.harvestertools.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.utils.ToolUtils;

import java.util.*;

public class ToolDropListener implements Listener {

    private HashMap<UUID, List<ItemStack>> savedItems = new HashMap<>();

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack item = e.getItemDrop().getItemStack();

        if (ToolUtils.isTool(item)) {
            //Cancell drop
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        UUID uuid = e.getEntity().getUniqueId();

        if (e.getKeepInventory())
            return;

        List<ItemStack> items = new ArrayList<>();
        List<ItemStack> itemsToRemove = new ArrayList<>();

        for (ItemStack item : e.getDrops()) {
            if (item != null && ToolUtils.isTool(item)) {
                items.add(item);
                itemsToRemove.add(item);
            }
        }

        if (!itemsToRemove.isEmpty()) {
            e.getDrops().removeAll(itemsToRemove);
        }

        if (!items.isEmpty()) {
            savedItems.put(uuid, items);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        if (savedItems.containsKey(uuid)) {
            List<ItemStack> items = savedItems.get(uuid);

            for (ItemStack item : items) {
                e.getPlayer().getInventory().addItem(item);
            }

            savedItems.remove(uuid);
        }
    }
}
