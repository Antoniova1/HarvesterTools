package org.frags.harvestertools.commands.subcommands.mainsubcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.utils.ToolUtils;

public class ToolsCommand extends SubCommand {
    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getSyntax() {
        return "<red>/harvestertools give <white><tool> <player>";
    }

    @Override
    public void performPlayer(Player player, String[] args, HarvesterTools plugin) {
        if (!player.hasPermission("harvestertools.main.give")) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
            return;
        }

        if (args.length != 3) {
            MessageManager.miniMessageSender(player, "<red>Wrong usage! Use " + getSyntax());
            return;
        }

        String tool = args[1];
        Tools tools = Tools.getTool(tool);
        if (tools == null) {
            MessageManager.miniMessageSender(player, "<red>That tool is not available, use 'hoe, pickaxe, rod, or sword");
            return;
        }

        String playerName = args[2];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null || !target.isOnline()) {
            //PLayer must be online
            MessageManager.miniMessageSender(player, "<red>Player must be online!");
            return;
        }

        ToolUtils.giveCorrectItem(tools, target);

        MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("give-item-sent")
                .replace("%player%", target.getName()).replace("%tool%", tools.name()));
        MessageManager.miniMessageSender(target, plugin.messages.getConfig().getString("give-item-received")
                .replace("%player%", player.getName()).replace("%tool%", tools.name()));

    }

    @Override
    public void performConsole(CommandSender sender, String[] args, HarvesterTools plugin) {
        if (args.length != 3) {
            Bukkit.getLogger().warning("Wrong usage! Please use /harvestertools give <tool> <player>");
            return;
        }

        String tool = args[1];
        Tools tools = Tools.getTool(tool);
        if (tools == null) {
            Bukkit.getLogger().warning("That tool is not available, use 'hoe, pickaxe, rod, or sword");
            return;
        }

        String playerName = args[2];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null || !target.isOnline()) {
            //PLayer must be online
            Bukkit.getLogger().warning("Player must be online");
            return;
        }

        ToolUtils.giveCorrectItem(tools, target);

        Bukkit.getLogger().info(tool + " given to " + target.getName());
        MessageManager.miniMessageSender(target, plugin.messages.getConfig().getString("give-item-sent")
                .replace("%player%", "CONSOLE").replace("%tool%", tools.name()));
    }
}
