package org.frags.harvestertools.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.objects.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class BlockManager {

    private final HarvesterTools plugin;

    private final HashMap<String, HarvesterDrops> blocksMap = new HashMap<>();

    private final ConfigurationSection section;

    public BlockManager(HarvesterTools plugin) {
        this.plugin = plugin;
        this.section = plugin.blocksFile.getConfig().getConfigurationSection("blocks");
        loadBlocks();
    }

    private void loadBlocks() {
        if (!plugin.blocksFile.getConfig().getBoolean("enable_custom")) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection blockSection = section.getConfigurationSection(key);
                double money = blockSection.getDouble("money");
                double essence = blockSection.getDouble("essence");
                double experience = blockSection.getDouble("experience");
                int level = blockSection.getInt("level");
                Material material;
                try {
                    material = Material.valueOf(key.toUpperCase());
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Error while creating custom drop for pickaxe");
                    Bukkit.getLogger().warning(key + " is not a valid material");
                    continue;
                }

                Drops drops = new Drops(material, level, money, essence, experience);

                blocksMap.put(key.toLowerCase(), drops);
            }
            return;
        }
        //Manage custom drops

        for (String key : section.getKeys(false)) {
            ConfigurationSection blockSection = section.getConfigurationSection(key);
            Material material;
            try {
                material = Material.valueOf(key.toUpperCase());
            } catch (NullPointerException e) {
                Bukkit.getLogger().warning("Error while creating custom drop for pickaxe");
                Bukkit.getLogger().warning(key + " is not a valid material");
                continue;
            }
            int level = blockSection.getInt("level");
            int rolls = blockSection.getInt("custom_drops.rolls");
            ConfigurationSection dropSection = blockSection.getConfigurationSection("custom_drops.drops");
            List<ItemsChance> itemsChanceList = new ArrayList<>();
            for (String dropKey : dropSection.getKeys(false)) {
                ConfigurationSection drop = dropSection.getConfigurationSection(dropKey);
                Material dropMaterial;
                try {
                    dropMaterial = Material.valueOf(drop.getString("material").toUpperCase());
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Error while creating custom crops");
                    Bukkit.getLogger().warning("Material " + drop.getString("material") + " is not a valid material");
                    continue;
                }

                double chance = drop.getDouble("chance");

                ItemStack itemStack = new ItemStack(dropMaterial);

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

                System.out.println(experience);

                itemsChanceList.add(new ItemsChance(itemStack, chance, price, essence, experience));
            }
            CustomDrops customDrops = new CustomDrops(material, level, rolls, itemsChanceList);
            blocksMap.put(key.toLowerCase(), customDrops);
        }

    }

    public List<ItemsChance> roll(CustomDrops drops) {
        List<ItemsChance> itemsChanceList = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < drops.getRolls(); i++) {
            double totalWeight = 0.0;

            for (ItemsChance itemsChance : drops.getItems()) {
                double chance = itemsChance.getChance() / 100;
                totalWeight += chance;
            }

            double randomValue = rand.nextDouble() * totalWeight;
            double cumulativeWeight = 0.0;

            for (ItemsChance itemsChance : drops.getItems()) {
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

    public HarvesterDrops getBlock(String blockName) {
        return blocksMap.getOrDefault(blockName.toLowerCase(), null);
    }
}
