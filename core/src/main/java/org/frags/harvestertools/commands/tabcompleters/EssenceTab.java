package org.frags.harvestertools.commands.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EssenceTab implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (!(sender instanceof Player player))
            return null;

        List<String> arguments = new ArrayList<>();

        if (player.hasPermission("harvestertools.essence.withdraw"))
            arguments.add("withdraw");
        if (player.hasPermission("harvestertools.essence.shop"))
            arguments.add("shop");
        if (player.hasPermission("harvestertools.essence.pay"))
            arguments.add("pay");
        if (player.hasPermission("harvestertools.essence.give"))
            arguments.add("give");
        if (player.hasPermission("harvestertools.essence.balance"))
            arguments.add("balance");

        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String arg : arguments) {
                if (arg.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(arg);
                }
            }
            return result;
        }

        return null;
    }
}
