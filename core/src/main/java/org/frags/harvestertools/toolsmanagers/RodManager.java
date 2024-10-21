package org.frags.harvestertools.toolsmanagers;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.utils.RandomSystem;
import org.frags.harvestertools.utils.ToolUtils;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class RodManager extends ToolManager {

    private final ConfigurationSection section;

    private final double moneyPrice;
    private final double essencePrice;
    private final double experience;

    public RodManager(HarvesterTools plugin, String player) {
        super(plugin, player);
        section = plugin.rodEnchantsFile.getConfig().getConfigurationSection("CustomEnchants");
        moneyPrice = section.getDouble("autosell.price-per-fish");
        essencePrice = section.getDouble("autosell.essence-per-fish");
        experience = section.getDouble("autosell.experience-per-fish");
    }

    public void fastRod(CustomEnchant fastRod, PlayerFishEvent e, ItemStack itemStack) {
        if (!enchantsManager.hasEnchantment(itemStack, fastRod))
            return;
        //Player has enchant
        int level = enchantsManager.getEnchantmentLevel(itemStack, fastRod);
        double boost = (fastRod.getBoostPerLevel() * level);

        double calculatedBoost = 1 / (1 + (boost / 100));

        int minWaitTime = e.getHook().getMinWaitTime();

        int reducedMinTime = (int) (minWaitTime * calculatedBoost);

        e.getHook().setMinWaitTime(reducedMinTime);
    }

    public void calculateAutoSellDrops(ItemStack itemStack, ItemStack drop) {
        Player player = getPlayer();

        if (!ToolUtils.isTool(itemStack))
            return;

        double initialEssencePrice = 0D;
        double initialMoneySell = 0D;
        double initialXP = 0D;

        if (ToolUtils.isAutoSell(itemStack)) {
            initialMoneySell += moneyPrice;
            initialEssencePrice += essencePrice;
            initialXP += experience;

            addMoney(initialMoneySell);
            addEssence(initialEssencePrice);
            addExperience(initialXP);

            if (!isInCollectPeriod() && !isInAutoSellPeriod()) {
                setAutoSellPeriod(true);
                long autoSellTime = 60 * 20;

                calculateBoostersValue(itemStack);

                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                scheduler.runTaskLater(plugin, () -> {

                    calculateBoostersAdder(itemStack);

                    double money = getMoney();
                    double essence = getEssence();

                    ConfigurationSection section = plugin.rodEnchantsFile.getConfig().getConfigurationSection("CustomEnchants.autosell");
                    ConfigurationSection actionBar = section.getConfigurationSection("actionbar");
                    if (actionBar.getBoolean("enabled")) {
                        String message = MessageManager.miniStringParse(actionBar.getString("message"))
                                .replace("%money%", Utils.formatNumber(BigDecimal.valueOf(money)))
                                .replace("%essence%", Utils.formatNumber(BigDecimal.valueOf(essence)));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                    }
                    ConfigurationSection title = section.getConfigurationSection("title");
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
                    List<String> message = section.getStringList("message");
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
                        //Give money
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
                }, autoSellTime);
            }
        } else {
            player.getInventory().addItem(drop);
            initialEssencePrice += essencePrice;
            initialXP += experience;

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

    public void procTsunami(ItemStack itemStack) {
        CustomEnchant tsunami = enchantsManager.getEnchant("tsunami", Tools.rod);
        if (tsunami == null)
            return;
        if (!enchantsManager.hasEnchantment(itemStack, tsunami))
            return;

        RandomSystem randomSystem = new RandomSystem();

        int level = enchantsManager.getEnchantmentLevel(itemStack, tsunami);
        double chance = tsunami.getChancePerLevel() * level;

        if (!randomSystem.success(chance, true))
            return;

        double money = essencePrice * 5 + (moneyBooster * (essencePrice * 5));
        double essence = essencePrice * 5 + (essenceBooster * (essencePrice * 5));

        addMoney(money);
        addEssence(essence);

        ConfigurationSection section = plugin.rodEnchantsFile.getConfig().getConfigurationSection("CustomEnchants.tsunami");
        ConfigurationSection actionBar = section.getConfigurationSection("actionbar");
        if (actionBar.getBoolean("enabled")) {
            String message = MessageManager.miniStringParse(actionBar.getString("message"))
                    .replace("%money%", Utils.formatNumber(BigDecimal.valueOf(money)))
                    .replace("%essence%", Utils.formatNumber(BigDecimal.valueOf(essence)));
            getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        }
        ConfigurationSection title = section.getConfigurationSection("title");
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
            getPlayer().sendTitle(titleMessage, subtitle, fadeIn, time, fadeOut);
        }
    }


    public void procCustomEnchants(ItemStack itemStack) {
        for (CustomEnchant enchant : enchantsManager.getRodEnchants()) {
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
