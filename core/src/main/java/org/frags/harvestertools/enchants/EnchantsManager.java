package org.frags.harvestertools.enchants;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;
import java.util.*;

import static org.frags.harvestertools.HarvesterTools.getInstance;

public class EnchantsManager {

    private final HarvesterTools plugin;

    private FileConfiguration hoeConfig;
    private FileConfiguration rodConfig;
    private FileConfiguration pickaxeConfig;
    private FileConfiguration swordConfig;

    private final Map<EnchantIdentifier, CustomEnchant> enchantmentsIdentifier = new HashMap<>();

    private final List<CustomEnchant> hoeEnchants = new ArrayList<>();

    private final List<CustomEnchant> pickaxeEnchants = new ArrayList<>();

    private final List<CustomEnchant> rodEnchants = new ArrayList<>();

    private final List<CustomEnchant> swordEnchants = new ArrayList<>();



    private final NamespacedKey toolsKey;


    public EnchantsManager(HarvesterTools plugin) {
        this.plugin = plugin;
        loadEnchantments();
        this.toolsKey = new NamespacedKey(plugin, "tool");
    }

    private void loadEnchantments(){
        loadEnchantment(plugin.hoeEnchantsFile.getConfig(), Tools.hoe, "Hoe Config");
        loadEnchantment(plugin.rodEnchantsFile.getConfig(), Tools.rod, "Rod Config");
        loadEnchantment(plugin.pickaxeEnchantsFile.getConfig(), Tools.pickaxe, "Pickaxe Config");
        loadEnchantment(plugin.swordEnchantsFile.getConfig(), Tools.sword, "Sword Config");
    }

    private void loadEnchantment(FileConfiguration config, Tools tool, String name) {
        Bukkit.getLogger().info("Loading " + name + "...");
        for (String key : config.getConfigurationSection("CustomEnchants").getKeys(false)) {
            int maxLevel = config.getInt("CustomEnchants." + key + ".maxLevel");
            double initialPrice = config.getDouble("CustomEnchants." + key + ".price");
            double increasePrice = config.getDouble("CustomEnchants." + key + ".price-increase");
            boolean upgradable = config.getBoolean("CustomEnchants." + key + ".upgradable");
            String customName = config.getString("CustomEnchants." + key + ".name");
            int requiredLevel = config.getInt("CustomEnchants." + key + ".required-level");
            int requiredPrestige = config.getInt("CustomEnchants." + key + ".required-prestige");
            double boostPerLevel = config.getDouble("CustomEnchants." + key + ".boost-per-level");
            double chancePerLevel = config.getDouble("CustomEnchants." + key + ".chance-per-level");

            ConfigurationSection commandsSection = config.getConfigurationSection("CustomEnchants." + key + ".commands");

            EnchantRewards rewards = null;

            if (commandsSection != null) {
                HashMap<String, Double> commandMap = new HashMap<>();
                for (String commandKey: commandsSection.getKeys(false)) {
                    ConfigurationSection commandSection = commandsSection.getConfigurationSection(commandKey);

                    String command = commandSection.getString("command");
                    double chance = commandSection.getDouble("chance");
                    commandMap.put(command, chance);
                }
                rewards = new EnchantRewards(commandMap);
            }

            CustomEnchant enchant = new CustomEnchant(key, maxLevel, initialPrice, increasePrice, upgradable, customName, requiredLevel, requiredPrestige, tool, boostPerLevel, chancePerLevel, rewards);
            EnchantIdentifier identifier = new EnchantIdentifier(tool, key);
            enchantmentsIdentifier.put(identifier, enchant);

            if (commandsSection != null) {
                if (name.contains("Hoe")) {
                    hoeEnchants.add(enchant);
                } else if (name.contains("Rod")) {
                    rodEnchants.add(enchant);
                } else if (name.contains("Pickaxe")) {
                    pickaxeEnchants.add(enchant);
                } else if (name.contains("Sword")) {
                    swordEnchants.add(enchant);
                }
            }

            Bukkit.getLogger().info("Registered enchant: " + key);
        }
    }

    public CustomEnchant getEnchant(String key, Tools tool) {
        return enchantmentsIdentifier.get(new EnchantIdentifier(tool, key));
    }

