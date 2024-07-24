package org.frags.harvestertools.enchants;

import org.bukkit.NamespacedKey;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.MessageManager;

import java.util.List;

import static org.frags.harvestertools.HarvesterTools.getInstance;

public class CustomEnchant {

    private final String name;
    private final int maxLevel;
    private final NamespacedKey key;
    private final double initialPrice;
    private final double increasePrice;
    private final boolean upgradable;
    private final String customName;
    private final int requiredLevel;
    private final int requiredPrestige;

    public CustomEnchant(String name, int maxLevel, double initialPrice, double increasePrice, boolean upgradable, String customName, int requiredLevel, int requiredPrestige, Tools tool) {
        this.name = name;
        this.maxLevel = maxLevel;
        this.key = new NamespacedKey(getInstance(), name);
        this.initialPrice = initialPrice;
        this.increasePrice = increasePrice;
        this.upgradable = upgradable;
        this.customName = MessageManager.miniStringParse(customName);
        this.requiredLevel = requiredLevel;
        this.requiredPrestige = requiredPrestige;
    }
    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public NamespacedKey getNamespacedKey() {
        return key;
    }

    public double getIncreasePrice() {
        return increasePrice;
    }

    public double getInitialPrice() {
        return initialPrice;
    }

    public boolean isUpgradable() {
        return upgradable;
    }

    public String getCustomName() {
        return customName;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getRequiredPrestige() {
        return requiredPrestige;
    }
}
