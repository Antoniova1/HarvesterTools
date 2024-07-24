package org.frags.harvestertools.menusystem.menu;

import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Sign;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enchants.Result;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.menusystem.Menu;
import org.frags.harvestertools.menusystem.PlayerMenuUtility;
import org.frags.harvestertools.utils.Utils;

import java.util.Collections;
import java.util.List;

public class ConfirmEnchantMenu extends Menu {

    private final ConfigurationSection section;

    private final int currentLevel;

    private final Result result;

    public ConfirmEnchantMenu(HarvesterTools plugin, PlayerMenuUtility playerMenuUtility) {
        super(plugin, playerMenuUtility);
        section = plugin.getConfig().getConfigurationSection("Confirm-Enchant-Menu");
        currentLevel = plugin.getEnchantsManager().getEnchantmentLevel(playerMenuUtility.getItem(), playerMenuUtility.getEnchant());
        result = plugin.getEnchantsManager().calculateMaxUpgrades(playerMenuUtility.getOwner(), playerMenuUtility.getEnchant(), playerMenuUtility.getItem());
    }

    private final NamespacedKey itemKey = new NamespacedKey(plugin, "item");

    @Override
    public String getMenuName() {
        return miniMessageParser(section.getString("title")).replace("%enchant%", playerMenuUtility.getEnchant().getName());
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();

        if (!clickedItem.hasItemMeta()) return;

        PersistentDataContainer container = clickedItem.getItemMeta().getPersistentDataContainer();

        String key = container.get(itemKey, PersistentDataType.STRING);

        if (key.equalsIgnoreCase("max-item")) {
            //This is the max Item :)
            ItemStack playerTool = playerMenuUtility.getItem();
            String enchantName = playerMenuUtility.getEnchant().getName();
            int upgrades = result.getUpgrades();

            e.getWhoClicked().closeInventory();
            plugin.getEnchantsManager().enchantItem(enchantName, playerTool, upgrades, playerMenuUtility.getOwner());
        } else if (key.equalsIgnoreCase("select-item")) {
            String line2 = "^^^^^^^^";
            String line3 = "numbers of levels";
            String line4 = "to upgrade";
            SignGUI gui = SignGUI.builder()
                    .setLines(null, line2, line3, line4)
                    .setType(Material.OAK_SIGN)
                    .setHandler((player, signGUIResult) -> {
                        String line0 = signGUIResult.getLine(0);

                        if (line0.isEmpty()) {
                            return Collections.emptyList();
                        }

                        try {
                            int number = Integer.parseInt(line0);
                            if (number == 0) {
                                return List.of(SignGUIAction.displayNewLines(null, line2, line3, line4));
                            }
                            ItemStack playerTool = playerMenuUtility.getItem();
                            int currentLevel = plugin.getEnchantsManager().getEnchantmentLevel(playerTool, playerMenuUtility.getEnchant());
                            int maxEnchant = playerMenuUtility.getEnchant().getMaxLevel();
                            if (number + currentLevel > maxEnchant) {
                                //If number is higher than it should be
                                MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("higher-than-max")
                                        .replace("%maxLevel%", String.valueOf(maxEnchant)));
                                return List.of(SignGUIAction.displayNewLines(null, line2, line3, line4));
                            }
                            String enchantName = playerMenuUtility.getEnchant().getName();
                            plugin.getEnchantsManager().enchantItem(enchantName, playerTool, number, player);
                        } catch (NumberFormatException ex) {
                            //The input wasn't a number
                            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-number"));
                            return List.of(SignGUIAction.displayNewLines(null, line2, line3, line4));
                        }

                        return Collections.emptyList();
                    })
                    .build();

            gui.open(playerMenuUtility.getOwner());
        }

    }

    @Override
    public void setMenuItems() {
        if (section.getBoolean("item-preview")) {
            inventory.setItem(section.getInt("item-preview-slot"), playerMenuUtility.getItem());
        }
        ConfigurationSection itemSection = section.getConfigurationSection("items");
        for (String key : itemSection.getKeys(false)) {
            ConfigurationSection createSection = itemSection.getConfigurationSection(key);
            String maxValue = null;
            try {
                maxValue = createSection.getString("value");
            } catch (NullPointerException npe) {
                Bukkit.getLogger().warning("Value of Confirm-Enchant-Menu is incorrect.");
            }

            try {
                ItemStack item = Utils.getHead(maxValue);
                ItemMeta meta = item.getItemMeta();
                String name = createSection.getString("name");

                meta.setDisplayName(replaceVariables(name));

                List<String> lore = createSection.getStringList("lore");

                for (int i = 0; i < lore.size(); i++) {
                    String replaced = replaceVariables(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                meta.setLore(lore);

                PersistentDataContainer container = meta.getPersistentDataContainer();

                container.set(itemKey, PersistentDataType.STRING, key);

                item.setItemMeta(meta);

                inventory.setItem(createSection.getInt("slot"), item);
            } catch (NullPointerException npe) {
                Bukkit.getLogger().warning("Error while trying to create item. Please check the wiki");
            }
        }


    }


    private String replaceVariables(String string) {
        String colorReplaced = miniMessageParser(string);
        return colorReplaced.
                replace("%enchant%", playerMenuUtility.getEnchant().getName()).
                replace("%current_level%", String.valueOf(currentLevel))
                .replace("%next_level%", String.valueOf(result.getUpgrades() + currentLevel))
                .replace("%price%", String.valueOf(result.getTotalCost()));
    }
}
