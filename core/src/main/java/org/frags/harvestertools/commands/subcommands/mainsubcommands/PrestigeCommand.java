package org.frags.harvestertools.commands.subcommands.mainsubcommands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.utils.ToolUtils;

public class PrestigeCommand extends SubCommand {
    @Override
    public String getName() {
        return "prestige";
    }

    @Override
    public String getDescription() {
        return "Adds prestige";
    }

    @Override
    public String getSyntax() {
        return "/harvestertools prestige <level>";
    }

    @Override
    public void performPlayer(Player player, String[] args, HarvesterTools plugin) {
        if (!player.hasPermission("harvestertools.prestige")) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
            return;
        }

        if (args.length != 2) {
            MessageManager.miniMessageSender(player, "<red>Wrong usage! Use /harvestertools prestige <amount>");
            return;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            MessageManager.miniMessageSender(player, "<red>Please hold a tool.");
            return;
        }
        if (!ToolUtils.isTool(itemStack)) {
            MessageManager.miniMessageSender(player, "<red>Please hold a tool.");
            return;
        }

        String stringNumber = args[1];
        int number;
        try {
            number = Integer.parseInt(stringNumber);
        } catch (NumberFormatException e) {
            MessageManager.miniMessageSender(player, "<red>You must put a number!");
            return;
        }

        ToolUtils.setPrestige(itemStack, number);
        MessageManager.miniMessageSender(player, "<green>You have successfully added " + number + " prestige!");
    }

    @Override
    public void performConsole(CommandSender sender, String[] args, HarvesterTools plugin) {

    }
}
