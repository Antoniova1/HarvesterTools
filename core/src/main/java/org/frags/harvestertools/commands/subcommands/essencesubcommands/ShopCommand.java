package org.frags.harvestertools.commands.subcommands.essencesubcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.menusystem.shopmenus.ShopCore;

public class ShopCommand extends SubCommand {
    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public String getDescription() {
        return "opens shop";
    }

    @Override
    public String getSyntax() {
        return "/essence shop";
    }

    @Override
    public void performPlayer(Player player, String[] args, HarvesterTools plugin) {
        if (!player.hasPermission("harvestertools.essence.shop")) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
            return;
        }

        MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("shop-open"));
        new ShopCore(plugin, HarvesterTools.getPlayerMenuUtilityMap(player)).open();
    }

    @Override
    public void performConsole(CommandSender sender, String[] args, HarvesterTools plugin) {
        Bukkit.getLogger().warning("You can't execute this command!");
    }
}
