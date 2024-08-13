package org.frags.harvestertools.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.objects.Block;

import java.util.HashMap;

public class BlockManager {

    private final HarvesterTools plugin;

    private final HashMap<String, Block> blocksMap = new HashMap<>();

    private final ConfigurationSection section;

    public BlockManager(HarvesterTools plugin) {
        this.plugin = plugin;
        this.section = plugin.blocksFile.getConfig().getConfigurationSection("blocks");
        loadBlocks();
    }

    private void loadBlocks() {
        for (String key : section.getKeys(false)) {
            ConfigurationSection blockSection = section.getConfigurationSection(key);
            double money = blockSection.getDouble("money");
            double essence = blockSection.getDouble("essence");
            double experience = blockSection.getDouble("experience");
            int level = blockSection.getInt("level");
            Block block = new Block(money, essence, experience, level);
            blocksMap.put(key.toLowerCase(), block);
        }
    }

    public Block getBlock(String blockName) {
        return blocksMap.getOrDefault(blockName.toLowerCase(), null);
    }
}
