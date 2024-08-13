package org.frags.harvestertools.commands.subcommands.essencesubcommands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.commands.SubCommand;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.utils.ToolUtils;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;
import java.util.List;

public class WithdrawCommand extends SubCommand {
    @Override
    public String getName() {
        return "withdraw";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getSyntax() {
        return "/essence withdraw <amount>";
    }

    @Override
    public void performPlayer(Player player, String[] args, HarvesterTools plugin) {
        if (!player.hasPermission("harvestertools.essence.withdraw")) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("no-permission"));
            return;
        }

        if (args.length != 2) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("wrong-withdraw-usage"));
            return;
        }
        String input = args[1];
        double amount = 0D;
        try {
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-number"));
            return;
        }

        if (plugin.getEssenceManager().withdrawEssence(player, amount)) {
            //Enough essence
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("Essence-item");
            String value = section.getString("value");
            ItemStack itemStack = null;
            if (value == null || value.isEmpty()) {
                //Then use material
                try {
                    Material material = Material.getMaterial(section.getString("material"));
                    itemStack = new ItemStack(material);
                } catch (NullPointerException e) {
                    Bukkit.getLogger().warning("Material is null!");
                }
            } else {
                //Use head
                itemStack = Utils.getHead(value);
            }

            ItemMeta meta = itemStack.getItemMeta();

            if (section.getBoolean("glow")) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1, true);
            }

            String formattedAmount = Utils.formatNumber(new BigDecimal(amount));

            String name = MessageManager.miniStringParse(section.getString("name"))
                    .replace("%amount%", formattedAmount);

            meta.setDisplayName(name);

            List<String> lore = section.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                String formattedLine = MessageManager.miniStringParse(lore.get(i))
                        .replace("%amount%", formattedAmount);
                lore.remove(i);
                lore.add(i, formattedLine);
            }

            meta.setLore(lore);

            PersistentDataContainer container = meta.getPersistentDataContainer();

            container.set(ToolUtils.essenceItemKey, PersistentDataType.DOUBLE, amount);

            itemStack.setItemMeta(meta);
            player.getInventory().addItem(itemStack);

            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("withdraw-success").replace("%amount%", formattedAmount));
            return;
        }

        //Not enough essence
        MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-enough-essence"));
    }

    @Override
    public void performConsole(CommandSender sender, String[] args, HarvesterTools plugin) {
        Bukkit.getLogger().warning("Only players can execute this command");
    }
}
