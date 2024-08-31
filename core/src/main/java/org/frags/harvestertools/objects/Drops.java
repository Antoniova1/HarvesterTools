package org.frags.harvestertools.objects;

import org.bukkit.Material;

public class Drops extends HarvesterDrops {

    private final double price;
    private final double essencePrice;
    private final double experience;

    public Drops(Material material, int requiredLevel, double price, double essencePrice, double experience) {
        super(material, requiredLevel);
        this.price = price;
        this.essencePrice = essencePrice;
        this.experience = experience;
    }

    /*public Crops(double price, double essencePrice, double experience) {
        this.material = material;
        this.requiredLevel = requiredLevel;
        this.price = price;
        this.essencePrice = essencePrice;
        this.experience = experience;
    }
     */

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
