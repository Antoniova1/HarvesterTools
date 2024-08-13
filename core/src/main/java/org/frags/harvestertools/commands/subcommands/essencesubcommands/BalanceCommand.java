package org.frags.harvestertools.commands.subcommands.essencesubcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.managers.MessageManager;

public class BalanceCommand extends SubCommand {
    @Override
    public String getName() {
        return "balance";
    }

    @Override
    public String getDescription() {
        return "shows the balance of a player";
    }

    @Override
    public String getSyntax() {
        return "/essence balance <player>";
    }

    @Override
    public void performPlayer(Player player, String[] args, HarvesterTools plugin) {
        if (!player.hasPermission("harvestertools.essence.balance")) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
            return;
        }

        if (args.length == 1) {
            //Check players own balance.
            String balance = plugin.getEssenceManager().getFormattedEssence(player);
            String message = plugin.messages.getConfig().getString("balance-command").replace("%player%", player.getName())
                    .replace("%essence%", balance);
            MessageManager.miniMessageSender(player, message);
        } else if (args.length == 2) {
            String input = args[1];
            OfflinePlayer target = Bukkit.getOfflinePlayer(input);
            if (!target.hasPlayedBefore()) {
                //Player have never logged in.
                MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("player-not-exist").replace("%player%", input));
                return;
            }
            String balance = plugin.getEssenceManager().getFormattedEssence(target);
            String message = plugin.messages.getConfig().getString("balance-command");
            MessageManager.miniMessageSender(player, message.replace("%player%", target.getName()).replace("%essence%", balance));
        }
    }

    @Override
    public void performConsole(CommandSender sender, String[] args, HarvesterTools plugin) {
        if (args.length != 2) {
            Bukkit.getLogger().warning("Usage: /essence balance <player>");
            return;
        }
        String input = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(input);
        if (!target.hasPlayedBefore()) {
            //Player have never logged in.
            Bukkit.getLogger().warning("Player not exist");
            return;
        }
        String balance = plugin.getEssenceManager().getFormattedEssence(target);
        Bukkit.getLogger().info(target.getName() + "balance is " + balance);
    }
}
