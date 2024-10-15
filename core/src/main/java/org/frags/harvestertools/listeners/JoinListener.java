package org.frags.harvestertools.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.toolsmanagers.HoeManager;
import org.frags.harvestertools.toolsmanagers.PickaxeManager;
import org.frags.harvestertools.toolsmanagers.RodManager;
import org.frags.harvestertools.toolsmanagers.SwordManager;

import static org.frags.harvestertools.utils.ToolUtils.*;

public class JoinListener implements Listener {

    private final HarvesterTools plugin;

    public JoinListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!plugin.getConfig().getBoolean("tools.give-on-join"))
            return;
        if (!player.hasPlayedBefore()) {
            player.getInventory().addItem(getHoe(player), getSword(player), getRod(player), getPickaxe(player));
        }

        player.setWalkSpeed(0.2F);
        plugin.hoeManagerMap.put(player.getName(), new HoeManager(plugin, player.getName()));
        plugin.swordManagerMap.put(player.getName(), new SwordManager(plugin, player.getName()));
        plugin.rodManagerMap.put(player.getName(), new RodManager(plugin, player.getName()));
        plugin.pickaxeManagerMap.put(player.getName(), new PickaxeManager(plugin, player.getName()));
    }

    @EventHandler
    public void createPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        plugin.getEssenceManager().createPlayer(player);
        plugin.getEssenceManager().loadPlayer(player.getUniqueId());
        Bukkit.getScheduler().runTaskTimer(plugin, () -> plugin.getEssenceManager().reloadPlayerEssence(player), 20L, 144000L); //2 Hours
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        plugin.hoeManagerMap.remove(player.getName());
        plugin.swordManagerMap.remove(player.getName());
        plugin.rodManagerMap.remove(player.getName());
        plugin.pickaxeManagerMap.remove(player.getName());
    }
}
