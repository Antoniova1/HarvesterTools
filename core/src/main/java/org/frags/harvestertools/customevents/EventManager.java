package org.frags.harvestertools.customevents;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enums.Tools;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventManager {


    private final HarvesterTools plugin;
    private final FileConfiguration config;

    private HashMap<Tools, Event> eventHashMap = new HashMap<>();

    public EventManager(HarvesterTools plugin) {
        this.plugin = plugin;
        this.config = plugin.eventsFile.getConfig();

        loadEvent();
    }


    public void loadEvent() {
        ConfigurationSection events = config.getConfigurationSection("events");
        for (String key : events.getKeys(false)) {
            //We are in the event
            Tools tools = Tools.getTool(key);
            if (tools == null) {
                Bukkit.getLogger().warning("Key " + key + " is not a valid tool. Please change it in the events.yml file");
                continue;
            }

            ConfigurationSection tool = events.getConfigurationSection(key);

            if (!tool.getBoolean("enabled")) {
                eventHashMap.put(tools, null);
                continue;
            }

            long time = (long) tool.getInt("time") * 60 * 20;

            List<String> startMessage = tool.getStringList("start-message");
            List<String> endMessage = tool.getStringList("end-message");

            ConfigurationSection rewards = tool.getConfigurationSection("rewards");
            HashMap<Integer, Rewards> rewardsMap = new HashMap<>();
            for (String rewardKey : rewards.getKeys(false)) {
                int top;
                try {
                    top = Integer.parseInt(rewardKey);
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Invalid reward key " + rewardKey);
                    continue;
                }

                List<String> reward = rewards.getStringList(rewardKey);

                rewardsMap.put(top, new Rewards(top, reward));
            }


            Event event = new Event(tools, time, startMessage, endMessage, rewardsMap);

            eventHashMap.put(tools, event);
        }
    }

    @Nullable
    public Event getEvent(Tools tool) {
        return eventHashMap.getOrDefault(tool, null);
    }

}