    public CustomEnchant getEnchant(EnchantIdentifier identifier) {
        return enchantmentsIdentifier.get(identifier);
    }

    public void enchantItem(String enchantName, ItemStack item, int level, Player player) {
        if (!item.hasItemMeta()) return;
        try {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(new NamespacedKey(plugin, "tool"), PersistentDataType.STRING)) return;

            EnchantIdentifier identifier = new EnchantIdentifier(getToolType(item), enchantName);
            CustomEnchant enchantment = enchantmentsIdentifier.get(identifier);
            if (enchantment == null) {
                Bukkit.getLogger().warning("Enchantment " + enchantName + " not found");
                return;
            }

            if (level < 1 || level > enchantment.getMaxLevel()) return;

            NamespacedKey key = new NamespacedKey(plugin, enchantName);

            double price = calculatePrice(enchantment, level, item);
            int currentLevel = getEnchantmentLevel(item, enchantment);
            int newLevel = currentLevel + level;

            if (!plugin.getEssenceManager().withdrawEssence(player, price)) {
                MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-enough-essence"));
                return;
            }
            if (enchantName.equalsIgnoreCase("sweeping")) {
                if (meta.hasEnchant(Enchantment.SWEEPING_EDGE)) {
                    meta.removeEnchant(Enchantment.SWEEPING_EDGE);
                }
                meta.addEnchant(Enchantment.SWEEPING_EDGE, newLevel, true);
            } else if (enchantName.equalsIgnoreCase("efficiency")) {
                if (meta.hasEnchant(Enchantment.DIG_SPEED)) {
                    meta.removeEnchant(Enchantment.DIG_SPEED);
                }
                meta.addEnchant(Enchantment.DIG_SPEED, newLevel, true);
            } else if (enchantName.equalsIgnoreCase("fortune")) {
                if (meta.hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
                    meta.removeEnchant(Enchantment.LOOT_BONUS_BLOCKS);
                }
                meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, newLevel, true);
            }

            if (!meta.hasEnchants())
                meta.addEnchant(Enchantment.DURABILITY, 1, true);

            if (newLevel > enchantment.getMaxLevel()) newLevel = enchantment.getMaxLevel();
            container.set(key, PersistentDataType.INTEGER, newLevel);

