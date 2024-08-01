package org.frags.harvestertools.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.essence.EssenceManager;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.utils.ToolUtils;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;

public class InteractListener implements Listener {

    private final HarvesterTools plugin;

    public InteractListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEssenceItemClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        //Only right clicks
        ItemStack itemStack = e.getItem();
        if (itemStack == null)
            return;
        if (!itemStack.hasItemMeta())
            return;
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        if (!container.has(ToolUtils.essenceItemKey, PersistentDataType.DOUBLE))
            return;
        double amount = container.get(ToolUtils.essenceItemKey, PersistentDataType.DOUBLE);
        String formattedAmount = Utils.formatNumber(new BigDecimal(amount));

        Player player = e.getPlayer();
        player.getInventory().remove(itemStack);
        plugin.getEssenceManager().addEssence(player, amount);

        MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("essence-redeem")
                .replace("%amount%", formattedAmount));
    }
}
