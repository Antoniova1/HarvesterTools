package org.frags.harvestertools.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;

public abstract class SubCommand {

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getSyntax();

    public abstract void performPlayer(Player player, String[] args, HarvesterTools plugin);

    public abstract void performConsole(CommandSender sender, String[] args, HarvesterTools plugin);

}
