package org.frags.harvestertools.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.managers.CommandCooldown;
import org.frags.harvestertools.utils.Utils;

import java.time.Duration;
import java.util.HashMap;

import static org.frags.harvestertools.managers.MessageManager.miniMessageParse;
import static org.frags.harvestertools.utils.ToolUtils.*;


public class ToolsCommand implements CommandExecutor {

    private final HarvesterTools plugin;

    private HashMap<Player, CommandCooldown> hoeCooldown = new HashMap<>();

    private HashMap<Player, CommandCooldown> swordCooldown = new HashMap<>();

    private HashMap<Player, CommandCooldown> rodCooldown = new HashMap<>();

    private HashMap<Player, CommandCooldown> pickaxeCooldown = new HashMap<>();

    public ToolsCommand(HarvesterTools plugin) {
        this.plugin = plugin;

    }



    //This command will give players their tools
    //Can be disabled
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        if (!(sender instanceof Player player))
            return true;
        if (command.getName().equalsIgnoreCase("hoe")) {
            Duration timeLeft;
            //checks if player is in map (avoiding null)
            if (hoeCooldown.containsKey(player)) {
                timeLeft = hoeCooldown.get(player).getRemainingCooldown();
            } else {
                timeLeft = Duration.ZERO;
            }
            if (timeLeft.isZero() || timeLeft.isNegative()) {
                ItemStack hoe = getHoe(player);

                if (hoe == null)
                    return true;
                //adds item
                player.getInventory().addItem(hoe);
                player.sendMessage(miniMessageParse(player, "given-item").replace("%item%", hoe.getItemMeta().getDisplayName()));

                //adds cooldown
                long toolCooldown = plugin.getConfig().getLong("tools.cooldown");
                CommandCooldown cooldown = new CommandCooldown(player, Duration.ofSeconds(toolCooldown));
                hoeCooldown.put(player, cooldown);
            } else {
                //if player is in cooldown
                player.sendMessage(miniMessageParse(player,"cooldown-message").replace("%time%", String.valueOf(timeLeft.toSeconds())));
            }
        } else if (command.getName().equalsIgnoreCase("sword")) {
            Duration timeLeft;
            //checks if player is in map (avoiding null)
            if (swordCooldown.containsKey(player)) {
                timeLeft = swordCooldown.get(player).getRemainingCooldown();
            } else {
                timeLeft = Duration.ZERO;
            }
            if (timeLeft.isZero() || timeLeft.isNegative()) {
                ItemStack sword = getSword(player);

                if (sword == null)
                    return true;
                //adds item
                player.getInventory().addItem(sword);
                player.sendMessage(miniMessageParse(player,"given-item").replace("%item%", sword.getItemMeta().getDisplayName()));

                //adds cooldown
                long toolCooldown = plugin.getConfig().getLong("tools.cooldown");
                CommandCooldown cooldown = new CommandCooldown(player, Duration.ofSeconds(toolCooldown));
                swordCooldown.put(player, cooldown);
            } else {
                //if player is in cooldown
                player.sendMessage(miniMessageParse(player, "cooldown-message").replace("%time%", String.valueOf(timeLeft.toSeconds())));
            }
        } else if (command.getName().equalsIgnoreCase("rod")) {
            Duration timeLeft;
            //checks if player is in map (avoiding null)
            if (rodCooldown.containsKey(player)) {
                timeLeft = rodCooldown.get(player).getRemainingCooldown();
            } else {
                timeLeft = Duration.ZERO;
            }
            if (timeLeft.isZero() || timeLeft.isNegative()) {
                ItemStack rod = getRod(player);

                if (rod == null)
                    return true;
                //adds item
                player.getInventory().addItem(rod);
                player.sendMessage(miniMessageParse(player,"given-item").replace("%item%", rod.getItemMeta().getDisplayName()));

                //adds cooldown
                long toolCooldown = plugin.getConfig().getLong("tools.cooldown");
                CommandCooldown cooldown = new CommandCooldown(player, Duration.ofSeconds(toolCooldown));
                rodCooldown.put(player, cooldown);
            } else {
                //if player is in cooldown
                player.sendMessage(miniMessageParse(player,"cooldown-message").replace("%time%", String.valueOf(timeLeft.toSeconds())));
            }
        } else if (command.getName().equalsIgnoreCase("pickaxe")) {
            Duration timeLeft;
            //checks if player is in map (avoiding null)
            if (pickaxeCooldown.containsKey(player)) {
                timeLeft = pickaxeCooldown.get(player).getRemainingCooldown();
            } else {
                timeLeft = Duration.ZERO;
            }
            if (timeLeft.isZero() || timeLeft.isNegative()) {
                ItemStack pickaxe = getPickaxe(player);

                if (pickaxe == null)
                    return true;
                //adds item
                player.getInventory().addItem(pickaxe);
                player.sendMessage(miniMessageParse(player, "given-item").replace("%item%", pickaxe.getItemMeta().getDisplayName()));

                //adds cooldown
                long toolCooldown = plugin.getConfig().getLong("tools.cooldown");
                CommandCooldown cooldown = new CommandCooldown(player, Duration.ofSeconds(toolCooldown));
                pickaxeCooldown.put(player, cooldown);
            } else {
                //if player is in cooldown
                player.sendMessage(miniMessageParse(player,"cooldown-message").replace("%time%", String.valueOf(timeLeft.toSeconds())));
            }
        }
        return true;
    }


}
