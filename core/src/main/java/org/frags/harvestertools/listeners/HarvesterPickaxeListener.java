package org.frags.harvestertools.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.utils.ToolUtils;

public class HarvesterPickaxeListener implements Listener {

    private final HarvesterTools plugin;

    public HarvesterPickaxeListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();

        Block block = e.getBlock();

        String worldName = player.getLocation().getWorld().getName();
        if (itemStack == null || itemStack.getType() == Material.AIR) return;

        if (!ToolUtils.isTool(itemStack)) return;

        if (ToolUtils.getTool(itemStack) != Tools.pickaxe) return;

        if (!plugin.getConfig().getStringList("mining-worlds").contains(worldName)) return;

        if (plugin.getBlockManager().getBlock(block.getType().name()) == null) return;

        //Do stuff

        org.frags.harvestertools.objects.Block customBlock = plugin.getBlockManager().getBlock(block.getType().name());

        plugin.getPickaxeUtils().calculateAutoSellDrops(itemStack, player, customBlock, block.getDrops());

        plugin.getPickaxeUtils().procHaste(player, itemStack);

        plugin.getPickaxeUtils().procSpeed(player, itemStack);

        plugin.getPickaxeUtils().addExperience(player, itemStack);

        plugin.getPickaxeUtils().calculateBoosters(player, itemStack);

        plugin.getPickaxeUtils().procCustomEnchants(player, itemStack);

        e.setDropItems(false);
    }
}
