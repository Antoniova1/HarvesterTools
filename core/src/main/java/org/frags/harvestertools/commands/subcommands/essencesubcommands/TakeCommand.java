package org.frags.harvestertools.commands.subcommands.essencesubcommands;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.essence.EssenceManager;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;

public class TakeCommand extends SubCommand {

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getSyntax() {
        return "/essence take <player> <amount> [-s]";
    }

    @Override
    public void performPlayer(Player player, String[] args, HarvesterTools plugin) {
        if (!player.hasPermission("harvestertools.essence.take")) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
            return;
        }

        if (args.length < 3) {
            MessageManager.miniMessageSender(player, "<red>Wrong usage! Use /essence take <player> <amount> [-s]");
            return;
        }

        //Right usage.

        String input = args[1];

        Player target = Bukkit.getPlayer(input);
        if (target == null || !target.isOnline()) {
            //Player is not online
            MessageManager.miniMessageSender(player, "<red>Player must be online!");
            return;
        }

        String stringAmount = args[2];
        double amount = 0;
        try {
            amount = Double.parseDouble(stringAmount);
        } catch (NumberFormatException e) {
            MessageManager.miniMessageSender(player, "<red>You must specify a number!");
            return;
        }

        if (amount <= 0) {
            MessageManager.miniMessageSender(player, "<red>You must give a number higher than 0!");
            return;
        }

        EssenceManager manager = plugin.getEssenceManager();

        manager.removeEssence(target, amount);

        String formattedNumber = Utils.formatNumber(new BigDecimal(amount));

        if (args.length == 4 && args[3].equalsIgnoreCase("-s")) {
            return;
        }
        MessageManager.miniMessageSender(player, "<green>You have successfully taken " + formattedNumber + " essence to " + target.getName() + "!");

        String message = plugin.messages.getConfig().getString("essence-taken-success").replace("%amount%", formattedNumber)
                .replace("%player%", player.getName());

        message = PlaceholderAPI.setPlaceholders(player, message);

        MessageManager.miniMessageSender(target, message);
    }

    @Override
    public void performConsole(CommandSender sender, String[] args, HarvesterTools plugin) {
        if (args.length < 3) {
            Bukkit.getLogger().warning("Wrong usage! Use /essence take <player> <amount>");
            return;
        }
        //Right usage.

        String input = args[1];

        Player target = Bukkit.getPlayer(input);
        if (target == null || !target.isOnline()) {
            //Player is not online
            Bukkit.getLogger().warning("Player must be online!");
            return;
        }

        String stringAmount = args[2];
        double amount = 0;
        try {
            amount = Double.parseDouble(stringAmount);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("You must specify a number!");
            return;
        }

        if (amount <= 0) {
            Bukkit.getLogger().info("<red>You must give a number higher than 0!");
            return;
        }

        EssenceManager manager = plugin.getEssenceManager();

        manager.removeEssence(target, amount);

        String formattedNumber = Utils.formatNumber(new BigDecimal(amount));

        if (args.length == 4 && args[3].equalsIgnoreCase("-s")) {
            return;
        }

        String message = plugin.messages.getConfig().getString("essence-given-success").replace("%amount%", formattedNumber).
                replace("%player%", "Console");

        MessageManager.miniMessageSender(target, message);
        Bukkit.getLogger().info("You have taken " + formattedNumber + " essence to " + target.getName() + "!");
    }
}

