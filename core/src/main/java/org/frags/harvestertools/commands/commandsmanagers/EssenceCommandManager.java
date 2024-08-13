package org.frags.harvestertools.commands.commandsmanagers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.commands.subcommands.essencesubcommands.*;
import org.frags.harvestertools.managers.MessageManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class EssenceCommandManager implements CommandExecutor {

    private final HarvesterTools plugin;

    private final ArrayList<SubCommand> subCommands = new ArrayList<>();

    public EssenceCommandManager(HarvesterTools plugin) {
        this.plugin = plugin;

        subCommands.add(new BalanceCommand());
        subCommands.add(new GiveCommand());
        subCommands.add(new PayCommand());
        subCommands.add(new WithdrawCommand());
        subCommands.add(new ShopCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (sender instanceof Player player) {

            if (args.length == 0) {
                //List that shows all possible commands
                if (player.hasPermission("harvestertools.essence")) {
                    MessageManager.miniMessageSender(player, "<green>These are the list of the possible commands:");
                    MessageManager.miniMessageSender(player, "<gray>/essence balance (player)");
                    MessageManager.miniMessageSender(player, "<gray>/essence pay <white>(player) (amount)");
                    MessageManager.miniMessageSender(player, "<gray>/essence withdraw <white>(amount)");
                    MessageManager.miniMessageSender(player, "<gray>/essence shop");
                    MessageManager.miniMessageSender(player, "<gray>/essence give <white>(player) (amount)");
                    return true;
                }
                //No permission message
                MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
                return true;
            }

            for (int i = 0; i < getSubCommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                    getSubCommands().get(i).performPlayer(player, args, plugin);
                }
            }
        } else {
            //Sender is console
            if (args.length > 0) {
                for (int i = 0; i < getSubCommands().size(); i++) {
                    if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                        getSubCommands().get(i).performConsole(sender, args, plugin);
                    }
                }
            }
        }


        return true;
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }
}
