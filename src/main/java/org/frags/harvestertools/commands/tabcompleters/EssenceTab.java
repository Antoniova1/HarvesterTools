package org.frags.harvestertools.commands.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EssenceTab implements TabCompleter {

    List<String> arguments = new ArrayList<String>();

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (sender instanceof Player) {
            Player p = (Player) sender;

        }

        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String arg : arguments) {
                if (arg.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(arg);
                }
                return result;
            }
        }

        return null;
    }
}
