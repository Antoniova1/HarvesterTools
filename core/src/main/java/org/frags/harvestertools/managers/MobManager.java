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

public class MobManager {

    private final HarvesterTools plugin;

    private final HashMap<String, HarvesterMob> mobsMap = new HashMap<>();

    private final ConfigurationSection section;

    public MobManager(HarvesterTools plugin) {
        this.plugin = plugin;
        this.section = plugin.mobsFile.getConfig().getConfigurationSection("Mobs");
        loadMobs();
    }

    private void loadMobs() {
        if (!plugin.mobsFile.getConfig().getBoolean("enable_custom")) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection mobSection = section.getConfigurationSection(key);
                double money = mobSection.getDouble("money");
                double essence = mobSection.getDouble("essence");
                double experience = mobSection.getDouble("experience");
                int level = mobSection.getInt("level");
                Mob mob = new Mob(level, money, essence, experience);
                mobsMap.put(key.toLowerCase(), mob);
            }
            return;
        }

        //manage custom mobs
        for (String key : section.getKeys(false)) {
            List<ItemsChance> itemsChanceList = new ArrayList<>();
            ConfigurationSection mobSection = section.getConfigurationSection(key);
            int level = mobSection.getInt("level");
            int rolls = mobSection.getInt("custom_drops.rolls");
            ConfigurationSection section1 = mobSection.getConfigurationSection("custom_drops.drops");
            for (String dropKey : section1.getKeys(false)) {
                ConfigurationSection drop = section1.getConfigurationSection(dropKey);

                Material dropMaterial;
                try {
                    dropMaterial = Material.valueOf(drop.getString("material"));
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

                itemsChanceList.add(new ItemsChance(itemStack, chance, price, essence, experience));
            }

            CustomMob mob = new CustomMob(level, rolls, itemsChanceList);
            mobsMap.put(key.toLowerCase(), mob);
        }


    }

    public List<ItemsChance> roll(CustomMob mobs) {
        List<ItemsChance> itemsChanceList = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < mobs.getRolls(); i++) {
            double totalWeight = 0.0;

            for (ItemsChance itemsChance : mobs.getItems()) {
                double chance = itemsChance.getChance() / 100;
                totalWeight += chance;
            }

            double randomValue = rand.nextDouble() * totalWeight;
            double cumulativeWeight = 0.0;

            for (ItemsChance itemsChance : mobs.getItems()) {
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

    public HarvesterMob getMob(String mobName) {
        return mobsMap.getOrDefault(mobName.toLowerCase(), null);
    }

}
