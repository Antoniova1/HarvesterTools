package org.frags.harvestertools.toolsmanagers;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.events.ObtainEssenceEvent;
import org.frags.harvestertools.events.ObtainMoneyEvent;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.objects.CustomDrops;
import org.frags.harvestertools.objects.Drops;
import org.frags.harvestertools.objects.HarvesterDrops;
import org.frags.harvestertools.objects.ItemsChance;
import org.frags.harvestertools.utils.RandomSystem;
import org.frags.harvestertools.utils.ToolUtils;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class PickaxeManager extends ToolManager {

    private final ConfigurationSection section;

    public PickaxeManager(HarvesterTools plugin, String player) {
        super(plugin, player);
        this.section = plugin.pickaxeEnchantsFile.getConfig().getConfigurationSection("CustomEnchants");
    }

    public void calculateAutoSellDrops(ItemStack itemStack, HarvesterDrops block, Collection<ItemStack> drops) {

        Player player = getPlayer();

        double initialEssencePrice = 0D;
        double initialMoneySell = 0D;
        double initialXP = 0D;

        if (ToolUtils.isAutoSell(itemStack)) {
            //Is activated

            if (block instanceof Drops drop) {
                for (int i = 0; i < drops.size(); i++) {
                    initialMoneySell += drop.getPrice();
                    initialEssencePrice += drop.getEssencePrice();
                    initialXP += drop.getExperience();
                }
            } else if (block instanceof CustomDrops customDrops) {
                List<ItemsChance> itemsChanceList = plugin.getBlockManager().roll(customDrops);
                if (!itemsChanceList.isEmpty()) {
                    for (ItemsChance item : itemsChanceList) {
                        initialMoneySell += item.getPrice();
                        initialEssencePrice += item.getEssence();
                        initialXP += item.getExperience();
                    }
                }
            }

            addMoney(initialMoneySell);
            addEssence(initialEssencePrice);
            addExperience(initialXP);


            if (!isInCollectPeriod() && !isInAutoSellPeriod()) {
                setAutoSellPeriod(true);
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                calculateBoostersValue(itemStack);
                long autoSellTime = section.getLong("autosell-time") * 20;

                scheduler.runTaskLater(plugin, () -> {

                    calculateBoostersAdder(itemStack);

                    double oldMoney = getMoney();
                    double oldEssence = getEssence();

                    if (plugin.canUseVault) {
                        Bukkit.getPluginManager().callEvent(new ObtainMoneyEvent(player, oldMoney, Tools.pickaxe, itemStack, this));
                    }

                    //plugin.getEssenceManager().addEssence(player, essence);
                    Bukkit.getPluginManager().callEvent(new ObtainEssenceEvent(player, oldEssence, Tools.pickaxe, itemStack, this));

                    double money = getMoney();
                    double essence = getMoney();

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

                    ItemMeta meta = itemStack.getItemMeta();

                    PersistentDataContainer container = meta.getPersistentDataContainer();

                    double toolEssence = container.getOrDefault(ToolUtils.essenceKey, PersistentDataType.DOUBLE, 0.0D);

                    container.set(ToolUtils.essenceKey, PersistentDataType.DOUBLE, toolEssence + essence);

                    itemStack.setItemMeta(meta);

                    setMoney(0);
                    setEssence(0);
                    setAutoSellPeriod(false);

                }, autoSellTime);
            }
        } else {
            if (block instanceof Drops drop) {
                for (ItemStack a : drops) {
                    player.getInventory().addItem(a);
                    initialEssencePrice += drop.getEssencePrice();
                    initialXP += drop.getExperience();
                }
            } else if (block instanceof CustomDrops customDrops) {
                List<ItemsChance> items = plugin.getBlockManager().roll(customDrops);
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

            if (!isInCollectPeriod() && !isInAutoSellPeriod()) {
                setCollectPeriod(true);

                calculateBoostersValue(itemStack);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    calculateBoostersAdder(itemStack);

                    Bukkit.getPluginManager().callEvent(new ObtainEssenceEvent(player, getEssence(), Tools.pickaxe, itemStack, this));

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

    public void procCustomEnchants(ItemStack itemStack) {
        for (CustomEnchant enchant : enchantsManager.getPickaxeEnchants()) {
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
