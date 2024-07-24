package org.frags.harvestertools.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static org.frags.harvestertools.HarvesterTools.getInstance;

public class ToolUtils {

    public static NamespacedKey ownerKey = new NamespacedKey(getInstance(), "owner");
    public static NamespacedKey levelKey = new NamespacedKey(getInstance(), "level");
    public static NamespacedKey prestigeKey = new NamespacedKey(getInstance(), "prestige");
    public static NamespacedKey essenceKey = new NamespacedKey(getInstance(), "essence");
    public static NamespacedKey autosellKey = new NamespacedKey(getInstance(), "autosell_mode");
    public static NamespacedKey essenceBoostKey = new NamespacedKey(getInstance(), "essence_boost");
    public static NamespacedKey moneyBoostKey = new NamespacedKey(getInstance(), "money_boost");
    public static NamespacedKey enchantBoostKey = new NamespacedKey(getInstance(), "enchant_boost");

    private static ConfigurationSection section = getInstance().getConfig().getConfigurationSection("tools");

    public static ItemStack getHoe(Player player) {
        try {
            ConfigurationSection hoe = section.getConfigurationSection("hoe");
            ItemStack itemStack = new ItemStack(Material.getMaterial(hoe.getString("material")));

            ItemMeta meta = itemStack.getItemMeta();

            meta.setDisplayName(replaceVariables(hoe.getString("name"), player));

            List<String> lore = hoe.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                String replaced = replaceVariables(lore.get(i), player);
                lore.remove(i);
                lore.add(i, replaced);
            }
            meta.setLore(lore);

            PersistentDataContainer container = meta.getPersistentDataContainer();

            addPDC(container, player);

            //Used to know what's the used tool | Use Tools.getTool() to deserialize it.
            container.set(new NamespacedKey(getInstance(), "tool"), PersistentDataType.STRING, "hoe");

            meta.setUnbreakable(true);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE
            , ItemFlag.HIDE_PLACED_ON);

            itemStack.setItemMeta(meta);
            return itemStack;
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Error while trying to create the Hoe!");
            Bukkit.getLogger().warning("Please, check the wiki to be sure you are doing it right.");
        }
        return null;
    }

    public static ItemStack getSword(Player player) {
        try {
            ConfigurationSection sword = section.getConfigurationSection("sword");
            ItemStack itemStack = new ItemStack(Material.getMaterial(sword.getString("material")));

            ItemMeta meta = itemStack.getItemMeta();

            meta.setDisplayName(replaceVariables(sword.getString("name"), player));

            List<String> lore = sword.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                String replaced = replaceVariables(lore.get(i), player);
                lore.remove(i);
                lore.add(i, replaced);
            }
            meta.setLore(lore);

            PersistentDataContainer container = meta.getPersistentDataContainer();

            addPDC(container, player);

            //Used to know what's the used tool | Use Tools.getTool() to deserialize it.
            container.set(new NamespacedKey(getInstance(), "tool"), PersistentDataType.STRING, "sword");

            meta.setUnbreakable(true);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

            itemStack.setItemMeta(meta);
            return itemStack;
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Error while trying to create the Sword!");
            Bukkit.getLogger().warning("Please, check the wiki to be sure you are doing it right.");
        }
        return null;
    }

    public static ItemStack getRod(Player player) {
        try {
            ConfigurationSection rod = section.getConfigurationSection("rod");
            ItemStack itemStack = new ItemStack(Material.FISHING_ROD);

            ItemMeta meta = itemStack.getItemMeta();

            meta.setDisplayName(replaceVariables(rod.getString("name"), player));

            List<String> lore = rod.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                String replaced = replaceVariables(lore.get(i), player);
                lore.remove(i);
                lore.add(i, replaced);
            }
            meta.setLore(lore);

            PersistentDataContainer container = meta.getPersistentDataContainer();

            addPDC(container, player);

            //Used to know what's the used tool | Use Tools.getTool() to deserialize it.
            container.set(new NamespacedKey(getInstance(), "tool"), PersistentDataType.STRING, "rod");

            meta.setUnbreakable(true);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

            itemStack.setItemMeta(meta);
            return itemStack;
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Error while trying to create the Rod!");
            Bukkit.getLogger().warning("Please, check the wiki to be sure you are doing it right.");
        }
        return null;
    }

    public static ItemStack getPickaxe(Player player) {
        try {
            ConfigurationSection pickaxe = section.getConfigurationSection("pickaxe");
            ItemStack itemStack = new ItemStack(Material.getMaterial(pickaxe.getString("material")));

            ItemMeta meta = itemStack.getItemMeta();

            meta.setDisplayName(replaceVariables(pickaxe.getString("name"), player));

            List<String> lore = pickaxe.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                String replaced = replaceVariables(lore.get(i), player);
                lore.remove(i);
                lore.add(i, replaced);
            }
            meta.setLore(lore);

            PersistentDataContainer container = meta.getPersistentDataContainer();

            addPDC(container, player);

            //Used to know what's the used tool | Use Tools.getTool() to deserialize it.
            container.set(new NamespacedKey(getInstance(), "tool"), PersistentDataType.STRING, "pickaxe");

            meta.setUnbreakable(true);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);


            itemStack.setItemMeta(meta);
            return itemStack;
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Error while trying to create the Pickaxe!");
            Bukkit.getLogger().warning("Please, check the wiki to be sure you are doing it right.");
        }
        return null;
    }

    private static String replaceVariables(String string, Player player) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        TextComponent text = (TextComponent) miniMessage.deserialize(string);
        //Replace in the return
        return LegacyComponentSerializer.legacySection().serialize(text).replace("%owner%", player.getName()).replace("%level%", "0").
                replace("%prestige%", "0").replace("%sell-mode%", "Collect").replace("%essence%", "0")
                .replace("%enchantlist%", "");
    }

    private static void addPDC(PersistentDataContainer container, Player player) {
        // Sets the owner
        container.set(ownerKey, PersistentDataType.STRING, player.getName());

        // Sets the level
        container.set(levelKey, PersistentDataType.INTEGER, 0);

        // Sets the prestige
        container.set(prestigeKey, PersistentDataType.INTEGER, 0);

        // Sets the essence
        container.set(essenceKey, PersistentDataType.DOUBLE, 0D);

        // Sets the mode
        container.set(autosellKey, PersistentDataType.STRING, "Collect");

        // Sets the essence boost
        container.set(essenceBoostKey, PersistentDataType.DOUBLE, 0D);

        // Sets the money boost
        container.set(moneyBoostKey, PersistentDataType.DOUBLE, 0D);

        // Sets the enchant boost
        container.set(enchantBoostKey, PersistentDataType.DOUBLE, 0D);
    }

    public static int getItemLevel(ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();;

        return container.getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
    }

    public static int getItemPrestige(ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();;

        return container.getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
    }
}
