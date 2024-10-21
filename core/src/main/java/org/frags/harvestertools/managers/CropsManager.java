package org.frags.harvestertools.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.objects.Drops;
import org.frags.harvestertools.objects.CustomDrops;
import org.frags.harvestertools.objects.HarvesterDrops;
import org.frags.harvestertools.objects.ItemsChance;
import org.frags.harvestertools.utils.Utils;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CropsManager {

    private final HarvesterTools plugin;

    public CropsManager(HarvesterTools plugin) {
        this.plugin = plugin;
        loadCrops();
    }

    private HashMap<Material, HarvesterDrops> cropsHashMap = new HashMap<>();

    public void loadCrops() {
        cropsHashMap.clear();
        if (!plugin.cropsFile.getConfig().getBoolean("enable_custom")) {
            ConfigurationSection cropsFile = plugin.cropsFile.getConfig().getConfigurationSection("crops");
            for (String key : cropsFile.getKeys(false)) {
                ConfigurationSection crop = cropsFile.getConfigurationSection(key);
                Material material;
                try {
                    material = Material.valueOf(key.toUpperCase());
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Material " + key + " is not a valid material");
                    Bukkit.getLogger().warning("Why are you changing keys in crops.yml?");
                    continue;
                }
                int level = crop.getInt("level");
                double price = crop.getDouble("price");
                double essencePrice = crop.getDouble("essence-price");
                double experience = crop.getDouble("experience");
                Drops crops = new Drops(material, level, price, essencePrice, experience);
                cropsHashMap.put(material, crops);
            }
            return;
        }
        //Manage custom crops (Somehow)
        ConfigurationSection custom_crops = plugin.cropsFile.getConfig().getConfigurationSection("custom_crops");
        for (String key : custom_crops.getKeys(false)) {
            int level = plugin.cropsFile.getConfig().getInt("crops." + key + ".level");
            ConfigurationSection crop = custom_crops.getConfigurationSection(key);
            Material material;
            try {
                material = Material.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Error while creating custom crops");
                Bukkit.getLogger().warning("Material " + key + " is not a valid material");
                System.out.println(1);
                continue;
            }
            int rolls = crop.getInt("rolls");
            List<ItemsChance> itemsChanceList = new ArrayList<>();
            for (String dropKey : crop.getConfigurationSection("drops").getKeys(false)) {
                ConfigurationSection drop = crop.getConfigurationSection("drops." + dropKey);

                ItemStack itemStack;
                try {
                    itemStack = new ItemStack(Material.valueOf(drop.getString("material")));
                } catch (IllegalArgumentException e) {
                    System.out.println(2);
                    itemStack = Utils.getHead(drop.getString("material"));
                }

                double chance = drop.getDouble("chance");

                String name = drop.getString("name");

                ItemMeta meta = itemStack.getItemMeta();

                meta.setDisplayName(MessageManager.miniStringParse(name));

                if (drop.getBoolean("glow")) {
                    meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                List<String> itemFlags = drop.getStringList("item-flags");
                if (!itemFlags.isEmpty()) {
                    for (String itemFlag : itemFlags) {
                        try {
                            meta.addItemFlags(ItemFlag.valueOf(itemFlag));
                        } catch (IllegalArgumentException e) {
                            Bukkit.getLogger().warning("Error while creating custom crops");
                            Bukkit.getLogger().warning("ItemFlag " + itemFlag + " is not a valid item flag");
                            continue;
                        }
                    }
                }

                List<String> lore = drop.getStringList("lore");
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, MessageManager.miniStringParse(lore.get(i)));
                }

                meta.setLore(lore);

                itemStack.setItemMeta(meta);

                double price = drop.getDouble("price");
                double essence = drop.getDouble("essence-price");
                double experience = drop.getDouble("experience");

                itemsChanceList.add(new ItemsChance(itemStack, chance, price, essence, experience));
            }

            CustomDrops customDrop = new CustomDrops(material, level, rolls, itemsChanceList);
            cropsHashMap.put(material, customDrop);
        }

    }

    /*@Nullable
    public List<ItemsChance> roll(CustomCrops crop) {
        List<ItemsChance> itemsChanceList = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < crop.getRolls(); i++) {
            for (ItemsChance itemsChance : crop.getItems()) {
                double roll = rand.nextDouble();
                double chance = itemsChance.getChance() / 100;
                if (roll <= chance) {
                    itemsChanceList.add(itemsChance);
                    break;
                }
            }
        }
        return itemsChanceList;
    }

     */

    public List<ItemsChance> roll(CustomDrops crops) {
        List<ItemsChance> itemsChanceList = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < crops.getRolls(); i++) {
            double totalWeight = 0.0;

            for (ItemsChance itemsChance : crops.getItems()) {
                double chance = itemsChance.getChance() / 100;
                totalWeight += chance;
            }

            double randomValue = rand.nextDouble() * totalWeight;
            double cumulativeWeight = 0.0;

            for (ItemsChance itemsChance : crops.getItems()) {
                double chance = itemsChance.getChance() / 100;
                cumulativeWeight += chance;
                if (randomValue <= cumulativeWeight) {
                    itemsChanceList.add(itemsChance);
                    break;
                }
            }
        }
        return itemsChanceList;
    }


    public HarvesterDrops getCrop(Material material) {
        return cropsHashMap.get(material);
    }
}
