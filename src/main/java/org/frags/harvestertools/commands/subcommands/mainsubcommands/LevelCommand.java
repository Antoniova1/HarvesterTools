package org.frags.harvestertools.commands.subcommands.mainsubcommands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.utils.ToolUtils;

public class LevelCommand extends SubCommand {
    @Override
    public String getName() {
        return "level";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getSyntax() {
        return "/harvestertools level <amount>";
    }

    @Override
    public void performPlayer(Player player, String[] args, HarvesterTools plugin) {
        //DON'T OVERRIDE MAX LEVEL
        if (!player.hasPermission("harvestertools.level")) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
            return;
        }

        if (args.length != 2) {
            MessageManager.miniMessageSender(player, "<red>Wrong usage! Use /harvestertools level <amount>");
            return;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;
        if (!ToolUtils.isTool(itemStack)) {
            MessageManager.miniMessageSender(player, "<red>Please hold a tool.");
            return;
        }

        String stringNumber = args[1];
        int number;
        try {
            number = Integer.parseInt(stringNumber);
        } catch (NumberFormatException e) {
            MessageManager.miniMessageSender(player, "<red>You must put a number!");
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        int toolLevel = ToolUtils.getItemLevel(itemStack);
        int maxLevel = plugin.getConfig().getInt("Prestige.max.level");

        int newLevel = number + toolLevel;

        if (newLevel > maxLevel)
            newLevel = maxLevel;

        container.set(ToolUtils.levelKey, PersistentDataType.INTEGER, newLevel);

    }

    @Override
    public void performConsole(CommandSender sender, String[] args, HarvesterTools plugin) {

    }
}
