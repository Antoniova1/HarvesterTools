package org.frags.harvestertools.utils;

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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enchants.EnchantsManager;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.CropsManager;
import org.frags.harvestertools.managers.LevelManager;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.objects.*;

import java.math.BigDecimal;
import java.util.*;

public class CropBreakUtils {

    private final HarvesterTools plugin;

    private final Set<Player> experienceKey = new HashSet<>();
    private final Set<Player> sellerKey = new HashSet<>();
    private final Set<Player> collectKey = new HashSet<>();
    private final Set<Player> autoSellKey = new HashSet<>();
    private final HashMap<Player, Double> moneyMap = new HashMap<>();
    private final HashMap<Player, Double> essenceMap = new HashMap<>();
    private final HashMap<Player, Double> experienceMap = new HashMap<>();
    private final HashMap<Player, Double> rushMap = new HashMap<>();
    private final HashMap<Player, Double> moneyBoostMap = new HashMap<>();
    private final HashMap<Player, Double> essenceBoostMap = new HashMap<>();
    private final HashMap<UUID, Long> lastSpeedTime = new HashMap<>();
    private final HashMap<UUID, Long> lastHasteTime = new HashMap<>();
    private final Set<Player> variableKey = new HashSet<>();

    private final long cooldownTime = 5000;

    private final EnchantsManager enchantsManager;

    private double initialMoney = 0D;
    private double initialEssence = 0D;
    private double initialExperience = 0D;

    public CropBreakUtils(HarvesterTools plugin) {
        this.plugin = plugin;
        this.enchantsManager = plugin.getEnchantsManager();
    }

    private void getInitialPrices(double money, double essence, double experience) {
        this.initialMoney = money;
        this.initialEssence = essence;
        this.initialExperience = experience;
    }


    public void calculateAutoSellDrops(ItemStack itemStack, Player player, Block block) {

        moneyMap.putIfAbsent(player, 0D);
        essenceMap.putIfAbsent(player, 0D);
        experienceMap.putIfAbsent(player, 0D);

        CropsManager cropsManager = plugin.getCropsManager();
        Material material = block.getType();

        HarvesterDrops crop = cropsManager.getCrop(material);

        double initialEssencePrice = 0D;
        double initialMoneySell = 0D;
        double initialXP = 0D;
        //Has the enchant -
        //See the mode:


        if (ToolUtils.isAutoSell(itemStack)) {
            //Is activated
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

            getInitialPrices(initialMoneySell, initialEssencePrice, initialXP);

            addAmountToMaps(player, initialMoneySell, initialEssencePrice, initialXP);

            long autoSellTime = plugin.hoeEnchantsFile.getConfig().getLong("CustomEnchants.autosell.autosell-time") * 20;


            if (!autoSellKey.contains(player)) {
                autoSellKey.add(player);
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                scheduler.runTaskLater(plugin, () -> {
                    double moneyL = moneyMap.get(player);
                    double essenceL = essenceMap.get(player);
                    if (sellerKey.contains(player)) {
                        moneyL = moneyL * 2;
                        essenceL = essenceL * 2;
                        sellerKey.remove(player);
                    }
                    ConfigurationSection section = plugin.hoeEnchantsFile.getConfig().getConfigurationSection("CustomEnchants.autosell");
                    ConfigurationSection actionBar = section.getConfigurationSection("actionbar");
                    if (actionBar.getBoolean("enabled")) {
                        String message = MessageManager.miniStringParse(actionBar.getString("message"))
                                .replace("%money%", Utils.formatNumber(BigDecimal.valueOf(moneyL)))
                                .replace("%essence%", Utils.formatNumber(BigDecimal.valueOf(essenceL)));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                    }
                    ConfigurationSection title = section.getConfigurationSection("title");
                    if (title.getBoolean("enabled")) {
                        String titleMessage = MessageManager.miniStringParse(title.getString("title"))
                                .replace("%money%", Utils.formatNumber(BigDecimal.valueOf(moneyL)))
                                .replace("%essence%", Utils.formatNumber(BigDecimal.valueOf(essenceL)));
                        String subtitle = MessageManager.miniStringParse(title.getString("subtitle"))
                                .replace("%money%", Utils.formatNumber(BigDecimal.valueOf(moneyL)))
                                .replace("%essence%", Utils.formatNumber(BigDecimal.valueOf(essenceL)));
                        int fadeIn = title.getInt("fadeIn");
                        int fadeOut = title.getInt("fadeOut");
                        int time = title.getInt("time");
                        player.sendTitle(titleMessage, subtitle, fadeIn, time, fadeOut);
                    }
                    List<String> message = section.getStringList("message");
                    if (!message.isEmpty()) {
                        double moneyBoost = moneyBoostMap.get(player);
                        double essenceBoost = essenceBoostMap.get(player);
                        for (String line : message) {
                            String formattedLine = MessageManager.miniStringParse(line)
                                    .replace("%money%", Utils.formatNumber(BigDecimal.valueOf(moneyL)))
                                    .replace("%essence%", Utils.formatNumber(BigDecimal.valueOf(essenceL)))
                                    .replace("%money_boost%", String.valueOf(moneyBoost))
                                    .replace("%essence_boost%", String.valueOf(essenceBoost));
                            player.sendMessage(formattedLine);
                        }
                    }
                    moneyBoostMap.remove(player);
                    essenceBoostMap.remove(player);

                    if (plugin.canUseVault) {
                        //Give money
                        plugin.getEcon().depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), moneyL);
                    }

                    plugin.getEssenceManager().addEssence(player, essenceL);

                    ItemMeta meta = itemStack.getItemMeta();

                    PersistentDataContainer container = meta.getPersistentDataContainer();

                    double essence = container.getOrDefault(ToolUtils.essenceKey, PersistentDataType.DOUBLE, 0.0D);

                    container.set(ToolUtils.essenceKey, PersistentDataType.DOUBLE, essence + essenceL);

                    itemStack.setItemMeta(meta);
                    moneyMap.remove(player);
                    essenceMap.remove(player);
                    autoSellKey.remove(player);
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



            getInitialPrices(initialMoneySell, initialEssencePrice, initialXP);

            addAmountToMaps(player, 0, initialEssencePrice, initialXP);
            if (!collectKey.contains(player)) {
                collectKey.add(player);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    collectKey.remove(player);
                    plugin.getEssenceManager().addEssence(player, essenceMap.get(player));
                    ItemMeta meta = itemStack.getItemMeta();

                    PersistentDataContainer container = meta.getPersistentDataContainer();

                    double essence = container.getOrDefault(ToolUtils.essenceKey, PersistentDataType.DOUBLE, 0.0D);

                    container.set(ToolUtils.essenceKey, PersistentDataType.DOUBLE, essence + essenceMap.get(player));

                    itemStack.setItemMeta(meta);
                    essenceMap.remove(player);
                }, 5 * 20);
            }
        }

