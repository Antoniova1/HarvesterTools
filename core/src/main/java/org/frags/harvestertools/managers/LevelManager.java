package org.frags.harvestertools.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.objects.Level;

import java.util.HashMap;

public class LevelManager {

    private final HashMap<Tools, Level> levelHashMap = new HashMap<>();

    private final HarvesterTools plugin;

    private final ConfigurationSection section;

    public LevelManager(HarvesterTools plugin) {
        this.plugin = plugin;
        this.section = plugin.getConfig().getConfigurationSection("levels");
        loadLevels();
    }

    private void loadLevels() {
        for (String key : section.getKeys(false)) {
            Tools tool = Tools.getTool(key);
            ConfigurationSection toolSection = section.getConfigurationSection(key);
            int maxLevel = toolSection.getInt("max-level");
            double startingXP = toolSection.getDouble("starting-xp");
            double incrementXP = toolSection.getDouble("increment-xp");

            Level level = new Level(maxLevel, startingXP, incrementXP);
            levelHashMap.put(tool, level);
        }
    }

    public Level getLevel(Tools tool) {
        return levelHashMap.get(tool);
    }
}
