package org.frags.harvestertools.menusystem.toolmenus;

import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enchants.Result;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.menusystem.Menu;
import org.frags.harvestertools.menusystem.PlayerMenuUtility;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;
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
        return miniMessageParser(section.getString("title")).replace("%enchant%", playerMenuUtility.getEnchant().getCustomName());
    }

    @Override
    public int getSlots() {
        return section.getInt("slots", 27);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        PersistentDataContainer container = clickedItem.getItemMeta().getPersistentDataContainer();

        String key = container.get(itemKey, PersistentDataType.STRING);

        if (key == null)
            return;

        ItemStack playerTool = playerMenuUtility.getItem();
        String enchantName = playerMenuUtility.getEnchant().getName();
        int maxEnchant = playerMenuUtility.getEnchant().getMaxLevel();
        int currentLevel = plugin.getEnchantsManager().getEnchantmentLevel(playerTool, playerMenuUtility.getEnchant());

        e.setCancelled(true);

        if (key.equalsIgnoreCase("max-item")) {
            int upgrades = result.getUpgrades();
            plugin.getEnchantsManager().enchantItem(enchantName, playerTool, upgrades, playerMenuUtility.getOwner());
            e.getWhoClicked().closeInventory();

        } else if (key.equalsIgnoreCase("select-item")) {
            // Mismo código para abrir GUI Sign con ingreso manual
            SignGUI gui = SignGUI.builder()
                    .setLines(null, "^^^^^^^^", "numbers of levels", "to upgrade")
                    .setType(Material.OAK_SIGN)
                    .setHandler((player, signGUIResult) -> {
                        String line0 = signGUIResult.getLine(0);

                        if (line0.isEmpty()) {
                            return Collections.emptyList();
                        }

                        try {
                            int number = Integer.parseInt(line0);
                            if (number == 0) {
                                return List.of(SignGUIAction.displayNewLines(null, "^^^^^^^^", "numbers of levels", "to upgrade"));
                            }

                            int currentLevelInner = plugin.getEnchantsManager().getEnchantmentLevel(playerTool, playerMenuUtility.getEnchant());
                            int maxEnchantInner = playerMenuUtility.getEnchant().getMaxLevel();

                            if (number + currentLevelInner > maxEnchantInner) {
                                MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("higher-than-max")
                                        .replace("%maxLevel%", String.valueOf(maxEnchantInner)));
                                return List.of(SignGUIAction.displayNewLines(null, "^^^^^^^^", "numbers of levels", "to upgrade"));
                            }

                            plugin.getEnchantsManager().enchantItem(enchantName, playerTool, number, player);
                        } catch (NumberFormatException ex) {
                            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-number"));
                            return List.of(SignGUIAction.displayNewLines(null, "^^^^^^^^", "numbers of levels", "to upgrade"));
                        }

                        return Collections.emptyList();
                    })
                    .build();

            gui.open(playerMenuUtility.getOwner());

        } else if (key.startsWith("buy-")) {
            try {
                int levelsToBuy = Integer.parseInt(key.substring(4));

                if (currentLevel + levelsToBuy > maxEnchant) {
                    MessageManager.miniMessageSender(e.getWhoClicked(), plugin.messages.getConfig().getString("higher-than-max")
                            .replace("%maxLevel%", String.valueOf(maxEnchant)));
                    return;
                }

                plugin.getEnchantsManager().enchantItem(enchantName, playerTool, levelsToBuy, playerMenuUtility.getOwner());
                e.getWhoClicked().closeInventory();

            } catch (NumberFormatException ex) {
                MessageManager.miniMessageSender(e.getWhoClicked(), plugin.messages.getConfig().getString("not-number"));
            }
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
                    lore.set(i, replaced);
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

        setFillerGlass(section.getString("filler"));
    }

    private String replaceVariables(String string) {
        String colorReplaced = miniMessageParser(string);

        // Preparamos reemplazos para los precios dinámicos de diferentes niveles
        BigDecimal price1 = getPriceForLevels(1);
        BigDecimal price5 = getPriceForLevels(5);
        BigDecimal price10 = getPriceForLevels(10);
        BigDecimal price20 = getPriceForLevels(20);

        return colorReplaced
                .replace("%enchant%", playerMenuUtility.getEnchant().getCustomName())
                .replace("%current_level%", String.valueOf(currentLevel))
                .replace("%next_level%", String.valueOf(result.getUpgrades() + currentLevel))
                .replace("%price%", Utils.formatNumber(BigDecimal.valueOf(result.getTotalCost())))
                .replace("%upgrades%", String.valueOf(result.getUpgrades()))
                .replace("%price_1%", Utils.formatNumber(price1))
                .replace("%price_5%", Utils.formatNumber(price5))
                .replace("%price_10%", Utils.formatNumber(price10))
                .replace("%price_20%", Utils.formatNumber(price20));
    }

    /**
     * Método para calcular el costo de mejorar X niveles.
     * Ajusta según la lógica del plugin.
     */
    private BigDecimal getPriceForLevels(int levels) {
        // Aquí debes usar la lógica de cálculo del plugin para el costo de 'levels' niveles.
        // El método calculateCostForLevels es un ejemplo que deberías adaptar según tu plugin.

        // Por simplicidad, asumimos que el costo es proporcional al resultado actual.
        BigDecimal costPerLevel = BigDecimal.valueOf(result.getTotalCost())
                .divide(BigDecimal.valueOf(result.getUpgrades()), BigDecimal.ROUND_HALF_UP);

        return costPerLevel.multiply(BigDecimal.valueOf(levels));
    }
}
