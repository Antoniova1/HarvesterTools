package org.frags.harvestertools.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.frags.harvestertools.HarvesterTools;

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
    }

    @EventHandler
    public void createPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        plugin.getEssenceManager().createPlayer(player);
        plugin.getEssenceManager().loadPlayer(player.getUniqueId());
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getEssenceManager().reloadPlayerEssence(player);
            }
        }, 20L, 144000L); //2 Hours
    }
}
