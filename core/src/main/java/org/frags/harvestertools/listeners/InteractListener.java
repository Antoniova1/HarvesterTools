package org.frags.harvestertools.listeners;

import org.bukkit.configuration.ConfigurationSection;
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
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enums.ToolMode;
import org.frags.harvestertools.enums.Tools;
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

    @EventHandler
    public void changeSellMode(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!player.isSneaking())
            return;
        ItemStack itemStack = e.getItem();
        if (itemStack == null)
            return;
        if (!itemStack.hasItemMeta())
            return;
        if (!ToolUtils.isTool(itemStack))
            return;
        Tools tools = ToolUtils.getTool(itemStack);
        CustomEnchant autosell = plugin.getEnchantsManager().getEnchant("autosell", tools);
        if (!plugin.getEnchantsManager().hasEnchantment(itemStack, autosell))
            return;
        if (tools == Tools.rod) {
            if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK)
                return;
            //Only left clicks

            //Change autosell mode
            ToolMode mode = ToolUtils.changeSellMode(itemStack);
            sendChangeMessage(player, mode);
            ToolUtils.updateVariables(itemStack);
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;


        ToolMode mode = ToolUtils.changeSellMode(itemStack);
        sendChangeMessage(player, mode);
        ToolUtils.updateVariables(itemStack);
    }


    private void sendChangeMessage(Player player, ToolMode mode) {
        ConfigurationSection section = plugin.hoeEnchantsFile.getConfig().getConfigurationSection("CustomEnchants.autosell.change");
        //Send Title
        ConfigurationSection title = section.getConfigurationSection("title");
        if (title.getBoolean("enabled")) {
            String titleMessage = MessageManager.miniStringParse(title.getString("title")).replace("%mode%", mode.toString());
            String subtitleMessage = MessageManager.miniStringParse(title.getString("subtitle")).replace("%mode%", mode.toString());
            int fadeIn = title.getInt("fadeIn");
            int fadeOut = title.getInt("fadeOut");
            int time = title.getInt("time");
            player.sendTitle(titleMessage, subtitleMessage, fadeIn, time, fadeOut);
        }
        MessageManager.miniMessageSender(player, section.getString("message").replace("%mode%", mode.toString()));
    }
}
