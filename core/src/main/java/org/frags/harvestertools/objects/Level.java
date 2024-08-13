package org.frags.harvestertools.objects;

public class Level {

    private int maxLevel;
    private double startingXP;
    private double incrementXP;

    public Level(int maxLevel, double startingXP, double incrementXP) {
        this.maxLevel = maxLevel;
        this.startingXP = startingXP;
        this.incrementXP = incrementXP;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public double getStartingXP() {
        return startingXP;
    }

    public double getIncrementXP() {
        return incrementXP;
    }

}
