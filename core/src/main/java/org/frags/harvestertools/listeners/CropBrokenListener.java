package org.frags.harvestertools.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.CropsManager;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.objects.HarvesterDrops;
import org.frags.harvestertools.toolsmanagers.HoeManager;
import org.frags.harvestertools.utils.RandomSystem;
import org.frags.harvestertools.utils.ToolUtils;

public class CropBrokenListener implements Listener {

    private final HarvesterTools plugin;

    public CropBrokenListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    //Cannot make diferent EventHandlers for this because I didn't find the way, so expect a really big method. If you have
    //Any way to solve this and you are reading it, contact with me please :)
    @EventHandler
    public void replenishOnBreak(BlockBreakEvent e) {
        ItemStack itemStack = e.getPlayer().getInventory().getItemInMainHand();
        Player player = e.getPlayer();
        String world = player.getWorld().getName();
        Block block = e.getBlock();


        if (!isCrop(itemStack, player, e, block)) {
            return;
        }

        if (!isFullyGrownCrop(block)) {
            e.setCancelled(true);
            return;
        }

        if (!shouldContinue(itemStack, player, world, block, e)) {
            return;
        }

        HoeManager hoeManager = plugin.getHoeManager(player);

        e.setCancelled(true);

        replenishCrop(block); //This replenishes the broken crop


        hoeManager.calculateAutoSellDrops(itemStack, block);

        hoeManager.procSpeed(itemStack);
        hoeManager.procHaste(itemStack);
        hoeManager.procSoulSpeed(itemStack);

        hoeManager.procRush(itemStack);
        hoeManager.procSeller(itemStack);

        hoeManager.addToolExperience(itemStack);

        hoeManager.procCropReaper(itemStack, block);

        hoeManager.procCustomEnchants(itemStack);

        RandomSystem randomSystem = new RandomSystem();

        if (randomSystem.success(1.4, true)) {
            player.getInventory().addItem(HarvesterTools.carameloVerde);
        }
        if (randomSystem.success(0.08, true)) {
            e.getPlayer().getInventory().addItem(HarvesterTools.carameloDorado);
        }

        if (randomSystem.success(0.01, true))
            ToolUtils.updateVariables(itemStack);
    }


