package org.frags.harvestertools.objects;

public class Mob extends HarvesterMob {

    private final double money;
    private final double essence;
    private final double experience;

    public Mob(double level, double money, double essence, double experience) {
        super(level);
        this.money = money;
        this.essence = essence;
        this.experience = experience;
    }


    public double getEssence() {
        return essence;
    }

    public double getExperience() {
        return experience;
    }

    public double getMoney() {
        return money;
    }
}
