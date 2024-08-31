package org.frags.harvestertools.objects;

import java.util.List;

public class CustomMob extends HarvesterMob {

    private final List<ItemsChance> itemsChances;
    private final int rolls;

    public CustomMob(double level,int rolls, List<ItemsChance> itemsChance) {
        super(level);
        this.itemsChances = itemsChance;
        this.rolls = rolls;
    }


    public List<ItemsChance> getItems() {
        return itemsChances;
    }

    public int getRolls() {
        return rolls;
    }
}
