package org.frags.harvestertools.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.menusystem.menu.ToolMenu;
import org.frags.harvestertools.enums.Tools;

import static org.frags.harvestertools.HarvesterTools.*;

public class OpenMenuListener implements Listener {

    private final HarvesterTools plugin;

    public OpenMenuListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null)
            return;
        if (!item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(getInstance(), "tool"), PersistentDataType.STRING))
            return;
        //The item clicked is a tool
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String toolTypeString =  container.get(new NamespacedKey(getInstance(), "tool"), PersistentDataType.STRING);

        //This cannot be null
        assert toolTypeString != null;
        Tools toolType = Tools.getTool(toolTypeString);

        //Open menu
        if (player.isSneaking()) {
            new ToolMenu(plugin, createPlayerMenuUtility(player, toolType, item)).open();
        }
    }
}
