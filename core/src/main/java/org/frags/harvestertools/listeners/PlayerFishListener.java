package org.frags.harvestertools.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enchants.EnchantsManager;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.utils.ToolUtils;

public class PlayerFishListener implements Listener {

    private final HarvesterTools plugin;

    private final EnchantsManager manager;

    public PlayerFishListener(HarvesterTools plugin) {
        this.plugin = plugin;
        this.manager = plugin.getEnchantsManager();
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        ItemStack itemStack = e.getPlayer().getInventory().getItemInMainHand();
        Player player = e.getPlayer();
        if (itemStack == null || itemStack.getType() == Material.AIR)
            itemStack = player.getInventory().getItemInOffHand();
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;

        if (!ToolUtils.isTool(itemStack))
            return;
        if (e.getState() == PlayerFishEvent.State.FISHING) {
            //Player cast the rod
            plugin.getFishingUtils().fastRod(e, itemStack);
        }

        if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            //Player fished
            if (e.getCaught() == null)
                return;

            ItemStack drop = ((Item) e.getCaught()).getItemStack();
            Entity entity = e.getCaught();
            entity.remove();

            plugin.getFishingUtils().calculateAutoSellDrops(itemStack, player, drop);

            plugin.getFishingUtils().procTsunami(player, itemStack);

            plugin.getFishingUtils().addExperience(player, itemStack);

            plugin.getFishingUtils().calculateBoosters(player, itemStack);

            plugin.getFishingUtils().procCustomEnchants(player, itemStack);
        }
    }
}