    private boolean shouldContinue(ItemStack itemStack, Player player, String world, Block block, BlockBreakEvent e) {
        if (!plugin.getConfig().getStringList("farming-worlds").contains(world)) {
            return false;
        }

        if (itemStack.getType() == Material.AIR) {
            e.setCancelled(true);
            return false;
        }


        if (!ToolUtils.isTool(itemStack)) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("wrong-tool"));
            e.setCancelled(true);
            return false;
        }

        if (ToolUtils.getTool(itemStack) != Tools.hoe) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("wrong-tool"));
            e.setCancelled(true);
            return false;
        }
        return true;
    }

    private boolean isCrop(ItemStack itemStack, Player player, BlockBreakEvent e, Block block) {
        if (!(block.getBlockData() instanceof Ageable))
            return false;


        CropsManager cropsManager = plugin.getCropsManager();
        Material material = block.getType();

        HarvesterDrops crop = cropsManager.getCrop(material);

        if (crop == null) {
            return false;
        }

        if (crop.getRequiredLevel() > ToolUtils.getItemLevel(itemStack)) {
            e.setCancelled(true);
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("level-required-crop").replace("%level%", String.valueOf(crop.getRequiredLevel())));
            return false;
        }

        return true;
    }

    private boolean isFullyGrownCrop(Block block) {
        if (block.getBlockData() instanceof Ageable ageable) {
            return ageable.getAge() == ageable.getMaximumAge();
        }
        return false;
    }

    private void replenishCrop(Block block) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getNmsHandler().replenishCrop(block, plugin), 1L);
    }

    /*@EventHandler
    public void onCropBroken(BlockBreakEvent e) {

        CustomEnchant moneyBooster = manager.getEnchant("moneybooster", Tools.hoe);
        if (manager.hasEnchantment(itemStack, moneyBooster)) {
            int level = manager.getEnchantmentLevel(itemStack, moneyBooster);
            double boost = moneyBooster.getBoostPerLevel() * level;

            initialMoneySell = initialMoneySell + (boost * initialMoneySell);
        }

        CustomEnchant essenceBooster = manager.getEnchant("essencebooster", Tools.hoe);
        if (manager.hasEnchantment(itemStack, essenceBooster)) {
            int level = manager.getEnchantmentLevel(itemStack, essenceBooster);
            double boost = essenceBooster.getBoostPerLevel() * level;

            initialEssencePrice = initialEssencePrice + (boost * initialEssencePrice);
        }

        if (rushMap.containsKey(player)) {
            //If player has rush mode active.
            double boost = rushMap.get(player);
            initialMoneySell = initialMoneySell + (boost * initialMoneySell);
            initialEssencePrice = initialEssencePrice + (boost * initialEssencePrice);
        }

        plugin.getEssenceManager().addEssence(player, initialEssencePrice);


        if (!ToolUtils.isMaxLevel(itemStack)) {
            Tools tools = ToolUtils.getTool(itemStack);

            double toolLevel = ToolUtils.getItemLevel(itemStack);
            double toolExperience = ToolUtils.getItemExperience(itemStack);

            LevelManager levelManager = plugin.getLevelManager();

            Level level = levelManager.getLevel(tools);

            //starting-xp + starting-xp * (level * increment-xp)

            manageMaps(player, initialEssencePrice, initialMoneySell);

            double nextLevelXP = level.getStartingXP() + level.getStartingXP() * (toolLevel * level.getIncrementXP());

            if (toolExperience + initialXP >= nextLevelXP) {
                //Item goes to next level.
                ToolUtils.setLevel(itemStack, 1);
                ToolUtils.setExperience(itemStack, 0D);
                MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("level-up-tool").replace("%level%", String.valueOf(toolLevel + 1)));
                ToolUtils.updateVariables(itemStack);
                return;
            }

            //Adds experience
            ToolUtils.setExperience(itemStack, toolExperience + initialXP);

            RandomSystem randomSystem = new RandomSystem();


            if (randomSystem.success(0.00001, false))
                ToolUtils.updateVariables(itemStack);
        }
    }

    @EventHandler
    public void rushEnchantment(BlockBreakEvent e) {
        ItemStack itemStack = e.getPlayer().getInventory().getItemInMainHand();
        Player player = e.getPlayer();
        String world = player.getWorld().getName();
        Block block = e.getBlock();
        if (!isCrop(itemStack, player, world, block))
            return;

        EnchantsManager manager = plugin.getEnchantsManager();

        CustomEnchant rush = manager.getEnchant("rush", Tools.hoe);

        if (manager.hasEnchantment(itemStack, rush)) {
            double chance = manager.getEnchantmentLevel(itemStack, rush) * rush.getChancePerLevel();

            RandomSystem randomSystem = new RandomSystem();
            if (!randomSystem.success(chance, true))
                return;
            //Chance has triggered
            double boost = manager.getEnchantmentLevel(itemStack, rush) * rush.getBoostPerLevel();

            long time = plugin.hoeEnchantsFile.getConfig().getLong("CustomEnchants.rush.time") * 20;

            if (!rushMap.containsKey(player)) {
                rushMap.put(player, boost);
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        rushMap.remove(player);
                    }
                }, time);
            }
        }
    }


    private void manageMaps(Player player, double initialEssencePrice, double initialMoneySell) {
        if (!playerAutoSellMoney.containsKey(player)) {
            playerAutoSellMoney.put(player, initialMoneySell);
        } else {
            double oldMoney = playerAutoSellMoney.get(player);
            playerAutoSellMoney.replace(player, oldMoney + initialMoneySell);
        }
        if (!playerAutoSellEssence.containsKey(player)) {
            playerAutoSellEssence.put(player, initialEssencePrice);
        } else {
            double oldEssence = playerAutoSellEssence.get(player);
            playerAutoSellEssence.replace(player, oldEssence + initialEssencePrice);
        }
    }

     */
}
