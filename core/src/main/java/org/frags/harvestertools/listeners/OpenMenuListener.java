package org.frags.harvestertools.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.menusystem.toolmenus.ToolMenu;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.utils.ToolUtils;

import java.util.HashMap;
import java.util.UUID;

import static org.frags.harvestertools.HarvesterTools.*;

public class OpenMenuListener implements Listener {

    private final HarvesterTools plugin;

    public OpenMenuListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    private final HashMap<UUID, Long> lastRightClickTime = new HashMap<>();

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (player.isSneaking() || item == null || !ToolUtils.isTool(item)) {
            return;
        }


        Tools toolType = ToolUtils.getTool(item);


        if (toolType == Tools.rod) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
                lastRightClickTime.put(player.getUniqueId(), System.currentTimeMillis());
                return;
            }

            if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                Long lastClick = lastRightClickTime.get(player.getUniqueId());
                if (lastClick != null && (System.currentTimeMillis() - lastClick) < 200) {
                    return; //System to avoid right click double proc
                }
                new ToolMenu(plugin,createPlayerMenuUtility(player, toolType, item)).open();
            }
            return;
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            new ToolMenu(plugin, createPlayerMenuUtility(player, toolType, item)).open();
        }
    }
}