        if (!variableKey.contains(player)) {
            variableKey.add(player);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                ToolUtils.updateVariables(itemStack);
                variableKey.remove(player);
            }, 120L);
        }

    }

    //This need to be run last
    public void calculateBoosters(Player player, ItemStack itemStack) {
        double moneyToAdd = 0D;
        double essenceToAdd = 0D;
        double experienceToAdd = 0D;

        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        double moneyBooster = 0D;
        double essenceBooster = 0D;

        double moneyPrestigeBooster = container.getOrDefault(ToolUtils.moneyBoostKey, PersistentDataType.DOUBLE, 0D);
        double essencePrestigeBooster = container.getOrDefault(ToolUtils.essenceBoostKey, PersistentDataType.DOUBLE, 0D);
        CustomEnchant moneyBoost = plugin.getEnchantsManager().getEnchant("moneybooster", Tools.hoe);
        if (moneyBoost != null) {
            if (enchantsManager.hasEnchantment(itemStack, moneyBoost)) {
                int level = enchantsManager.getEnchantmentLevel(itemStack, moneyBoost);
                double boost = moneyBoost.getBoostPerLevel() * level;

                moneyToAdd = boost * initialMoney;
                moneyBooster = boost + moneyPrestigeBooster;

                if (rushMap.containsKey(player)) {
                    double rushBoost = rushMap.get(player);
                    moneyToAdd = moneyToAdd + (moneyToAdd * rushBoost);
                }
            }
        }


        CustomEnchant essenceBoost = plugin.getEnchantsManager().getEnchant("essencebooster", Tools.hoe);
        if (essenceBoost != null) {
            if (enchantsManager.hasEnchantment(itemStack, moneyBoost)) {
                int level = enchantsManager.getEnchantmentLevel(itemStack, moneyBoost);
                double boost = essenceBoost.getBoostPerLevel() * level;

                essenceToAdd = boost * initialEssence;
                essenceBooster = boost + essencePrestigeBooster;

                if (rushMap.containsKey(player)) {
                    double rushBoost = rushMap.get(player);
                    essenceToAdd = essenceToAdd + (essenceToAdd * rushBoost);
                }
            }
        }

        CustomEnchant experienceBoost = plugin.getEnchantsManager().getEnchant("experiencebooster", Tools.hoe);
        if (experienceBoost != null) {
            if (enchantsManager.hasEnchantment(itemStack, experienceBoost)) {
                int level = enchantsManager.getEnchantmentLevel(itemStack, experienceBoost);
                double boost = experienceBoost.getBoostPerLevel() * level;

                experienceToAdd = boost * initialExperience;

                if (rushMap.containsKey(player)) {
                    double rushBoost = rushMap.get(player);
                    experienceToAdd = experienceToAdd + (experienceToAdd * rushBoost);
                }
            }
        }

        if (!moneyBoostMap.containsKey(player)) {
            moneyBoostMap.put(player, moneyBooster);
            essenceBoostMap.put(player, essenceBooster);
        }

        addAmountToMaps(player,
                moneyToAdd + (moneyToAdd * moneyPrestigeBooster),
                essenceToAdd + (essenceToAdd * essencePrestigeBooster),
                experienceToAdd);
    }

    public void addExperience(Player player, ItemStack itemStack) {
        if (ToolUtils.isMaxLevel(itemStack))
            return;
        if (!experienceKey.contains(player)) {

            double experience = experienceMap.getOrDefault(player, 0D);
            experienceKey.add(player);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                experienceKey.remove(player);
            }, 100L);

            Tools tools = ToolUtils.getTool(itemStack);

            double toolLevel = ToolUtils.getItemLevel(itemStack);
            double toolExperience = ToolUtils.getItemExperience(itemStack);

            LevelManager levelManager = plugin.getLevelManager();

            Level level = levelManager.getLevel(tools);

            //starting-xp + starting-xp * (level * increment-xp)

            double nextLevelXP = level.getStartingXP() + level.getStartingXP() * (toolLevel * level.getIncrementXP());

            if (toolExperience + experience >= nextLevelXP) {
                //Item goes to next level.
                ToolUtils.setLevel(itemStack, 1);
                ToolUtils.setExperience(itemStack, 0D);
                MessageManager.miniMessageSender(player, plugin.messages.getConfig().getString("level-up-tool").replace("%level%", String.valueOf((int) toolLevel + 1)));
                ToolUtils.updateVariables(itemStack);
                return;
            }

            //Adds experience
            ToolUtils.setExperience(itemStack, toolExperience + experience);
        }
    }

    public void procCropReaper(Player player, ItemStack itemStack, Block block) {
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
        double essence = 0D;
        double money = 0D;
        //Enchant has been activated
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block blockToCheck = block.getRelative(x, 0, z);

                if (!(blockToCheck.getBlockData() instanceof Ageable))
                    continue;

                plugin.getNmsHandler().replenishCrop(blockToCheck, plugin);

                List<ItemStack> drops = plugin.getNmsHandler().getDrops(blockToCheck);

                for (int i = 0; i < drops.size(); i++) {
                    if (drop instanceof Drops crop) {
                        essence += crop.getEssencePrice();
                        money += crop.getPrice();
                    } else if (drop instanceof CustomDrops crop) {
                        for (ItemsChance items : crop.getItems()) {
                            essence += items.getEssence();
                            money += items.getPrice();
                        }
                    }
                }
            }
        }

        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        double moneyPrestigeBooster = container.getOrDefault(ToolUtils.moneyBoostKey, PersistentDataType.DOUBLE, 0D);
        double essencePrestigeBooster = container.getOrDefault(ToolUtils.essenceBoostKey, PersistentDataType.DOUBLE, 0D);
        CustomEnchant moneyBooster = enchantsManager.getEnchant("moneybooster", Tools.hoe);

        if (moneyBooster != null) {
            if (enchantsManager.hasEnchantment(itemStack, moneyBooster)) {
                double boost = moneyBooster.getBoostPerLevel() * level;

                money = money + (boost * initialMoney);

                if (rushMap.containsKey(player)) {
                    double rushBoost = rushMap.get(player);
                    money = money + (money * rushBoost);
                }
            }
        }

        CustomEnchant essenceBooster = enchantsManager.getEnchant("essencebooster", Tools.hoe);
        if (essenceBooster != null) {
            if (enchantsManager.hasEnchantment(itemStack, essenceBooster)) {
                double boost = essenceBooster.getBoostPerLevel() * level;

                essence = essence + (boost * initialEssence);

                if (rushMap.containsKey(player)) {
                    double rushBoost = rushMap.get(player);
                    essence = essence + (essence * rushBoost);
                }
            }
        }

        money = money + (money * moneyPrestigeBooster);
        essence = essence + (essence * essencePrestigeBooster);

        ConfigurationSection section = plugin.hoeEnchantsFile.getConfig().getConfigurationSection("CustomEnchants.cropreaper");
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

        if (plugin.canUseVault) {
            //Give money
            plugin.getEcon().depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), money);
        }

        plugin.getEssenceManager().addEssence(player, essence);
    }

    public void procRush(Player player, ItemStack itemStack) {
        CustomEnchant rush = plugin.getEnchantsManager().getEnchant("rush", Tools.hoe);
        if (rush == null)
            return;
        if (!enchantsManager.hasEnchantment(itemStack, rush))
            return;

        if (rushMap.containsKey(player))
            return;

        int level = enchantsManager.getEnchantmentLevel(itemStack, rush);
        double chance = level * rush.getChancePerLevel();

        RandomSystem randomSystem = new RandomSystem();

        if (!randomSystem.success(chance, true))
            return;

        double boost = level * rush.getBoostPerLevel();

        rushMap.putIfAbsent(player, boost);
        MessageManager.miniMessageSender(player, plugin.hoeEnchantsFile.getConfig().getString("CustomEnchants.rush.message"));

        long time = plugin.hoeEnchantsFile.getConfig().getLong("CustomEnchants.rush.time") * 20;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            rushMap.remove(player);
        }, time);
    }

    public void procSeller(Player player, ItemStack itemStack) {
        if (sellerKey.contains(player))
            return;
        CustomEnchant seller = enchantsManager.getEnchant("seller", Tools.hoe);
        if (seller == null)
            return;
        if (!enchantsManager.hasEnchantment(itemStack, seller))
            return;

        int level = enchantsManager.getEnchantmentLevel(itemStack, seller);
        double chance = level * seller.getChancePerLevel();

        RandomSystem randomSystem = new RandomSystem();


        if (randomSystem.success(chance, true)) {
            sellerKey.add(player);
            MessageManager.miniMessageSender(player, plugin.hoeEnchantsFile.getConfig().getString("CustomEnchants.seller.message"));
        }
    }

    public void procSpeed(Player player, ItemStack itemStack) {
        UUID playerUUID = player.getUniqueId();

        CustomEnchant speed = enchantsManager.getEnchant("speed", Tools.hoe);
        if (speed == null)
            return;

        if (!enchantsManager.hasEnchantment(itemStack, speed))
            return;

        int level = enchantsManager.getEnchantmentLevel(itemStack, speed);

        long currentTime = System.currentTimeMillis();

        if (lastSpeedTime.containsKey(playerUUID)) {
            long lastTime = lastSpeedTime.get(playerUUID);

            if ((currentTime - lastTime) < cooldownTime) {
                return;
            }
        }
        PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, 200, level);
        player.addPotionEffect(effect);

        lastSpeedTime.replace(playerUUID, currentTime);
    }

    public void procHaste(Player player, ItemStack itemStack) {
        UUID playerUUID = player.getUniqueId();

        CustomEnchant haste = enchantsManager.getEnchant("haste", Tools.hoe);
        if (haste == null)
            return;

        if (!enchantsManager.hasEnchantment(itemStack, haste))
            return;

        int level = enchantsManager.getEnchantmentLevel(itemStack, haste);
        long currentTime = System.currentTimeMillis();

        if (lastHasteTime.containsKey(playerUUID)) {
            long lastTime = lastHasteTime.get(playerUUID);

            if ((currentTime - lastTime) < cooldownTime)
                return;
        }


        PotionEffect effect = new PotionEffect(PotionEffectType.FAST_DIGGING, 200, level);
        player.addPotionEffect(effect);

        lastHasteTime.replace(playerUUID, currentTime);
    }

    private void addAmountToMaps(Player player, double money, double essence, double experience) {
        double newMoney = moneyMap.get(player) + money;
        double newEssence = essenceMap.get(player) + essence;
        double newExperience = experienceMap.get(player) + experience;

        moneyMap.replace(player, newMoney);
        essenceMap.replace(player, newEssence);
        experienceMap.replace(player, newExperience);
    }


    public void procCustomEnchants(Player player, ItemStack itemStack) {
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
                String finalCommand = command.replace("%player%", player.getName());
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    finalCommand = PlaceholderAPI.setPlaceholders(player, finalCommand);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);

            }
        }
    }


}
