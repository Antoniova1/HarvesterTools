package org.frags.harvestertools.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.events.ObtainExperienceEvent;
import org.frags.harvestertools.managers.LevelManager;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.objects.Level;
import org.frags.harvestertools.utils.ToolUtils;

public class ObtainExperienceListener implements Listener {

    private final HarvesterTools plugin;

    public ObtainExperienceListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExperienceObtained(ObtainExperienceEvent e) {
        if (e.getTool() == null)
            return;

        ItemStack itemStack = e.getTool();

        Tools tools = ToolUtils.getTool(itemStack);


        double toolLevel = ToolUtils.getItemLevel(itemStack);
        double toolExperience = ToolUtils.getItemExperience(itemStack);

        LevelManager levelManager = plugin.getLevelManager();

        Level level = levelManager.getLevel(tools);

        if (toolLevel >= level.getMaxLevel())
            return;

        double nextLevelXP = level.getStartingXP() + level.getStartingXP() * (toolLevel * level.getIncrementXP());

        if (toolExperience + e.getExperience() >= nextLevelXP) {
            //Next level for the item
            ToolUtils.setLevel(itemStack, 1);
            ToolUtils.setExperience(itemStack, 0D);
            MessageManager.miniMessageSender(e.getPlayer(), plugin.messages.getConfig().getString("level-up-tool").replace("%level%", String.valueOf((int) toolLevel + 1)));
            ToolUtils.updateVariables(itemStack);
            return;
        }

        ToolUtils.setExperience(itemStack, toolExperience + e.getExperience());
        ToolUtils.updateVariables(itemStack);
    }
}
