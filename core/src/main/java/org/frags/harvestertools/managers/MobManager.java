package org.frags.harvestertools.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.objects.Mob;

import javax.swing.text.html.parser.Entity;
import java.util.HashMap;

public class MobManager {

    private final HarvesterTools plugin;

    private final HashMap<String, Mob> mobsMap = new HashMap<>();

    private final ConfigurationSection section;

    public MobManager(HarvesterTools plugin) {
        this.plugin = plugin;
        this.section = plugin.mobsFile.getConfig().getConfigurationSection("Mobs");
        loadMobs();
    }

    private void loadMobs() {
        for (String key : section.getKeys(false)) {
            ConfigurationSection mobSection = section.getConfigurationSection(key);
            double money = mobSection.getDouble("money");
            double essence = mobSection.getDouble("essence");
            double experience = mobSection.getDouble("experience");
            int level = mobSection.getInt("level");
            Mob mob = new Mob(money, essence, experience, level);
            mobsMap.put(key.toLowerCase(), mob);
        }
    }

    public Mob getMob(String mobName) {
        return mobsMap.getOrDefault(mobName.toLowerCase(), null);
    }

}