            updateLore(meta, enchantment, newLevel);
            item.setItemMeta(meta);

            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("enchanted-item")
                    .replace("%level%", String.valueOf(level))
                    .replace("%price%", String.valueOf(Utils.formatNumber(BigDecimal.valueOf(price)))));
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Error while trying to enchant item! Contact the developer");
        }
    }

    private double calculatePrice(CustomEnchant enchantment, int level, ItemStack item) {
        int toolLevel = getEnchantmentLevel(item, enchantment);
        double price = 0D;
        for (int i = 0; i < level; i++) {
            price += getLevelPrice(enchantment, toolLevel + i);
        }
        return price;
    }

    private void updateLore(ItemMeta meta, CustomEnchant enchantment, int newLevel) {
        List<String> lore = meta.getLore();
        String enchantmentFormat = MessageManager.miniStringParse(plugin.getConfig().getString("enchantList"))
                .replace("%enchant%", enchantment.getCustomName())
                .replace("%level%", String.valueOf(newLevel))
                .replace("%maxLevel%", enchantment.isUpgradable() ? String.valueOf(enchantment.getMaxLevel()) : "1");
        boolean found = false;
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains(enchantment.getCustomName())) {
                lore.set(i, enchantmentFormat);
                found = true;
                break;
            }
        }
        if (!found) {
            lore.add(enchantmentFormat);
        }
        meta.setLore(lore);
    }

    private Tools getToolType(ItemStack item) {
        // Implement this method to determine the tool type based on item properties or metadata
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return Tools.getTool(Objects.requireNonNull(container.get(toolsKey, PersistentDataType.STRING))); // Example, replace with actual implementation
    }

    public Result calculateMaxUpgrades(Player player, CustomEnchant enchant, ItemStack item) {
        double balance = plugin.getEssenceManager().getEssence(player);
        int itemLevel = getEnchantmentLevel(item, enchant);
        int maxLevel = enchant.getMaxLevel();

        if (itemLevel >= maxLevel) return Result.EMTPY;

        int upgrades = 0;
        double totalCost = 0;

        while (balance >= totalCost && itemLevel + upgrades < maxLevel) {
            double levelPrice = getLevelPrice(enchant, itemLevel + upgrades);
            if (balance < totalCost + levelPrice) break;
            totalCost += levelPrice;
            upgrades++;
        }

        return new Result(upgrades, totalCost);
    }

    public double getNextLevelPrice(ItemStack item, CustomEnchant enchant) {
        int level = getEnchantmentLevel(item, enchant);
        return enchant.getInitialPrice() + (enchant.getIncreasePrice() * level);
    }

    public double getLevelPrice(CustomEnchant enchant, int level) {
        return enchant.getInitialPrice() + (enchant.getIncreasePrice() * level);
    }

    public boolean hasEnchantment(ItemStack item, CustomEnchant enchant) {
        if (item == null || item.getType() == Material.AIR) return false;
        NamespacedKey key = new NamespacedKey(plugin, enchant.getName());
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }

    public int getEnchantmentLevel(ItemStack item, CustomEnchant enchant) {
        if (item == null || item.getType() == Material.AIR) return 0;
        ItemMeta meta = item.getItemMeta();
        if (!item.hasItemMeta()) return 0;
        NamespacedKey key = new NamespacedKey(plugin, enchant.getName());
        return meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public List<CustomEnchant> getHoeEnchants() {
        return hoeEnchants;
    }

    public List<CustomEnchant> getPickaxeEnchants() {
        return pickaxeEnchants;
    }

    public List<CustomEnchant> getRodEnchants() {
        return rodEnchants;
    }

    public List<CustomEnchant> getSwordEnchants() {
        return swordEnchants;
    }

    /*public void enchantItem(String enchantName, ItemStack item, int level, Player player) {
        if (!item.hasItemMeta()) return;
        try {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            //Only tools can be enchanted
            if (!container.has(new NamespacedKey(getInstance(), "tool"), PersistentDataType.STRING)) return;

            if (enchantmentsIdentifier.get(enchantName) == null) {
                Bukkit.getLogger().warning("Enchantment " + enchantName + " not found");
                return;
            }

            CustomEnchant enchantment = enchantmentsIdentifier.get(enchantName);

            if (level < 1 || level > enchantment.getMaxLevel()) return; // Invalid level


            NamespacedKey key = new NamespacedKey(plugin, enchantName);

            double levels = 0;
            double price = 0D;

            int toolLevel = getEnchantmentLevel(item, enchantment);

            while (levels < level) {
                levels++;
                price = price + getLevelPrice(enchantment, toolLevel);
                toolLevel++;
            }



            for (int i = 0; i < level; i++) {
                price += getLevelPrice(enchantment, toolLevel + i);
            }

            int currentLevel = getEnchantmentLevel(item, enchantment);

            int newLevel = currentLevel + level;


            if (!plugin.getEssenceManager().withdrawEssence(player, price)) {
                //Not Enough essence
                MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-enough-essence"));
                return;
            }

            if (!meta.hasEnchant(Enchantment.DURABILITY)) {
                //Method to add glow
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
            }

            //Enchants item

            if (newLevel > enchantment.getMaxLevel()) newLevel = enchantment.getMaxLevel();
            container.set(key, PersistentDataType.INTEGER, newLevel);

            //Adds Lore

            List<String> lore = meta.getLore();

            String enchantmentFormat = MessageManager.miniStringParse(plugin.getConfig().getString("enchantList")).replace("%enchant%", enchantment.getCustomName())
                  .replace("%level%", String.valueOf(newLevel))
                  .replace("%maxLevel%", enchantment.isUpgradable() ? String.valueOf(enchantment.getMaxLevel()) : "1");
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).contains(enchantName)) {
                    lore.remove(i);
                    lore.add(i, enchantmentFormat);
                    meta.setLore(lore);
                    item.setItemMeta(meta);


                    MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("enchanted-item").replace("%level%", String.valueOf(level)).
                            replace("%price%", String.valueOf(price)));
                    return;
                }
            }

            lore.add(enchantmentFormat);
            meta.setLore(lore);
            item.setItemMeta(meta);

            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("enchanted-item").replace("%level%", String.valueOf(level)).
                    replace("%price%", String.valueOf(price)));
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Error while trying to enchant item! Contact with dev");
        }
    }

    //This returns level and Price
    public Result calculateMaxUpgrades(Player player, CustomEnchant enchant, ItemStack item) {
        double balance = plugin.getEssenceManager().getEssence(player);
        double initialPrice = getNextLevelPrice(item, enchant);

        int itemLevel = getEnchantmentLevel(item, enchant);

        int upgrades = 0;
        double currentPrice = initialPrice;
        double totalCost = 0;

        while (balance >= currentPrice) {
            if (itemLevel + upgrades > enchant.getMaxLevel())
                break;
            balance -= currentPrice;
            totalCost += currentPrice;
            upgrades++;
            currentPrice += enchant.getIncreasePrice();
        }


        return new Result(upgrades - 1, totalCost);
    }



    public Result calculateMaxUpgrades(Player player, CustomEnchant enchant, ItemStack item) {
        double initialPrice = getNextLevelPrice(item, enchant);
        double firstBalance = plugin.getEssenceManager().getEssence(player);
        int itemLevel = getEnchantmentLevel(item, enchant);


        int upgrades = 0;
        double currentPrice = initialPrice;
        double balance = firstBalance;
        int level = itemLevel;

        while (balance >= currentPrice) {
            if (itemLevel + upgrades >= enchant.getMaxLevel())
                break;
            upgrades++;
            balance = balance - getLevelPrice(enchant, level);
            currentPrice = currentPrice + getLevelPrice(enchant, level);
            level++;
        }
        return new Result(upgrades, currentPrice);
    }



    public Result calculateMaxUpgrades(Player player, CustomEnchant enchant, ItemStack item) {
        double balance = plugin.getEssenceManager().getEssence(player);
        double initialPrice = getNextLevelPrice(item, enchant);

        if(balance < initialPrice) {
            return Result.EMTPY;
        }

        int itemLevel = getEnchantmentLevel(item, enchant);
        int maxLevel = enchant.getMaxLevel();

        if(itemLevel >= enchant.getMaxLevel()) {
            return Result.EMTPY;
        }

        int upgrades = 0;
        double currentPrice = 0;


        while(balance >= currentPrice && itemLevel + upgrades < maxLevel) {
            double levelPrice = getLevelPrice(enchant, itemLevel + upgrades);
            if (balance - (currentPrice + levelPrice) < 0)
                break;
            currentPrice += getLevelPrice(enchant, itemLevel + upgrades);
            System.out.println("currentPrice = " + currentPrice);
            upgrades++;
            System.out.println("Upgrades: " + upgrades);
        }

        return new Result(upgrades, currentPrice);
    }

    public double getNextLevelPrice (ItemStack item, CustomEnchant enchant) {
        int level = getEnchantmentLevel(item, enchant);

        double initialPrice = enchant.getInitialPrice();
        double increasePrice = enchant.getIncreasePrice();

        return initialPrice + (increasePrice * level);
    }

    public double getLevelPrice(CustomEnchant enchant, int level) {
        double initialPrice = enchant.getInitialPrice();
        double increasePrice = enchant.getIncreasePrice();

        return initialPrice + (increasePrice * level);
    }


    public boolean hasEnchantment(ItemStack item, CustomEnchant enchant) {
        if (item == null || item.getType() == Material.AIR) return false;
        NamespacedKey key =  new NamespacedKey(plugin, enchant.getName());
        return item.getItemMeta().getPersistentDataContainer().has(key);
    }

    public int getEnchantmentLevel(ItemStack item, CustomEnchant enchant){
        if (item == null || item.getType() == Material.AIR) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        if (!item.hasItemMeta()) return 0;
        NamespacedKey key = new NamespacedKey(plugin, enchant.getName());
        return meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public Collection<CustomEnchant> getEnchantmentsList() {
        return enchantmentsIdentifier.values();
    }

     */

}

