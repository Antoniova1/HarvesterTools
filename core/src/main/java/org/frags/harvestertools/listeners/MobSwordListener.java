package org.frags.harvestertools.listeners;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.objects.HarvesterMob;
import org.frags.harvestertools.utils.ToolUtils;

public class MobSwordListener implements Listener {

    private final HarvesterTools plugin;

    public MobSwordListener(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        Entity entity = e.getEntity();
        Player player = e.getEntity().getKiller();
        //e.getDrops().clear();

        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (itemStack == null || itemStack.getType() == Material.AIR) return;

        if (!ToolUtils.isTool(itemStack)) return;

        String entityName;
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            boolean isMythicMob = MythicBukkit.inst().getMobManager().isMythicMob(entity);
            if (isMythicMob) {
                ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(entity);
                if (activeMob == null) return;
                MythicMob mob = activeMob.getType();
                entityName = mob.getInternalName();
            } else {
                entityName = entity.getType().name();
            }
        } else {
            entityName = entity.getType().name();
        }

        if (plugin.getMobManager().getMob(entityName) == null)
            return;
        //Mob registered in file

        HarvesterMob mob = plugin.getMobManager().getMob(entityName);

        plugin.getMobUtils().calculateAutoSellDrops(itemStack, player, mob, e.getDrops());

        e.getDrops().clear();

        plugin.getMobUtils().procHaste(player, itemStack);

        plugin.getMobUtils().procStrength(player, itemStack);

        plugin.getMobUtils().calculateBoosters(player, itemStack);

        plugin.getMobUtils().addExperience(player, itemStack);

        plugin.getMobUtils().procCustomEnchants(player, itemStack);

    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        if (!(e.getEntity() instanceof Damageable)) return;
        Entity entity = e.getEntity();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) return;

        if (!ToolUtils.isTool(itemStack)) return;

        String entityName;
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            boolean isMythicMob = MythicBukkit.inst().getMobManager().isMythicMob(entity);
            if (isMythicMob) {
                ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(entity);
                if (activeMob == null) return;
                MythicMob mob = activeMob.getType();
                entityName = mob.getInternalName();
            } else {
                entityName = entity.getType().name();
            }
        } else {
            entityName = entity.getType().name();
        }

        plugin.getMobUtils().manageSharpness(e, itemStack);

        if (plugin.getMobManager().getMob(entityName) == null)
            return;
        //Mob registered in file

        HarvesterMob mob = plugin.getMobManager().getMob(entityName);

        int toolLevel = ToolUtils.getItemLevel(itemStack);

        if (mob.getLevel() > toolLevel) {
            MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("not-enough-level")
                    .replace("%level%", String.valueOf(mob.getLevel())));
            e.setCancelled(true);
            return;
        }

    }
}
