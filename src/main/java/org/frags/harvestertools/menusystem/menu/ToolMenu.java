package org.frags.harvestertools.menusystem.menu;

import me.clip.placeholderapi.PlaceholderAPI;
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
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.menusystem.Menu;
import org.frags.harvestertools.menusystem.PlayerMenuUtility;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.utils.Utils;

import java.util.HashMap;
import java.util.List;

public class ToolMenu extends Menu {

    private final Tools tools;
    private final FileConfiguration menuFile;
    private final ConfigurationSection section;

    private HashMap<Integer, ItemStack> itemSlot = new HashMap<>();

    private NamespacedKey key = new NamespacedKey(plugin, "commands");

    public ToolMenu(HarvesterTools plugin, PlayerMenuUtility playerMenuUtility) {
        super(plugin, playerMenuUtility);
        tools = playerMenuUtility.getTool();
        menuFile = plugin.menuFile.getConfig();
        section = menuFile.getConfigurationSection("Tool-Menu");
    }



    @Override
    public String getMenuName() {
        String deserializeTool = tools.name();
        return miniMessageParser(section.getString(deserializeTool + "-name").replace("%tool%", deserializeTool));
    }

    @Override
    public int getSlots() {
        return section.getInt("slots");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (!item.hasItemMeta())
            return;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (item.isSimilar(itemSlot.get(e.getSlot()))) {
            //It is a custom item (With commands)
            if (container.has(key, PersistentDataType.STRING)) {
                String commands = container.get(key, PersistentDataType.STRING);
                if (commands == null)
                    return;
                String[] splittedCommand = commands.split(",");
                for (String command : splittedCommand) {
                    if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        //If PlaceholderAPI is on the server:
                         command = PlaceholderAPI.setPlaceholders(player, command);
                    } else {
                        //If not just replace player (Will be adding more if the users request them)
                        command = command.replace("%player%", player.getName());
                    }
                    if (command.equalsIgnoreCase("[close]")) {
                        //Special command
                        player.closeInventory();
                        continue;
                    } else if (command.contains("[message]")) {
                        String message = command.replace("[message]", "");
                        if (message.charAt(0) == ' ') {
                            message = message.replaceFirst(" ", "");
                        }
                        MessageManager.miniMessageSender(player, message);
                        continue;
                    }
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        } else if (container.has(new NamespacedKey(plugin, "prestige"), PersistentDataType.STRING)) {
            //Item used for prestige - Opens prestige menu
            new PrestigeMenu(plugin, playerMenuUtility).open();
        } else if (container.has(new NamespacedKey(plugin, "enchants"), PersistentDataType.STRING)) {
            new EnchantsMenu(plugin, playerMenuUtility).open();
        }
    }

    @Override
    public void setMenuItems() {
        if (section.getBoolean("item-preview")) {
            int previewSlot = section.getInt("item-preview-slot");
            inventory.setItem(previewSlot, playerMenuUtility.getItem());
        }
        if (tools == Tools.hoe) {
            setHoeItems();
        } else if (tools == Tools.sword) {
            setSwordItems();
        } else if (tools == Tools.rod) {
            setRodItems();
        } else if (tools == Tools.pickaxe) {
            setPickaxeItems();
        }
    }


    private void setHoeItems() {
        ConfigurationSection hoeSection = section.getConfigurationSection("hoe-menu");
        for (String key : hoeSection.getKeys(false)) {
            ConfigurationSection madeItem = hoeSection.getConfigurationSection(key);
            if (madeItem.getString("item").equalsIgnoreCase("prestige")) {
                //Value can be a material, if the material is null, it will get the base64
                String value = madeItem.getString("value");
                ItemStack prestige;
                if (Material.getMaterial(value) != null) {
                    //Then use custom Material
                    prestige = new ItemStack(Material.getMaterial(value));
                } else {
                    //Use head
                    prestige = Utils.getHead(value);
                }
                ItemMeta prestigeMeta = prestige.getItemMeta();
                //Name
                String name = miniMessageParser(madeItem.getString("name"));
                prestigeMeta.setDisplayName(name);

                //Lore
                List<String> lore = madeItem.getStringList("lore");
                for (int i = 0; i < lore.size(); i++) {
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                prestigeMeta.setLore(lore);

                PersistentDataContainer container = prestigeMeta.getPersistentDataContainer();

                container.set(new NamespacedKey(plugin, "prestige"), PersistentDataType.STRING, "prestige");

                prestige.setItemMeta(prestigeMeta);

                inventory.setItem(madeItem.getInt("slot"), prestige);
            } else if (madeItem.getString("item").equalsIgnoreCase("enchants")) {
                //Value can be a material, if the material is null, it will get the base64
                String value = madeItem.getString("value");
                ItemStack enchants;
                if (Material.getMaterial(value) != null) {
                    //Then use custom Material
                    enchants = new ItemStack(Material.getMaterial(value));
                } else {
                    //Use head
                    enchants = Utils.getHead(value);
                }
                ItemMeta enchantsMeta = enchants.getItemMeta();
                //Name
                String name = miniMessageParser(madeItem.getString("name"));
                enchantsMeta.setDisplayName(name);

                //Lore
                List<String> lore = madeItem.getStringList("lore");
                for (int i = 0; i < lore.size(); i++) {
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                enchantsMeta.setLore(lore);

                PersistentDataContainer container = enchantsMeta.getPersistentDataContainer();

                container.set(new NamespacedKey(plugin, "enchants"), PersistentDataType.STRING, "enchants");

                enchants.setItemMeta(enchantsMeta);

                inventory.setItem(madeItem.getInt("slot"), enchants);
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
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                itemMeta.setLore(lore);

                PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                List<String> commands = madeItem.getStringList("commands");
                if (!commands.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < commands.size(); i++) {
                        if (i > 0) {
                            builder.append(",");
                        }
                        builder.append(commands.get(i));
                    }
                    container.set(this.key, PersistentDataType.STRING, builder.toString());
                }

                item.setItemMeta(itemMeta);

                int slot = madeItem.getInt("slot");

                itemSlot.put(slot, item);

                inventory.setItem(slot, item);
            }
        }
    }

    private void setSwordItems() {
        ConfigurationSection hoeSection = section.getConfigurationSection("sword-menu");
        for (String key : hoeSection.getKeys(false)) {
            ConfigurationSection madeItem = hoeSection.getConfigurationSection(key);
            if (madeItem.getString("item").equalsIgnoreCase("prestige")) {
                //Value can be a material, if the material is null, it will get the base64
                String value = madeItem.getString("value");
                ItemStack prestige;
                if (Material.getMaterial(value) != null) {
                    //Then use custom Material
                    prestige = new ItemStack(Material.getMaterial(value));
                } else {
                    //Use head
                    prestige = Utils.getHead(value);
                }
                ItemMeta prestigeMeta = prestige.getItemMeta();
                //Name
                String name = miniMessageParser(madeItem.getString("name"));
                prestigeMeta.setDisplayName(name);

                //Lore
                List<String> lore = madeItem.getStringList("lore");
                for (int i = 0; i < lore.size(); i++) {
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                prestigeMeta.setLore(lore);

                PersistentDataContainer container = prestigeMeta.getPersistentDataContainer();

                container.set(new NamespacedKey(plugin, "prestige"), PersistentDataType.STRING, "prestige");

                prestige.setItemMeta(prestigeMeta);

                inventory.setItem(madeItem.getInt("slot"), prestige);
            } else if (madeItem.getString("item").equalsIgnoreCase("enchants")) {
                //Value can be a material, if the material is null, it will get the base64
                String value = madeItem.getString("value");
                ItemStack enchants;
                if (Material.getMaterial(value) != null) {
                    //Then use custom Material
                    enchants = new ItemStack(Material.getMaterial(value));
                } else {
                    //Use head
                    enchants = Utils.getHead(value);
                }
                ItemMeta enchantsMeta = enchants.getItemMeta();
                //Name
                String name = miniMessageParser(madeItem.getString("name"));
                enchantsMeta.setDisplayName(name);

                //Lore
                List<String> lore = madeItem.getStringList("lore");
                for (int i = 0; i < lore.size(); i++) {
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                enchantsMeta.setLore(lore);

                PersistentDataContainer container = enchantsMeta.getPersistentDataContainer();

                container.set(new NamespacedKey(plugin, "enchants"), PersistentDataType.STRING, "enchants");

                enchants.setItemMeta(enchantsMeta);

                inventory.setItem(madeItem.getInt("slot"), enchants);
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
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                itemMeta.setLore(lore);

                PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                List<String> commands = madeItem.getStringList("commands");
                if (!commands.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < commands.size(); i++) {
                        if (i > 0) {
                            builder.append(",");
                        }
                        builder.append(commands.get(i));
                    }
                    container.set(this.key, PersistentDataType.STRING, builder.toString());
                }

                item.setItemMeta(itemMeta);

                int slot = madeItem.getInt("slot");

                itemSlot.put(slot, item);

                inventory.setItem(slot, item);
            }
        }
    }
    private void setRodItems() {
        ConfigurationSection hoeSection = section.getConfigurationSection("rod-menu");
        for (String key : hoeSection.getKeys(false)) {
            ConfigurationSection madeItem = hoeSection.getConfigurationSection(key);
            if (madeItem.getString("item").equalsIgnoreCase("prestige")) {
                //Value can be a material, if the material is null, it will get the base64
                String value = madeItem.getString("value");
                ItemStack prestige;
                if (Material.getMaterial(value) != null) {
                    //Then use custom Material
                    prestige = new ItemStack(Material.getMaterial(value));
                } else {
                    //Use head
                    prestige = Utils.getHead(value);
                }
                ItemMeta prestigeMeta = prestige.getItemMeta();
                //Name
                String name = miniMessageParser(madeItem.getString("name"));
                prestigeMeta.setDisplayName(name);

                //Lore
                List<String> lore = madeItem.getStringList("lore");
                for (int i = 0; i < lore.size(); i++) {
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                prestigeMeta.setLore(lore);

                PersistentDataContainer container = prestigeMeta.getPersistentDataContainer();

                container.set(new NamespacedKey(plugin, "prestige"), PersistentDataType.STRING, "prestige");

                prestige.setItemMeta(prestigeMeta);

                inventory.setItem(madeItem.getInt("slot"), prestige);
            } else if (madeItem.getString("item").equalsIgnoreCase("enchants")) {
                //Value can be a material, if the material is null, it will get the base64
                String value = madeItem.getString("value");
                ItemStack enchants;
                if (Material.getMaterial(value) != null) {
                    //Then use custom Material
                    enchants = new ItemStack(Material.getMaterial(value));
                } else {
                    //Use head
                    enchants = Utils.getHead(value);
                }
                ItemMeta enchantsMeta = enchants.getItemMeta();
                //Name
                String name = miniMessageParser(madeItem.getString("name"));
                enchantsMeta.setDisplayName(name);

                //Lore
                List<String> lore = madeItem.getStringList("lore");
                for (int i = 0; i < lore.size(); i++) {
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                enchantsMeta.setLore(lore);

                PersistentDataContainer container = enchantsMeta.getPersistentDataContainer();

                container.set(new NamespacedKey(plugin, "enchants"), PersistentDataType.STRING, "enchants");

                enchants.setItemMeta(enchantsMeta);

                inventory.setItem(madeItem.getInt("slot"), enchants);
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
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                itemMeta.setLore(lore);

                PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                List<String> commands = madeItem.getStringList("commands");
                if (!commands.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < commands.size(); i++) {
                        if (i > 0) {
                            builder.append(",");
                        }
                        builder.append(commands.get(i));
                    }
                    container.set(this.key, PersistentDataType.STRING, builder.toString());
                }

                item.setItemMeta(itemMeta);

                int slot = madeItem.getInt("slot");

                itemSlot.put(slot, item);

                inventory.setItem(slot, item);
            }
        }
    }

    private void setPickaxeItems() {
        ConfigurationSection hoeSection = section.getConfigurationSection("pickaxe-menu");
        for (String key : hoeSection.getKeys(false)) {
            ConfigurationSection madeItem = hoeSection.getConfigurationSection(key);
            if (madeItem.getString("item").equalsIgnoreCase("prestige")) {
                //Value can be a material, if the material is null, it will get the base64
                String value = madeItem.getString("value");
                ItemStack prestige;
                if (Material.getMaterial(value) != null) {
                    //Then use custom Material
                    prestige = new ItemStack(Material.getMaterial(value));
                } else {
                    //Use head
                    prestige = Utils.getHead(value);
                }
                ItemMeta prestigeMeta = prestige.getItemMeta();
                //Name
                String name = miniMessageParser(madeItem.getString("name"));
                prestigeMeta.setDisplayName(name);

                //Lore
                List<String> lore = madeItem.getStringList("lore");
                for (int i = 0; i < lore.size(); i++) {
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                prestigeMeta.setLore(lore);

                PersistentDataContainer container = prestigeMeta.getPersistentDataContainer();

                container.set(new NamespacedKey(plugin, "prestige"), PersistentDataType.STRING, "prestige");

                prestige.setItemMeta(prestigeMeta);

                inventory.setItem(madeItem.getInt("slot"), prestige);
            } else if (madeItem.getString("item").equalsIgnoreCase("enchants")) {
                //Value can be a material, if the material is null, it will get the base64
                String value = madeItem.getString("value");
                ItemStack enchants;
                if (Material.getMaterial(value) != null) {
                    //Then use custom Material
                    enchants = new ItemStack(Material.getMaterial(value));
                } else {
                    //Use head
                    enchants = Utils.getHead(value);
                }
                ItemMeta enchantsMeta = enchants.getItemMeta();
                //Name
                String name = miniMessageParser(madeItem.getString("name"));
                enchantsMeta.setDisplayName(name);

                //Lore
                List<String> lore = madeItem.getStringList("lore");
                for (int i = 0; i < lore.size(); i++) {
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                enchantsMeta.setLore(lore);

                PersistentDataContainer container = enchantsMeta.getPersistentDataContainer();

                container.set(new NamespacedKey(plugin, "enchants"), PersistentDataType.STRING, "enchants");

                enchants.setItemMeta(enchantsMeta);

                inventory.setItem(madeItem.getInt("slot"), enchants);
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
                    String replaced = miniMessageParser(lore.get(i));
                    lore.remove(i);
                    lore.add(i, replaced);
                }
                itemMeta.setLore(lore);

                PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                List<String> commands = madeItem.getStringList("commands");
                if (!commands.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < commands.size(); i++) {
                        if (i > 0) {
                            builder.append(",");
                        }
                        builder.append(commands.get(i));
                    }
                    container.set(this.key, PersistentDataType.STRING, builder.toString());
                }

                item.setItemMeta(itemMeta);

                int slot = madeItem.getInt("slot");

                itemSlot.put(slot, item);

                inventory.setItem(slot, item);
            }
        }
    }
}
