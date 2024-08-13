package org.frags.harvestertools.enchants;

public class Result {

    public static final Result EMTPY = new Result(0, 0);

    private int upgrades;
    private double totalCost;

    public Result(int upgrades, double totalCost) {
        this.upgrades = upgrades;
        this.totalCost = totalCost;
    }

    public int getUpgrades() {
        return upgrades;
    }

    public double getTotalCost() {
        return totalCost;
    }
}
