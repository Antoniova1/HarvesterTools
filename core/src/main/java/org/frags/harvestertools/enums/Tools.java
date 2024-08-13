package org.frags.harvestertools.enums;

import org.jetbrains.annotations.Nullable;

public enum Tools {
    hoe, sword, pickaxe, rod, all;

    @Nullable
    public static Tools getTool(String tool) {
        if (tool.equalsIgnoreCase("hoe")) {
            return hoe;
        } else if (tool.equalsIgnoreCase("sword")) {
            return sword;
        } else if (tool.equalsIgnoreCase("pickaxe")) {
            return pickaxe;
        } else if (tool.equalsIgnoreCase("rod")) {
            return rod;
        } else if (tool.equalsIgnoreCase("all")){
            return all;
        } else {
            return null;
        }
    }
}
