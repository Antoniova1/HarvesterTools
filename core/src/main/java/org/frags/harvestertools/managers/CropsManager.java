package org.frags.harvestertools.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.objects.Crops;

import java.util.HashMap;

public class CropsManager {

    private final HarvesterTools plugin;

    public CropsManager(HarvesterTools plugin) {
        this.plugin = plugin;
        loadCrops();
    }

    private HashMap<Material, Crops> cropsHashMap = new HashMap<>();

    public void loadCrops() {
        cropsHashMap.clear();
        ConfigurationSection cropsFile = plugin.cropsFile.getConfig().getConfigurationSection("crops");
        for (String key : cropsFile.getKeys(false)) {
            ConfigurationSection crop = cropsFile.getConfigurationSection(key);
            Material material;
            try {
                material = Material.valueOf(key);
            } catch (NullPointerException e) {
                Bukkit.getLogger().warning("Material " + key + " is not a valid material");
                Bukkit.getLogger().warning("Why are you changing keys in crops.yml?");
                continue;
            }
            int level = crop.getInt("level");
            double price = crop.getDouble("price");
            double essencePrice = crop.getDouble("essence-price");
            double experience = crop.getDouble("experience");
            Crops crops = new Crops(material, level, price, essencePrice, experience);
            cropsHashMap.put(material, crops);
        }
    }

    public Crops getCrop(Material material) {
        return cropsHashMap.get(material);
    }
}
