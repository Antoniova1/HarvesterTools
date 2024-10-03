package org.frags.harvestertools.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.utils.ToolUtils;

public class PrepareToolsListener implements Listener {


    @EventHandler
    public void onEnchant(EnchantItemEvent e) {
        ItemStack item = e.getItem();
        if (item.getType() == Material.AIR)
            return;
        if (ToolUtils.isTool(item)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        ItemStack itemStack = e.getInventory().getItem(0);
        if (itemStack == null)
            return;
        if (itemStack.getType() == Material.AIR)
            return;
        if (ToolUtils.isTool(itemStack)) {
            e.setResult(null);
        }
    }

    @EventHandler
    public void onAnvilClick(InventoryClickEvent e) {
        if (e.getInventory() instanceof AnvilInventory) {
            ItemStack item = e.getCurrentItem();
            if (item == null)
                return;
            if (item.getType() == Material.AIR)
                return;
            if (ToolUtils.isTool(item)) {
                e.setCancelled(true);
            }
        }
    }
}
