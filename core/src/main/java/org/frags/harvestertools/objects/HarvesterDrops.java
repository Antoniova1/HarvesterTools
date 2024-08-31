package org.frags.harvestertools.objects;

import org.bukkit.Material;

public class HarvesterDrops {

    private final Material material;
    private final int requiredLevel;

    public HarvesterDrops(Material material, int requiredLevel) {
        this.material = material;
        this.requiredLevel = requiredLevel;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public Material getMaterial() {
        return material;
    }

}
