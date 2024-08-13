package org.frags.harvestertools.menusystem.shopmenus;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.menusystem.Menu;
import org.frags.harvestertools.menusystem.PlayerMenuUtility;
import org.frags.harvestertools.utils.Utils;

import javax.naming.Name;
import java.util.HashMap;
import java.util.List;

public class ShopMenu extends Menu {

    private final ConfigurationSection section;

    private final NamespacedKey priceKey;

    private final HashMap<Integer, List<String>> itemCommands = new HashMap<>();

    private final NamespacedKey nameKey;

    public ShopMenu(HarvesterTools plugin, PlayerMenuUtility playerMenuUtility, String key) {
        super(plugin, playerMenuUtility);
        this.section = plugin.shopFile.getConfig().getConfigurationSection(key);
        this.priceKey = new NamespacedKey(plugin, "price");
        this.nameKey = new NamespacedKey(plugin, "name");
    }

    @Override
    public String getMenuName() {
        return miniMessageParser(section.getString("title"));
    }

    @Override
    public int getSlots() {
        return section.getInt("size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack itemStack = e.getCurrentItem();
        int slot = e.getSlot();

        if (itemCommands.containsKey(slot)) {
            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
            double price = container.get(priceKey, PersistentDataType.DOUBLE);
            if (!plugin.getEssenceManager().withdrawEssence(player, price)) {
                //Not enough essence
                player.closeInventory();
                MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-enough-essence"));
                return;
            }

            List<String> commands = itemCommands.get(slot);
            for (String command : commands) {
                String commandLine = command;
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    commandLine = PlaceholderAPI.setPlaceholders(player, commandLine);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandLine.replace("%player%", player.getName()));
            }
            String name = container.get(nameKey, PersistentDataType.STRING);
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("successful-buy").replace("%item%", name));
        }
    }

    @Override
    public void setMenuItems() {
        ConfigurationSection itemSection = section.getConfigurationSection("items");
        for (String key : itemSection.getKeys(false)) {
            ConfigurationSection itemConfig = itemSection.getConfigurationSection(key);
            int slot;
            try {
                slot = Integer.parseInt(key);
            } catch (NumberFormatException ex) {
                Bukkit.getLogger().warning("Couldn't parse " + key + " as an integer. Please, keys in items section can only be numbers.");
                continue;
            }
            String itemName = itemConfig.getString("material");
            ItemStack itemStack;
            try {
                itemStack = new ItemStack(Material.valueOf(itemName));
            } catch (NullPointerException e) {
                Bukkit.getLogger().warning("Material at " + key + " is not a valid material.");
                continue;
            }

            if (itemStack.getType() == Material.PLAYER_HEAD) {
                String value = itemConfig.getString("value");
                if (value == null || value.isEmpty()) {
                    //Don't do anything
                } else {
                    itemStack = Utils.getHead(value);
                }
            }

            String name = miniMessageParser(itemConfig.getString("name"));

            List<String> expectedLore = itemConfig.getStringList("lore");

            for (int i = 0; i < expectedLore.size(); i++) {
                String replaced = miniMessageParser(expectedLore.get(i));

                expectedLore.set(i, replaced);
            }

            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(expectedLore);

            if (itemConfig.getBoolean("glow")) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            }

            double price = itemConfig.getDouble("price");
            PersistentDataContainer container = meta.getPersistentDataContainer();

            container.set(priceKey, PersistentDataType.DOUBLE, price);

            container.set(nameKey, PersistentDataType.STRING, itemConfig.getString("name"));

            itemStack.setItemMeta(meta);

            itemCommands.put(slot, itemConfig.getStringList("commands"));

            inventory.setItem(slot, itemStack);
        }

        setFillerGlass(section.getString("filler"));
    }
}
