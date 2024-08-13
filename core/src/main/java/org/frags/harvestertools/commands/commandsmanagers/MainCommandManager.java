package org.frags.harvestertools.commands.commandsmanagers;

import com.sun.tools.javac.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.HelpCommand;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.commands.subcommands.mainsubcommands.LevelCommand;
import org.frags.harvestertools.commands.subcommands.mainsubcommands.PrestigeCommand;
import org.frags.harvestertools.commands.subcommands.mainsubcommands.ReloadCommand;
import org.frags.harvestertools.commands.subcommands.mainsubcommands.ToolsCommand;
import org.frags.harvestertools.managers.MessageManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainCommandManager implements CommandExecutor {

    private ArrayList<SubCommand> subCommands = new ArrayList<>();

    private final HarvesterTools plugin;

    public MainCommandManager(HarvesterTools plugin) {
        this.plugin = plugin;
        //register subcommands
        subCommands.add(new LevelCommand());
        subCommands.add(new ToolsCommand());
        subCommands.add(new PrestigeCommand());
        subCommands.add(new ReloadCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        //harvestertools /tools /ht /harvester /tool
        if (sender instanceof Player player) {

            if (args.length == 0) {
                //List that shows all possible commands
                if (player.hasPermission("harvestertools.main")) {
                    MessageManager.miniMessageSender(player, "<green>These are the list of the possible commands:");
                    MessageManager.miniMessageSender(player, "<gray>/harvestertools give <white>(tool) (player)");
                    MessageManager.miniMessageSender(player, "<gray>/harvestertools level <white>(amount)");
                    MessageManager.miniMessageSender(player, "<gray>/harvestertools prestige <white>(amount)");
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
