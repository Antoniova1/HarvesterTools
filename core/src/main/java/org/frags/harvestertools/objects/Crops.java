package org.frags.harvestertools.objects;

import org.bukkit.Material;

public class Crops {


    private final Material material;
    private final int requiredLevel;
    private final double price;
    private final double essencePrice;
    private final  double experience;

    public Crops(Material material, int requiredLevel, double price, double essencePrice, double experience) {
        this.material = material;
        this.requiredLevel = requiredLevel;
        this.price = price;
        this.essencePrice = essencePrice;
        this.experience = experience;
    }

    public Material getMaterial() {
        return material;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public double getPrice() {
        return price;
    }

    public double getEssencePrice() {
        return essencePrice;
    }

    public double getExperience() {
        return experience;
    }
}
