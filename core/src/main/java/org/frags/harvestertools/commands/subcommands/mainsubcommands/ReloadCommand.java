package org.frags.harvestertools.commands.subcommands.mainsubcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.managers.MessageManager;

public class ReloadCommand extends SubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads config.";
    }

    @Override
    public String getSyntax() {
        return "/harvestertools reload";
    }

    @Override
    public void performPlayer(Player player, String[] args, HarvesterTools plugin) {
        if (!player.hasPermission("harvestertools.reload")) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
            return;
        }

        long start = System.currentTimeMillis();

        plugin.reloadConfig();
        plugin.pickaxeEnchantsFile.reloadConfig();
        plugin.hoeEnchantsFile.reloadConfig();
        plugin.rodEnchantsFile.reloadConfig();
        plugin.swordEnchantsFile.reloadConfig();
        plugin.blocksFile.reloadConfig();
        plugin.cropsFile.reloadConfig();
        plugin.mobsFile.reloadConfig();
        plugin.shopFile.reloadConfig();
        plugin.menuFile.reloadConfig();
        plugin.messages.reloadConfig();
        plugin.eventsFile.reloadConfig();
        plugin.reloadObjects();

        long finish = System.currentTimeMillis();

        int total = (int) (finish - start);

        MessageManager.miniMessageSender(player, "<green>Config has been reloaded in " + total + "<green>ms!");
    }

    @Override
    public void performConsole(CommandSender sender, String[] args, HarvesterTools plugin) {
        long start = System.currentTimeMillis();

        plugin.reloadConfig();
        plugin.pickaxeEnchantsFile.reloadConfig();
        plugin.hoeEnchantsFile.reloadConfig();
        plugin.rodEnchantsFile.reloadConfig();
        plugin.swordEnchantsFile.reloadConfig();
        plugin.blocksFile.reloadConfig();
        plugin.cropsFile.reloadConfig();
        plugin.mobsFile.reloadConfig();
        plugin.shopFile.reloadConfig();
        plugin.menuFile.reloadConfig();
        plugin.messages.reloadConfig();
        plugin.eventsFile.reloadConfig();
        plugin.reloadObjects();

        long finish = System.currentTimeMillis();

        int total = (int) (finish - start);

        Bukkit.getLogger().info("Config has been reloaded in " + total + "ms!");
    }
}
