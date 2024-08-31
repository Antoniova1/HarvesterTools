package org.frags.harvestertools.objects;

import org.bukkit.Material;

import java.util.List;

public class CustomDrops extends HarvesterDrops {

    private final int rolls;
    private final List<ItemsChance> items;

    public CustomDrops(Material material, int requiredLevel, int rolls, List<ItemsChance> items) {
        super(material, requiredLevel);
        this.items = items;
        this.rolls = rolls;
    }

    public int getRolls() {
        return rolls;
    }

    public List<ItemsChance> getItems() {
        return items;
    }
}
