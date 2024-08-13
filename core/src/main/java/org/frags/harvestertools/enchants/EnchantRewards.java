package org.frags.harvestertools.enchants;

import java.util.HashMap;
import java.util.List;

public class EnchantRewards {

    private HashMap<String, Double> commands = new HashMap<>();

    public EnchantRewards(HashMap<String, Double> commands) {
        this.commands = commands;
    }

    public HashMap<String, Double> getCommands() {
        return commands;
    }
}
