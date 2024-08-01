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

public class PayCommand extends SubCommand {
    @Override
    public String getName() {
        return "pay";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getSyntax() {
        return "/essence pay <player> <amount>";
    }

    @Override
    public void performPlayer(Player player, String[] args, HarvesterTools plugin) {
        if (!player.hasPermission("harvestertools.essence.pay")) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
            return;
        }

        if (args.length != 3) {
            MessageManager.miniMessageSender(player, "<red>Wrong usage! Use /essence pay <player> <amount>");
            return;
        }

        //Right usage.

        String input = args[1];

        Player target = Bukkit.getPlayer(input);
        if (target == null || !target.isOnline()) {
            //Player is not online
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("player-not-online"));
            return;
        }

        String stringAmount = args[2];
        double amount = 0D;
        try {
            amount = Double.parseDouble(stringAmount);
        } catch (NumberFormatException e) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-number"));
            return;
        }

        EssenceManager manager = plugin.getEssenceManager();

        if (manager.withdrawEssence(player, amount)) {
            //Player have enough essence
            manager.addEssence(target, amount);
            String formattedNumber = Utils.formatNumber(new BigDecimal(amount));
            MessageManager.miniMessageSender(target, plugin.messages.getConfig().getString("pay-success-receive").replace("%amount%", formattedNumber).
                    replace("%player%", player.getName()));
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("pay-success-send").replace("%amount%", formattedNumber)
                    .replace("%player%", target.getName()));
            return;
        }
        //Player does not have enough essence
        MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-enough-essence"));
    }

    @Override
    public void performConsole(CommandSender sender, String[] args, HarvesterTools plugin) {
        Bukkit.getLogger().info("Use /essence give <player> <amount>");
    }
}
