package org.frags.harvestertools.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enchants.EnchantsManager;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.toolsmanagers.RodManager;
import org.frags.harvestertools.utils.RandomSystem;
import org.frags.harvestertools.utils.ToolUtils;

import java.util.concurrent.ThreadLocalRandom;

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

            CustomEnchant fastRod = plugin.getEnchantsManager().getEnchant("fastrod", Tools.rod);

            if (fastRod != null) {
                RodManager rodManager = plugin.getRodManager(player);

                rodManager.fastRod(fastRod, e, itemStack);
            }
            //Player cast the rod

            FishHook hook = e.getHook();
            hook.setCustomNameVisible(true);

            int randomNumber = ThreadLocalRandom.current().nextInt(0, 80);

            hook.setMaxLureTime(hook.getMinLureTime());

            hook.setMinWaitTime(hook.getMinWaitTime() + randomNumber);
            hook.setMaxWaitTime(hook.getMinWaitTime());

            int totalTimeMs = (hook.getMinWaitTime()+ hook.getMinLureTime()) * 50;

            new BukkitRunnable() {
                int time = totalTimeMs;

                public void run() {

                    if (time <= 0 || hook.isDead()) {
                        hook.setCustomName(ChatColor.translateAlternateColorCodes('&', "&a0.000"));
                        this.cancel();
                        return;
                    }

                    int seconds = time / 1000;
                    int milliseconds = time % 1000;

                    String newTimeDisplay = ChatColor.translateAlternateColorCodes('&', "&a" + seconds + "." + String
                            .format("%03d", milliseconds));
                    hook.setCustomName(newTimeDisplay);

                    time -= 50;

                    if (hook.isDead() || hook.getLocation() == null)
                        this.cancel();
                }
            }.runTaskTimer(plugin, 0L, 1L);

        }

        if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            //Player fished
            if (e.getCaught() == null)
                return;

            ItemStack drop = ((Item) e.getCaught()).getItemStack();
            Entity entity = e.getCaught();
            entity.remove();
            RodManager rodManager = plugin.getRodManager(player);


            rodManager.calculateAutoSellDrops(itemStack, drop);

            rodManager.procTsunami(itemStack);

            rodManager.addToolExperience(itemStack);


            rodManager.procCustomEnchants(itemStack);

            RandomSystem randomSystem = new RandomSystem();

            if (randomSystem.success(16, true)) {
                e.getPlayer().getInventory().addItem(HarvesterTools.carameloAzul);
            }
            if (randomSystem.success(1, true)) {
                e.getPlayer().getInventory().addItem(HarvesterTools.carameloDorado);
            }

            if (randomSystem.success(1, false))
                ToolUtils.updateVariables(itemStack);
        }
    }
}
