package org.frags.harvestertools.utils;

import java.util.Random;

public class RandomSystem {

    private final Random random;

    public RandomSystem() {
        this.random = new Random();
    }

    private boolean success(double chance) {
        if (chance < 0.0 || chance > 1.0) {
            throw new IllegalArgumentException("chance must be between 0.0 and 1.0");
        }
        return random.nextDouble() < chance;
    }

    public boolean success(double chance, boolean divide) {
        double finalChance = chance;
        if (divide) {
            finalChance = finalChance / 100;
        }
        return success(finalChance);
    }


}
