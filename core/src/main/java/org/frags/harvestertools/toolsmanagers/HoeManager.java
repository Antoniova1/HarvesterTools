package org.frags.harvestertools.toolsmanagers;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Crops;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.CropsManager;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.objects.CustomDrops;
import org.frags.harvestertools.objects.Drops;
import org.frags.harvestertools.objects.HarvesterDrops;
import org.frags.harvestertools.objects.ItemsChance;
import org.frags.harvestertools.utils.RandomSystem;
import org.frags.harvestertools.utils.ToolUtils;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class HoeManager extends ToolManager {


    private boolean seller;
    private boolean isInRush;
    private boolean isInSoulSpeed;


    private double rushBoost;

    private final ConfigurationSection section;

    public HoeManager(HarvesterTools plugin, String player) {
        super(plugin, player);
        section = plugin.hoeEnchantsFile.getConfig().getConfigurationSection("CustomEnchants.autosell");
    }

    public void calculateAutoSellDrops(ItemStack itemStack, Block block) {
        Player player = getPlayer();

        if (!ToolUtils.isTool(itemStack))
            return;

        CropsManager cropsManager = plugin.getCropsManager();
        Material material = block.getType();

        HarvesterDrops crop = cropsManager.getCrop(material);

        double initialEssencePrice = 0D;
        double initialMoneySell = 0D;
        double initialXP = 0D;

        if (ToolUtils.isAutoSell(itemStack)) {
            //Auto sell activated
            if (crop instanceof Drops crops) {
                int size = plugin.getNmsHandler().getDrops(block).size();
                for (int i = 0; i < size; i++) {
                    initialMoneySell += crops.getPrice();
                    initialEssencePrice += crops.getEssencePrice();
                    initialXP += crops.getExperience();
                }
            } else if (crop instanceof CustomDrops crops) {
                List<ItemsChance> itemsChanceList = cropsManager.roll(crops);
                if (!itemsChanceList.isEmpty()) {
                    for (ItemsChance item : itemsChanceList) {
                        initialMoneySell += item.getPrice();
                        initialEssencePrice += item.getEssence();
                        initialXP += item.getExperience();
                    }
                }
            }

            if (!isInRush) {
                addEssence(initialEssencePrice);
                addMoney(initialMoneySell);
            } else {
                addEssence(initialEssencePrice + (initialEssencePrice * getRushBoost()));
                addMoney(initialMoneySell + (initialMoneySell * getRushBoost()));
            }

            addExperience(initialXP);


            if (!isInAutoSellPeriod()) {
                setAutoSellPeriod(true);
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                calculateBoostersValue(itemStack);
                long autoSellTime = 60 * 20;

                scheduler.runTaskLater(plugin, () -> {

                    calculateBoostersAdder(itemStack);

                    double money = getMoney();
                    double essence = getEssence();

                    if (isInSeller()) {
                        money = money *2;
                        essence = essence *2;
                        setSeller(false);
                    }

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

            if (crop instanceof Drops crops) {
                for (ItemStack item : plugin.getNmsHandler().getDrops(block)) {
                    player.getInventory().addItem(item);
                    initialEssencePrice += crops.getEssencePrice();
                    initialXP += crops.getExperience();
                }
            } else if (crop instanceof CustomDrops crops) {
                List<ItemsChance> items = cropsManager.roll(crops);
                if (!items.isEmpty()) {
                    for (ItemsChance item : items) {
                        player.getInventory().addItem(item.getItem());
                        initialEssencePrice += item.getEssence();
                        initialXP += item.getExperience();
                    }
                }
            }

            if (!isInRush) {
                addEssence(initialEssencePrice);
            } else {
                addEssence(initialEssencePrice + (initialEssencePrice * getRushBoost()));
            }

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


    public void procCropReaper(ItemStack itemStack, Block block) {

        CustomEnchant cropReaper = enchantsManager.getEnchant("cropreaper", Tools.hoe);
        if (cropReaper == null)
            return;
        if (!enchantsManager.hasEnchantment(itemStack, cropReaper))
            return;

        RandomSystem randomSystem = new RandomSystem();

        int level = enchantsManager.getEnchantmentLevel(itemStack, cropReaper);
        double chance = cropReaper.getChancePerLevel() * level;

        if (!randomSystem.success(chance, true))
            return;

        CropsManager crops = plugin.getCropsManager();
        HarvesterDrops drop = crops.getCrop(block.getType());

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block blockToCheck = block.getRelative(x, 0, z);

                if (!(blockToCheck.getBlockData() instanceof Ageable ageable))
                    continue;
                if (ageable.getAge() != ageable.getMaximumAge())
                    continue;

                plugin.getNmsHandler().replenishCrop(blockToCheck, plugin);

                List<ItemStack> drops = plugin.getNmsHandler().getDrops(blockToCheck);

                for (int i = 0; i < drops.size(); i++) {
                    if (drop instanceof Drops crop) {
                        addMoney(crop.getPrice());
                        addEssence(crop.getEssencePrice());
                    } else if (drop instanceof CustomDrops customDrops) {
                        for (ItemsChance items : customDrops.getItems()) {
                            if (!isInRushPeriod()) {
                                addMoney(items.getPrice());
                                addEssence(items.getEssence());
                            } else {
                                addMoney(items.getPrice() + (items.getPrice() + rushBoost));
                                addEssence(items.getEssence() + (items.getEssence() + rushBoost));
                            }
                        }
                    }
                }
            }
        }
    }

    public void procRush(ItemStack itemStack) {
        CustomEnchant rush = plugin.getEnchantsManager().getEnchant("rush", Tools.hoe);
        if (rush == null)
            return;
        if (!enchantsManager.hasEnchantment(itemStack, rush))
            return;

        if (isInRushPeriod())
            return;

        int level = enchantsManager.getEnchantmentLevel(itemStack, rush);
        double chance = rush.getChancePerLevel() * level;

        RandomSystem randomSystem = new RandomSystem();

        if (!randomSystem.success(chance, true))
            return;

        rushBoost = level * rush.getBoostPerLevel();

        setInRush(true);

        MessageManager.miniMessageSender(getPlayer(), plugin.hoeEnchantsFile.getConfig().getString("CustomEnchants.rush.message"));

        long time = plugin.hoeEnchantsFile.getConfig().getLong("CustomEnchants.rush.time") * 20;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            setInRush(false);
        }, time);
    }

    public void procSeller(ItemStack itemStack) {
        if (isInSeller())
            return;
        CustomEnchant seller = enchantsManager.getEnchant("seller", Tools.hoe);

        if (seller == null)
            return;
        if (!enchantsManager.hasEnchantment(itemStack, seller))
            return;

        int level = enchantsManager.getEnchantmentLevel(itemStack, seller);
        double chance = level * seller.getChancePerLevel();

        RandomSystem randomSystem = new RandomSystem();

        if (!randomSystem.success(chance, true))
            return;

        setSeller(true);
        MessageManager.miniMessageSender(getPlayer(), plugin.hoeEnchantsFile.getConfig().getString("CustomEnchants.seller.message"));
    }

    public void procSoulSpeed(ItemStack itemStack) {
        CustomEnchant soulSpeed = enchantsManager.getEnchant("soul_speed", Tools.hoe);
        if (soulSpeed == null)
            return;

        if (isInSoulSpeed)
            return;


        if (!enchantsManager.hasEnchantment(itemStack, soulSpeed))
            return;

        Block standingBlock = getPlayer().getLocation().getBlock();
        if (standingBlock.getType() != Material.SOUL_SAND)
            return;

        getPlayer().setWalkSpeed(0.4F);
        isInSoulSpeed = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                Block standingBlockTimer = getPlayer().getLocation().getBlock();

                if (standingBlockTimer.getType() != Material.SOUL_SAND) {
                    getPlayer().setWalkSpeed(0.2F);
                    isInSoulSpeed = false;
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 20L);
    }

    public void procCustomEnchants(ItemStack itemStack) {
        for (CustomEnchant enchant : enchantsManager.getHoeEnchants()) {
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


    public boolean isInSeller() {
        return seller;
    }

    private void setSeller(boolean seller) {
        this.seller = seller;
    }

    private boolean isInRushPeriod() {
        return isInRush;
    }

    private void setInRush(boolean inRush) {
        this.isInRush = inRush;
    }

    public double getRushBoost() {
        return rushBoost;
    }
}
