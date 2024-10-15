package org.frags.harvestertools.toolsmanagers;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.objects.CustomMob;
import org.frags.harvestertools.objects.HarvesterMob;
import org.frags.harvestertools.objects.ItemsChance;
import org.frags.harvestertools.objects.Mob;
import org.frags.harvestertools.utils.RandomSystem;
import org.frags.harvestertools.utils.ToolUtils;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class SwordManager extends ToolManager{

    private final ConfigurationSection section;

    private long lastStrenghtTime;

    public SwordManager(HarvesterTools plugin, String player) {
        super(plugin, player);
        section = plugin.swordEnchantsFile.getConfig().getConfigurationSection("CustomEnchants");
    }

    public void calculateAutoSellDrops(ItemStack itemStack, HarvesterMob mobs, List<ItemStack> drops) {
        Player player = getPlayer();

        if (!ToolUtils.isTool(itemStack))
            return;

        double initialEssencePrice = 0D;
        double initialMoneySell = 0D;
        double initialXP = 0D;

        if (ToolUtils.isAutoSell(itemStack)) {
            //Autosell activated

            if (mobs instanceof Mob mob) {
                for (int i = 0; i < drops.size(); i++) {
                    initialMoneySell += mob.getMoney();
                    initialEssencePrice += mob.getEssence();
                    initialXP += mob.getExperience();
                }
            } else if (mobs instanceof CustomMob mob) {
                List<ItemsChance> itemsChanceList = plugin.getMobManager().roll(mob);
                if (!itemsChanceList.isEmpty()) {
                    for (ItemsChance item : itemsChanceList) {
                        initialMoneySell += item.getPrice();
                        initialEssencePrice += item.getEssence();
                        initialXP += item.getExperience();
                    }
                }
            }

            addEssence(initialEssencePrice);
            addMoney(initialMoneySell);
            addExperience(initialXP);


            if (!isInAutoSellPeriod()) {
                setAutoSellPeriod(true);
                BukkitScheduler scheduler = plugin.getServer().getScheduler();
                calculateBoostersValue(itemStack);
                long autosellTime = 60*20;
                scheduler.runTaskLater(plugin, () -> {

                    calculateBoostersAdder(itemStack);

                    double money = getMoney();
                    double essence = getEssence();
                    ConfigurationSection actionBar = section.getConfigurationSection("autosell.actionbar");
                    if (actionBar.getBoolean("enabled")) {
                        String message = MessageManager.miniStringParse(actionBar.getString("message"))
                                .replace("%money%", Utils.formatNumber(BigDecimal.valueOf(money)))
                                .replace("%essence%", Utils.formatNumber(BigDecimal.valueOf(essence)));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                    }
                    ConfigurationSection title = section.getConfigurationSection("autosell.title");
                    if (title.getBoolean("enabled")) {
                        String titleMessage = MessageManager.miniStringParse(title.getString("title"))
                                .replace("%money%", Utils.formatNumber(BigDecimal.valueOf(money)))
                                .replace("%essence%", Utils.formatNumber(BigDecimal.valueOf(essence)));
                        String subtitle = MessageManager.miniStringParse(title.getString("subtitle"))
                                .replace("%money%", Utils.formatNumber(BigDecimal.valueOf(money)))
                                .replace("%essence%", Utils.formatNumber(BigDecimal.valueOf(essence)));
                        int fadeIn = title.getInt("fadeIn");
                        int fadeOut = title.getInt("fadeOut");
                        int time = title.getInt("time");
                        player.sendTitle(titleMessage, subtitle, fadeIn, time, fadeOut);
                    }

                    List<String> message = section.getStringList("autosell.message");
                    if (!message.isEmpty()) {
                        double moneyBoost = moneyBooster;
                        double essenceBoost = essenceBooster;
                        for (String line : message) {
                            String formattedLine = MessageManager.miniStringParse(line)
                                    .replace("%money%", Utils.formatNumber(BigDecimal.valueOf(money)))
                                    .replace("%essence%", Utils.formatNumber(BigDecimal.valueOf(essence)))
                                    .replace("%money_boost%", String.format("%.2f", moneyBoost))
                                    .replace("%essence_boost%", String.format("%.2f", essenceBoost));
                            player.sendMessage(formattedLine);
                        }
                    }

                    if (plugin.canUseVault) {
                        plugin.getEcon().depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), money);
                    }

                    plugin.getEssenceManager().addEssence(player, essence);

                    ItemMeta meta = itemStack.getItemMeta();

                    PersistentDataContainer container = meta.getPersistentDataContainer();

                    double toolEssence = container.getOrDefault(ToolUtils.essenceKey, PersistentDataType.DOUBLE, 0D);


                    container.set(ToolUtils.essenceKey, PersistentDataType.DOUBLE, toolEssence + essence);

                    itemStack.setItemMeta(meta);


                    setMoney(0);
                    setEssence(0);
                    setAutoSellPeriod(false);
                }, autosellTime);
            }
        } else {
            if (mobs instanceof Mob mob) {
                for (ItemStack drop : drops) {
                    player.getInventory().addItem(drop);
                    initialEssencePrice += mob.getEssence();
                    initialXP += mob.getExperience();
                }
            } else if (mobs instanceof CustomMob mob) {
                List<ItemsChance> items = plugin.getMobManager().roll(mob);
                if (!items.isEmpty()) {
                    for (ItemsChance item : items) {
                        player.getInventory().addItem(item.getItem());
                        initialEssencePrice += item.getEssence();
                        initialXP += item.getExperience();
                    }
                }
            }


            addEssence(initialEssencePrice);

            addExperience(initialXP);

            if (!isInCollectPeriod() || !isInAutoSellPeriod()) {
                setCollectPeriod(true);

                calculateBoostersValue(itemStack);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    calculateBoostersAdder(itemStack);

                    plugin.getEssenceManager().addEssence(player, getEssence());

                    ItemMeta meta = itemStack.getItemMeta();

                    PersistentDataContainer container = meta.getPersistentDataContainer();

                    double essence = container.getOrDefault(ToolUtils.essenceKey, PersistentDataType.DOUBLE, 0D);

                    container.set(ToolUtils.essenceKey, PersistentDataType.DOUBLE, essence + getEssence());

                    setEssence(0);
                    setCollectPeriod(false);
                }, 100);
            }
        }
    }


    public void manageSharpness(EntityDamageEvent e, ItemStack itemStack) {
        CustomEnchant sharpness = enchantsManager.getEnchant("sharpness", Tools.sword);
        if (sharpness != null) {
            if (enchantsManager.hasEnchantment(itemStack, sharpness)) {
                int level = enchantsManager.getEnchantmentLevel(itemStack, sharpness);
                double boost = sharpness.getBoostPerLevel() * level;

                double calculatedBoost = (1 + (boost / 100));

                e.setDamage(e.getDamage() * calculatedBoost);
            }
        }
    }

    public void procStrength(ItemStack itemStack) {
        CustomEnchant strength = enchantsManager.getEnchant("strength", Tools.sword);
        if (strength == null)
            return;

        if (!enchantsManager.hasEnchantment(itemStack, strength))
            return;

        int level = enchantsManager.getEnchantmentLevel(itemStack, strength);
        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastStrenghtTime) < cooldownTime)
            return;

        PotionEffect effect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, level);
        getPlayer().addPotionEffect(effect);

        lastStrenghtTime = currentTime;
    }


    public void procCustomEnchants(ItemStack itemStack) {
        for (CustomEnchant enchant : enchantsManager.getSwordEnchants()) {
            if (enchant == null)
                continue;
            if (!enchantsManager.hasEnchantment(itemStack, enchant))
                continue;
            //Player has enchant

            int level = enchantsManager.getEnchantmentLevel(itemStack, enchant);
            double chance = enchant.getChancePerLevel() * level;

            RandomSystem randomSystem = new RandomSystem();

            if (!randomSystem.success(chance, true))
                continue;
            //Command has been activated.

            HashMap<String, Double> commands = enchant.getCommands().getCommands();

            for (String command : commands.keySet()) {
                double commandChance = commands.get(command);
                if (!randomSystem.success(commandChance, true))
                    continue;
                //Give command
                String finalCommand = command.replace("%player%", getPlayer().getName());
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    finalCommand = PlaceholderAPI.setPlaceholders(getPlayer(), finalCommand);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            }
        }
    }

}
