package org.frags.harvestertools.commands.tabcompleters;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommandTab implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof Player player))
            return null;

        List<String> arguments = new ArrayList<>();

        if (player.hasPermission("harvestertools.level"))
            arguments.add("level");
        if (player.hasPermission("harvestertools.prestige"))
            arguments.add("prestige");
        if (player.hasPermission("harvestertools.main.give"))
            arguments.add("give");

        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String arg : arguments) {
                if (arg.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(arg);
                }
            }
            return result;
        } else if (args.length == 2) {
            result.clear();
            if (args[0].equalsIgnoreCase("level")) {
                result.add("<amount>");
            } else if (args[0].equalsIgnoreCase("prestige")) {
                result.add("<amount>");
            } else if (args[0].equalsIgnoreCase("give")) {
                result.add("hoe"); result.add("pickaxe");
                result.add("rod"); result.add("sword");
            }
            return result;
        } else if (args.length == 3) {
            if (!args[0].equalsIgnoreCase("give"))
                return null;
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.startsWith(args[2]))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
