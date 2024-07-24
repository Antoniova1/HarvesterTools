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
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enchants.EnchantIdentifier;
import org.frags.harvestertools.enchants.EnchantsManager;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.menusystem.Menu;
import org.frags.harvestertools.menusystem.PlayerMenuUtility;
import org.frags.harvestertools.utils.ToolUtils;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;
import java.util.List;

public class EnchantsMenu extends Menu {

    private final NamespacedKey namespacedKey;

    private final FileConfiguration enchantsFile;
    private final ConfigurationSection menuSection;
    private final ConfigurationSection enchantsSection;
    private final Tools tool;

    public EnchantsMenu(HarvesterTools plugin, PlayerMenuUtility playerMenuUtility) {
        super(plugin, playerMenuUtility);
        this.namespacedKey = new NamespacedKey(plugin, "enchant");
        tool = playerMenuUtility.getTool();
        if (tool == Tools.hoe) {
            enchantsFile = plugin.hoeEnchantsFile.getConfig();
        } else if (tool == Tools.pickaxe) {
            enchantsFile = plugin.pickaxeEnchantsFile.getConfig();
        } else if (tool == Tools.rod) {
            enchantsFile = plugin.rodEnchantsFile.getConfig();
        } else {
            enchantsFile = plugin.swordEnchantsFile.getConfig();
        }
        menuSection = enchantsFile.getConfigurationSection("EnchantsMenu");
        enchantsSection = enchantsFile.getConfigurationSection("CustomEnchants");
    }

    @Override
    public String getMenuName() {
        return miniMessageParser(menuSection.getString("name"));
    }

    @Override
    public int getSlots() {
        return menuSection.getInt("size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        //Add essence here
        ItemStack itemStack = e.getCurrentItem();
        if (!itemStack.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING))
            return;
        String enchantName = itemStack.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        EnchantIdentifier identifier = new EnchantIdentifier(playerMenuUtility.getTool(), enchantName);
        CustomEnchant enchant = plugin.getEnchantsManager().getEnchant(identifier);
        //This is the enchant the player want to add
        playerMenuUtility.setEnchant(enchant);
        Player player = (Player) e.getWhoClicked();

        int toolLevel = ToolUtils.getItemLevel(playerMenuUtility.getItem());

        int toolPrestige = ToolUtils.getItemPrestige(playerMenuUtility.getItem());

        System.out.println(enchant.getName());
        System.out.println(enchant.getRequiredLevel());
        System.out.println(enchant.getRequiredPrestige());

        System.out.println(toolLevel);

        if (toolLevel < enchant.getRequiredLevel()) {
            player.closeInventory();
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-enough-level").replace("%level%", enchant.getRequiredLevel() + ""));
            return;
        }

        if (toolPrestige < enchant.getRequiredPrestige()) {
            player.closeInventory();
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-enough-prestige").replace("%prestige%", enchant.getRequiredPrestige() + ""));
            return;
        }

        if (enchant.isUpgradable()) {
            if (plugin.getEnchantsManager().getEnchantmentLevel(playerMenuUtility.getItem(), enchant) >= enchant.getMaxLevel()) {
                player.closeInventory();
                MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("already-max-level"));
                return;
            }
            new ConfirmEnchantMenu(plugin, playerMenuUtility).open();
        } else {
            //Enchant item 1 level
            if (!plugin.getEnchantsManager().hasEnchantment(playerMenuUtility.getItem(), enchant)) {
                plugin.getEnchantsManager().enchantItem(enchantName, playerMenuUtility.getItem(), 1, player);
                player.closeInventory();
                return;
            }
            player.closeInventory();
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("already-have-enchant"));

        }
    }

    @Override
    public void setMenuItems() {
        if (menuSection.getBoolean("item-preview")) {
            int previewSlot = menuSection.getInt("item-preview-slot");
            inventory.setItem(previewSlot, playerMenuUtility.getItem());
        }

        for (String key : enchantsSection.getKeys(false)) {
            ConfigurationSection section = enchantsSection.getConfigurationSection(key);
            String value = section.getString("value");
            ItemStack itemStack;
            if (value == null || value.isEmpty()) {
                //Then use material
                try {
                    Material material = Material.getMaterial(section.getString("material"));
                    itemStack = new ItemStack(material);
                } catch (NullPointerException e) {
                    Bukkit.getLogger().warning("Material is null!");
                    continue;
                }
            } else {
                //Use head
                itemStack = Utils.getHead(value);
            }
            CustomEnchant enchant = null;
            try {
                enchant = plugin.getEnchantsManager().getEnchant(key, playerMenuUtility.getTool());
            } catch (NullPointerException e) {
                Bukkit.getLogger().warning("This enchant doesn't exist!");
            }

            try {
                String name = enchant.getCustomName();

                ItemMeta meta = itemStack.getItemMeta();

                meta.setDisplayName(name);

                List<String> lore = section.getStringList("lore");

                int enchantLevel = plugin.getEnchantsManager().getEnchantmentLevel(playerMenuUtility.getItem(), enchant);

                double price = enchant.getInitialPrice() + (enchantLevel + 1) * enchant.getIncreasePrice();

                String formattedPrice = Utils.formatNumber(new BigDecimal(price));

                for (int i = 0; i < lore.size(); i++) {
                    String replaced = miniMessageParser(lore.get(i)).replace("%price%", formattedPrice).
                            replace("%level%", String.valueOf(enchantLevel)
                                    .replace("%maxLevel%", String.valueOf(enchant.getMaxLevel())));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                meta.setLore(lore);

                PersistentDataContainer container = meta.getPersistentDataContainer();

                container.set(namespacedKey, PersistentDataType.STRING, key);

                itemStack.setItemMeta(meta);

                int slot = section.getInt("slot");

                inventory.setItem(slot, itemStack);
            } catch (NullPointerException e) {
                Bukkit.getLogger().warning("Error while creating an item, check the wiki.!");
            }

        }
    }
}
