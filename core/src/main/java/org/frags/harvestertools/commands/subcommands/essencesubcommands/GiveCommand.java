package org.frags.harvestertools.commands.subcommands.essencesubcommands;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.essence.EssenceManager;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;

public class GiveCommand extends SubCommand {
    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getDescription() {
        return "gives essence to player";
    }

    @Override
    public String getSyntax() {
        return "/essence give <player> <amount>";
    }

    @Override
    public void performPlayer(Player player, String[] args, HarvesterTools plugin) {
        if (!player.hasPermission("harvestertools.essence.give")) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
            return;
        }

        if (args.length != 3) {
            MessageManager.miniMessageSender(player, "<red>Wrong usage! Use /essence give <player> <amount>");
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

        EssenceManager manager = plugin.getEssenceManager();

        manager.addEssence(target, amount);

        String formattedNumber = Utils.formatNumber(new BigDecimal(amount));

        MessageManager.miniMessageSender(player, "<green>You have successfully given " + formattedNumber + " essence to " + target.getName() + "!");

        String message = plugin.messages.getConfig().getString("essence-given-success").replace("%amount%", formattedNumber)
                .replace("%player%", player.getName());

        message = PlaceholderAPI.setPlaceholders(player, message);

        MessageManager.miniMessageSender(target, message);
    }

    @Override
    public void performConsole(CommandSender sender, String[] args, HarvesterTools plugin) {
        if (args.length != 3) {
            Bukkit.getLogger().warning("Wrong usage! Use /essence give <player> <amount>");
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

        EssenceManager manager = plugin.getEssenceManager();

        manager.addEssence(target, amount);

        String formattedNumber = Utils.formatNumber(new BigDecimal(amount));

        String message = plugin.messages.getConfig().getString("essence-given-success").replace("%amount%", formattedNumber).
                replace("%player%", "Console");

        MessageManager.miniMessageSender(target, message);
        Bukkit.getLogger().info("You have given " + formattedNumber + " essence to " + target.getName() + "!");
    }
}
