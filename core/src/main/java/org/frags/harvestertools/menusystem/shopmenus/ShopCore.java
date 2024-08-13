package org.frags.harvestertools.menusystem.shopmenus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.menusystem.Menu;
import org.frags.harvestertools.menusystem.PlayerMenuUtility;
import org.frags.harvestertools.utils.Utils;

import java.util.List;

public class ShopCore extends Menu {

    private final ConfigurationSection section;

    private final NamespacedKey menuKey;

    public ShopCore(HarvesterTools plugin, PlayerMenuUtility playerMenuUtility) {
        super(plugin, playerMenuUtility);
        this.section = plugin.shopFile.getConfig().getConfigurationSection("main_menu");
        this.menuKey = new NamespacedKey(plugin, "menu");
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
        ItemStack itemStack = e.getCurrentItem();
        if (!itemStack.hasItemMeta()) return;

        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        if (!container.has(menuKey, PersistentDataType.STRING)) return;

        String key = container.get(menuKey, PersistentDataType.STRING);

        new ShopMenu(plugin, playerMenuUtility, key).open();
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

            String formattedAmount = plugin.getEssenceManager().getFormattedEssence(playerMenuUtility.getOwner());

            String name = miniMessageParser(itemConfig.getString("name")).replace("%essence%", formattedAmount);

            List<String> expectedLore = itemConfig.getStringList("lore");

            for (int i = 0; i < expectedLore.size(); i++) {
                String replaced = miniMessageParser(expectedLore.get(i).replace("%essence%", formattedAmount));

                expectedLore.set(i, replaced);
            }

            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(expectedLore);

            if (itemConfig.getBoolean("glow")) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            }

            String newMenu = itemConfig.getString("menu");

            if (newMenu == null || newMenu.isEmpty()) {
                //Don't do anything
            } else {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(menuKey, PersistentDataType.STRING, newMenu);
            }

            itemStack.setItemMeta(meta);
            inventory.setItem(slot, itemStack);
        }
        setFillerGlass(section.getString("filler"));
    }
}
