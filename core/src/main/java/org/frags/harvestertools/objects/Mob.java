package org.frags.harvestertools.objects;

public class Mob {

    private final double money;
    private final double experience;
    private final double essence;
    private final double level;

    public Mob(double money, double experience, double essence, double level) {
        this.money = money;
        this.experience = experience;
        this.essence = essence;
        this.level = level;
    }

    public double getExperience() {
        return experience;
    }

    public double getEssence() {
        return essence;
    }

    public double getLevel() {
        return level;
    }

    public double getMoney() {
        return money;
    }
}
