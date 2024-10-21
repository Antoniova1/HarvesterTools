package org.frags.harvestertools.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.objects.HarvesterDrops;
import org.frags.harvestertools.toolsmanagers.PickaxeManager;
import org.frags.harvestertools.utils.RandomSystem;
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

        if (!plugin.getConfig().getStringList("mining-worlds").contains(worldName)) return;

        //Farming world, can only break ores with tool

        if (plugin.getBlockManager().getBlock(block.getType().name()) == null)
            return;

        //Ore detected.
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            e.setCancelled(true);
            return;
        }


        if (!ToolUtils.isTool(itemStack)) {
            e.setCancelled(true);
            return;
        }

        if (ToolUtils.getTool(itemStack) != Tools.pickaxe) {
            e.setCancelled(true);
            return;
        }
        //Do stuff

        /*if (ToolUtils.isTool(itemStack)) {
            player.sendMessage(ChatColor.RED + "Los picos especiales están desactivados ahora mismo.");
            e.setCancelled(true);
            return;
        }
         */


        HarvesterDrops customBlock = plugin.getBlockManager().getBlock(block.getType().name());

        int toolLevel = ToolUtils.getItemLevel(itemStack);

        if (toolLevel < customBlock.getRequiredLevel()) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-enough-level").replace("%level%", String.valueOf(customBlock.getRequiredLevel())));
            e.setCancelled(true);
            return;
        }

        if (plugin.getConfig().getBoolean("tools.pickaxe.regen-block")) {
            e.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getNmsHandler().regenBlock(block, plugin);
            }, 2L);
        }

        PickaxeManager pickaxeManager = plugin.getPickaxeManager(player);

        pickaxeManager.calculateAutoSellDrops(itemStack, customBlock, block.getDrops());

        pickaxeManager.procHaste(itemStack);

        pickaxeManager.procSpeed(itemStack);

        pickaxeManager.addToolExperience(itemStack);

        pickaxeManager.procCustomEnchants(itemStack);

        RandomSystem randomSystem = new RandomSystem();

        if (randomSystem.success(0.01, false))
            ToolUtils.updateVariables(itemStack);

        e.setDropItems(false);
    }
}
