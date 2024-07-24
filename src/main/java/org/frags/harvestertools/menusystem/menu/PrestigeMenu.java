package org.frags.harvestertools.menusystem.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.menusystem.Menu;
import org.frags.harvestertools.menusystem.PlayerMenuUtility;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.utils.ToolUtils;
import org.frags.harvestertools.utils.Utils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.frags.harvestertools.managers.MessageManager.miniMessageParse;

public class PrestigeMenu extends Menu {

    private FileConfiguration menuFile;
    private final ConfigurationSection section;
    private final Tools tools;

    public PrestigeMenu(HarvesterTools plugin, PlayerMenuUtility playerMenuUtility) {
        super(plugin, playerMenuUtility);
        menuFile = plugin.menuFile.getConfig();
        section = menuFile.getConfigurationSection("Prestige-Menu");
        tools = playerMenuUtility.getTool();
    }

    @Override
    public String getMenuName() {
        String deserializeTool = tools.name();
        return miniMessageParser(section.getString("title").replace("%tool%", deserializeTool));
    }

    @Override
    public int getSlots() {
        return section.getInt("slots");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (!e.getCurrentItem().hasItemMeta())
            return;
        Player player = (Player) e.getWhoClicked();
        PersistentDataContainer container = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
        if (container.has(new NamespacedKey(plugin, "prestige"), PersistentDataType.STRING)) {
            ConfigurationSection prestigeSection = plugin.getConfig().getConfigurationSection("Prestige");


            //It is prestige item.
            //Boost, ess, money, and enchants.
            //Reset level
            try {
                ItemStack tool = playerMenuUtility.getItem();

                PersistentDataContainer toolContainer = Objects.requireNonNull(tool.getItemMeta()).getPersistentDataContainer();

                int level = toolContainer.get(ToolUtils.levelKey, PersistentDataType.INTEGER);

                if (level < prestigeSection.getInt("level-to-prestige")) {
                    player.sendMessage(miniMessageParse(player,"not-enough-level-to-prestige").replace("%level%",
                            String.valueOf(plugin.getConfig().getInt("Prestige.level-to-prestige"))));
                    player.closeInventory();
                    return;
                }

                toolContainer.set(ToolUtils.levelKey, PersistentDataType.INTEGER, 0);

                int prestige = toolContainer.get(ToolUtils.prestigeKey, PersistentDataType.INTEGER);

                toolContainer.set(ToolUtils.prestigeKey, PersistentDataType.INTEGER, prestige + 1);

                ConfigurationSection section = prestigeSection.getConfigurationSection("boosts");

                if (prestigeSection.getBoolean("boost-essence")) {
                    double essenceBoost = toolContainer.get(ToolUtils.essenceBoostKey, PersistentDataType.DOUBLE);
                    double prestigeEssenceBoost = section.getDouble("essence");
                    toolContainer.set(ToolUtils.essenceBoostKey, PersistentDataType.DOUBLE, essenceBoost + prestigeEssenceBoost);
                }

                if (prestigeSection.getBoolean("boost-money")) {
                    double moneyBoost = toolContainer.get(ToolUtils.moneyBoostKey, PersistentDataType.DOUBLE);
                    double prestigeMoneyBoost = section.getDouble("money");
                    toolContainer.set(ToolUtils.moneyBoostKey, PersistentDataType.DOUBLE, moneyBoost + prestigeMoneyBoost);
                }

                if (prestigeSection.getBoolean("boost-enchants")) {
                    double enchantBoost = toolContainer.get(ToolUtils.enchantBoostKey, PersistentDataType.DOUBLE);
                    double prestigeEnchantBoost = section.getDouble("enchants");
                    toolContainer.set(ToolUtils.enchantBoostKey, PersistentDataType.DOUBLE, enchantBoost + prestigeEnchantBoost);
                }
                player.sendMessage(miniMessageParser("prestiged-item").replace("%prestige%", String.valueOf(prestige + 1)));
            } catch (NullPointerException er) {
                Bukkit.getLogger().warning("Error while trying to prestige an item, report this error to the developer please.");
                Bukkit.getLogger().warning("Null error.");
                return;
            }
        }
    }

    @Override
    public void setMenuItems() {
        ConfigurationSection itemSection = section.getConfigurationSection("items");
        for (String key : itemSection.getKeys(false)) {
            ConfigurationSection madeItem = itemSection.getConfigurationSection(key);
            if (madeItem.getString("item").equalsIgnoreCase("reset")) {
                //It's a special item
                //Value can be a material, if the material is null, it will get the base64
                String value = madeItem.getString("value");
                ItemStack reset;
                if (Material.getMaterial(value) != null) {
                    //Then use custom Material
                    reset = new ItemStack(Material.getMaterial(value));
                } else {
                    //Use head
                    reset = Utils.getHead(value);
                }
                ItemMeta resetMeta = reset.getItemMeta();
                //Name
                String name = miniMessageParser(madeItem.getString("name"));
                resetMeta.setDisplayName(name);

                //Lore
                List<String> lore = madeItem.getStringList("lore");

                for (int i = 0; i < lore.size(); i++) {
                    String replaced = replaceVariables(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                resetMeta.setLore(lore);

                PersistentDataContainer container = resetMeta.getPersistentDataContainer();

                container.set(new NamespacedKey(plugin, "prestige"), PersistentDataType.STRING, UUID.randomUUID().toString());

                reset.setItemMeta(resetMeta);

                inventory.setItem(madeItem.getInt("slot"), reset);
            } else {
                String value = madeItem.getString("value");
                ItemStack item;
                if (value == null || value.isEmpty()) {
                    //Then use material
                    try {
                        Material material = Material.getMaterial(madeItem.getString("item"));
                        item = new ItemStack(material);
                    } catch (NullPointerException e) {
                        Bukkit.getLogger().warning("Material is null!");
                        continue;
                    }
                } else {
                    //Use head
                    item = Utils.getHead(value);
                }
                ItemMeta itemMeta = item.getItemMeta();
                //Name
                String name = miniMessageParser(madeItem.getString("name"));
                itemMeta.setDisplayName(name);

                //Lore
                List<String> lore = madeItem.getStringList("lore");
                for (int i = 0; i < lore.size(); i++) {
                    String replaced = replaceVariables(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                itemMeta.setLore(lore);

                item.setItemMeta(itemMeta);

                int slot = madeItem.getInt("slot");

                inventory.setItem(slot, item);
            }
        }
    }

    public String replaceVariables(String string) {
        try {
            PersistentDataContainer toolContainer = playerMenuUtility.getItem().getItemMeta().getPersistentDataContainer();
            int level = toolContainer.get(ToolUtils.levelKey, PersistentDataType.INTEGER);
            double essBoost = toolContainer.get(ToolUtils.essenceBoostKey, PersistentDataType.DOUBLE);
            double moneyBoost = toolContainer.get(ToolUtils.moneyBoostKey, PersistentDataType.DOUBLE);
            double enchantBoost = toolContainer.get(ToolUtils.enchantBoostKey, PersistentDataType.DOUBLE);
            return miniMessageParser(string.replace("%level%", String.valueOf(level)).
                    replace("%current_essence_boost%", String.valueOf(essBoost)).replace("%current_money_boost%", String.valueOf(moneyBoost)).
                    replace("%current_enchant_boost%", String.valueOf(enchantBoost)).replace("%level_to_prestige%", String.valueOf(
                            plugin.getConfig().getInt("Prestige.level-to-prestige"))));
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("This shouldn't be happening, please report it to the dev - Null PDC");

        }
        return  null;
    }
}
