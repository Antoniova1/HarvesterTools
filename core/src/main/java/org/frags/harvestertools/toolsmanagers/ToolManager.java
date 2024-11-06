package org.frags.harvestertools.toolsmanagers;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enchants.EnchantsManager;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.events.ObtainExperienceEvent;
import org.frags.harvestertools.managers.LevelManager;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.objects.Level;
import org.frags.harvestertools.utils.ToolUtils;

public class ToolManager {

    protected final HarvesterTools plugin;

    private final String playerName;
    private Player player;

    private double money = 0D;
    private double essence = 0D;
    private double experience = 0D;

    protected double moneyBooster = 0D;
    protected double essenceBooster = 0D;
    protected double experienceBooster = 0D;

    protected EnchantsManager enchantsManager;

    private boolean isAutoSell;
    private boolean isCollectPeriod;

    private long lastSpeedTime;
    private long lastHasteTime;

    protected final long cooldownTime = 5000;


    private boolean isExperiencePeriod;

    public ToolManager(HarvesterTools plugin, String player) {
        this.plugin = plugin;
        this.playerName = player;
        this.player = Bukkit.getPlayer(player);
        this.enchantsManager = plugin.getEnchantsManager();
    }


    public boolean isInAutoSellPeriod() {
        return isAutoSell;
    }

    public void setAutoSellPeriod(boolean inAutoSellPeriod) {
        isAutoSell = inAutoSellPeriod;
    }

    public boolean isInExperiencePeriod() {
        return isExperiencePeriod;
    }

    public void setExperiencePeriod(boolean inExperiencePeriod) {
        isExperiencePeriod = inExperiencePeriod;
    }

    public boolean isInCollectPeriod() {
        return isCollectPeriod;
    }

    public void setCollectPeriod(boolean inCollectPeriod) {
        isCollectPeriod = inCollectPeriod;
    }

    public Player getPlayer() {
        return player;
    }

    public double getMoney() {
        return money;
    }

    public double getEssence() {
        return essence;
    }

    public double getExperience() {
        return experience;
    }

    public void addMoney(double money) {
        this.money += money;
    }

    public void addEssence(double essence) {
        this.essence += essence;
    }

    public void addExperience(double experience) {
        this.experience += experience;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public void setEssence(double essence) {
        this.essence = essence;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }

    public void addToolExperience(ItemStack itemStack) {
        if (ToolUtils.isMaxLevel(itemStack))
            return;

        if (!isExperiencePeriod) {
            setExperiencePeriod(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                calculateExperienceBooster(itemStack);

                Bukkit.getPluginManager().callEvent(new ObtainExperienceEvent(player, getExperience(), ToolUtils.getTool(itemStack), itemStack));

                setExperience(0);
                setExperiencePeriod(false);
            }, 100L);
        }
    }

    public void procSpeed(ItemStack itemStack) {
        Tools tools = ToolUtils.getTool(itemStack);

        CustomEnchant speed = enchantsManager.getEnchant("speed", tools);

        if (speed == null)
            return;

        if (!enchantsManager.hasEnchantment(itemStack, speed))
            return;

        int level = enchantsManager.getEnchantmentLevel(itemStack, speed);

        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastSpeedTime) < cooldownTime) {
            return;
        }

        PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, 200, level);
        player.addPotionEffect(effect);

        lastSpeedTime = System.currentTimeMillis();
    }

    public void procHaste(ItemStack itemStack) {
        Tools tools = ToolUtils.getTool(itemStack);

        CustomEnchant haste = enchantsManager.getEnchant("haste", tools);
        if (haste == null)
            return;

        if (!enchantsManager.hasEnchantment(itemStack, haste))
            return;

        int level = enchantsManager.getEnchantmentLevel(itemStack, haste);
        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastHasteTime) < cooldownTime)
            return;

        PotionEffect effect = new PotionEffect(PotionEffectType.FAST_DIGGING, 200, level);
        player.addPotionEffect(effect);

        lastHasteTime = System.currentTimeMillis();
    }



    public void calculateBoostersValue(ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        Tools tool = ToolUtils.getTool(itemStack);

        if (tool == null)
            return;

        moneyBooster = 0;
        essenceBooster = 0;

        double moneyPrestigeBooster = container.getOrDefault(ToolUtils.moneyBoostKey, PersistentDataType.DOUBLE, 0D);
        double essencePrestigeBooster = container.getOrDefault(ToolUtils.essenceBoostKey, PersistentDataType.DOUBLE, 0D);

        CustomEnchant moneyBoost = plugin.getEnchantsManager().getEnchant("moneybooster", tool);
        if (moneyBoost != null) {
            if (enchantsManager.hasEnchantment(itemStack, moneyBoost)) {
                int level = enchantsManager.getEnchantmentLevel(itemStack, moneyBoost);

                moneyBooster = (moneyBoost.getBoostPerLevel() * level) + moneyPrestigeBooster;
            } else {
                moneyBooster = 0D + moneyPrestigeBooster;
            }
        }

        CustomEnchant essenceBoost = plugin.getEnchantsManager().getEnchant("essencebooster", tool);
        if (essenceBoost != null) {
            if (enchantsManager.hasEnchantment(itemStack, essenceBoost)) {
                int level = enchantsManager.getEnchantmentLevel(itemStack, essenceBoost);

                essenceBooster = (essenceBoost.getBoostPerLevel() * level) + essencePrestigeBooster;
            } else {
                essenceBooster = 0D + essencePrestigeBooster;
            }
        }


    }

    public void calculateExperienceBooster(ItemStack itemStack) {
        Tools tool = ToolUtils.getTool(itemStack);

        CustomEnchant experienceBoost = plugin.getEnchantsManager().getEnchant("experiencebooster", tool);
        if (experienceBoost != null) {
            if (enchantsManager.hasEnchantment(itemStack, experienceBoost)) {
                int level = enchantsManager.getEnchantmentLevel(itemStack, experienceBoost);

                experienceBooster = experienceBoost.getBoostPerLevel() * level;
            } else {
                experienceBooster = 0D;
            }
        }

        experience = experience + (experience * experienceBooster);

    }

    public void calculateBoostersAdder(ItemStack itemStack) {
        double moneyBoost = moneyBooster;
        double essenceBoost = essenceBooster;

        money = money + (money * moneyBoost);
        essence = essence + (essence * essenceBoost);
    }

}
