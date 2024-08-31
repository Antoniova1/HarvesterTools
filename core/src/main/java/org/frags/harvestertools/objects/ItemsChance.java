package org.frags.harvestertools.objects;

import org.bukkit.inventory.ItemStack;

public class ItemsChance {

    private final ItemStack item;
    private final double chance;
    private final double price;
    private final double essence;
    private final double experience;

    public ItemsChance(ItemStack item, double chance, double price, double essence, double experience) {
        this.item = item;
        this.chance = chance;
        this.price = price;
        this.essence = essence;
        this.experience = experience;
    }

    public double getPrice() {
        return price;
    }

    public double getEssence() {
        return essence;
    }

    public double getExperience() {
        return experience;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getChance() {
        return chance;
    }
}
