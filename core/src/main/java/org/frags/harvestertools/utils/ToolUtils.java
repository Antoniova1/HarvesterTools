package org.frags.harvestertools.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.harvestertools.enums.ToolMode;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.managers.LevelManager;
import org.frags.harvestertools.managers.MessageManager;
import org.frags.harvestertools.objects.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

import static org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin;
import static org.frags.harvestertools.HarvesterTools.getInstance;

public class ToolUtils {

    public static NamespacedKey ownerKey = new NamespacedKey(getInstance(), "owner");
    public static NamespacedKey levelKey = new NamespacedKey(getInstance(), "level");
    public static NamespacedKey prestigeKey = new NamespacedKey(getInstance(), "prestige");
    public static NamespacedKey essenceKey = new NamespacedKey(getInstance(), "essence");
    public static NamespacedKey autosellKey = new NamespacedKey(getInstance(), "autosell_mode");
    public static NamespacedKey essenceBoostKey = new NamespacedKey(getInstance(), "essence_boost");
    public static NamespacedKey moneyBoostKey = new NamespacedKey(getInstance(), "money_boost");
    public static NamespacedKey enchantBoostKey = new NamespacedKey(getInstance(), "enchant_boost");
    public static NamespacedKey toolKey = new NamespacedKey(getInstance(), "tool");
    public static NamespacedKey experienceKey = new NamespacedKey(getInstance(), "experience");

    public static NamespacedKey essenceItemKey = new NamespacedKey(getInstance(), "essence_item");

    private static ConfigurationSection section;

    public ToolUtils() {
        section = getInstance().getConfig().getConfigurationSection("tools");
    }

    public static ItemStack getHoe(Player player) {
        try {
            ConfigurationSection hoe = section.getConfigurationSection("hoe");
            ItemStack itemStack = new ItemStack(Material.getMaterial(hoe.getString("material")));

            ItemMeta meta = itemStack.getItemMeta();

            meta.setDisplayName(replaceVariables(hoe.getString("name"), player));

            List<String> lore = hoe.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                String replaced = replaceVariables(lore.get(i), player);
                lore.remove(i);
                lore.add(i, replaced);
            }
            meta.setLore(lore);

            PersistentDataContainer container = meta.getPersistentDataContainer();

            addPDC(container, player);

            //Used to know what's the used tool | Use Tools.getTool() to deserialize it.
            container.set(toolKey, PersistentDataType.STRING, "hoe");

            meta.setUnbreakable(true);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE
            , ItemFlag.HIDE_PLACED_ON);

            itemStack.setItemMeta(meta);
            return itemStack;
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Error while trying to create the Hoe!");
            Bukkit.getLogger().warning("Please, check the wiki to be sure you are doing it right.");
        }
        return null;
    }

    public static ItemStack getSword(Player player) {
        try {
            ConfigurationSection sword = section.getConfigurationSection("sword");
            ItemStack itemStack = new ItemStack(Material.getMaterial(sword.getString("material")));

            ItemMeta meta = itemStack.getItemMeta();

            meta.setDisplayName(replaceVariables(sword.getString("name"), player));

            List<String> lore = sword.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                String replaced = replaceVariables(lore.get(i), player);
                lore.remove(i);
                lore.add(i, replaced);
            }
            meta.setLore(lore);

            PersistentDataContainer container = meta.getPersistentDataContainer();

            addPDC(container, player);

            //Used to know what's the used tool | Use Tools.getTool() to deserialize it.
            container.set(toolKey, PersistentDataType.STRING, "sword");

            meta.setUnbreakable(true);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

            itemStack.setItemMeta(meta);
            return itemStack;
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Error while trying to create the Sword!");
            Bukkit.getLogger().warning("Please, check the wiki to be sure you are doing it right.");
        }
        return null;
    }

    public static ItemStack getRod(Player player) {
        try {
            ConfigurationSection rod = section.getConfigurationSection("rod");
            ItemStack itemStack = new ItemStack(Material.FISHING_ROD);

            ItemMeta meta = itemStack.getItemMeta();

            meta.setDisplayName(replaceVariables(rod.getString("name"), player));

            List<String> lore = rod.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                String replaced = replaceVariables(lore.get(i), player);
                lore.remove(i);
                lore.add(i, replaced);
            }
            meta.setLore(lore);

            PersistentDataContainer container = meta.getPersistentDataContainer();

            addPDC(container, player);

            //Used to know what's the used tool | Use Tools.getTool() to deserialize it.
            container.set(toolKey, PersistentDataType.STRING, "rod");

            meta.setUnbreakable(true);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

            itemStack.setItemMeta(meta);
            return itemStack;
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Error while trying to create the Rod!");
            Bukkit.getLogger().warning("Please, check the wiki to be sure you are doing it right.");
        }
        return null;
    }

    public static ItemStack getPickaxe(Player player) {
        try {
            ConfigurationSection pickaxe = section.getConfigurationSection("pickaxe");
            ItemStack itemStack = new ItemStack(Material.getMaterial(pickaxe.getString("material")));

            ItemMeta meta = itemStack.getItemMeta();

            meta.setDisplayName(replaceVariables(pickaxe.getString("name"), player));

            List<String> lore = pickaxe.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                String replaced = replaceVariables(lore.get(i), player);
                lore.remove(i);
                lore.add(i, replaced);
            }
            meta.setLore(lore);

            PersistentDataContainer container = meta.getPersistentDataContainer();

            addPDC(container, player);

            //Used to know what's the used tool | Use Tools.getTool() to deserialize it.
            container.set(toolKey, PersistentDataType.STRING, "pickaxe");

            meta.setUnbreakable(true);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);


            itemStack.setItemMeta(meta);
            return itemStack;
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Error while trying to create the Pickaxe!");
            Bukkit.getLogger().warning("Please, check the wiki to be sure you are doing it right.");
        }
        return null;
    }

    public static ItemStack giveCorrectItem(Tools tool, Player player) {
        ItemStack item;
        if (tool == Tools.hoe) {
            item = getHoe(player);
        } else if (tool == Tools.rod) {
            item = getRod(player);
        } else if (tool == Tools.pickaxe) {
            item = getPickaxe(player);
        } else {
            item = getSword(player);
        }
        player.getInventory().addItem(item);
        return item;
    }

    private static String replaceVariables(String string, Player player) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        TextComponent text = (TextComponent) miniMessage.deserialize(string);
        int progressType = getInstance().getConfig().getInt("progress-type");
        String progressStringType;
        if (progressType == 1) {
            //Percentage
            progressStringType = "0%";
        } else if (progressType == 2) {
            progressStringType = ChatColor.translateAlternateColorCodes('&', "&c&l|||||||||| &f0%");
        } else {
            progressStringType = ChatColor.translateAlternateColorCodes('&', "&c&l||||||||||");
        }
        //Replace in the return
        return LegacyComponentSerializer.legacySection().serialize(text).replace("%owner%", player.getName()).replace("%level%", "0").
                replace("%prestige%", "0").replace("%sell-mode%", "Collect").replace("%essence%", "0")
                .replace("%enchantlist%", "").replace("%progress%", progressStringType);
    }

    private static void addPDC(PersistentDataContainer container, Player player) {
        // Sets the owner
        container.set(ownerKey, PersistentDataType.STRING, player.getName());

        // Sets the level
        container.set(levelKey, PersistentDataType.INTEGER, 0);

        // Sets the prestige
        container.set(prestigeKey, PersistentDataType.INTEGER, 0);

        // Sets the essence
        container.set(essenceKey, PersistentDataType.DOUBLE, 0D);

        // Sets the mode
        container.set(autosellKey, PersistentDataType.STRING, ToolMode.toString(ToolMode.Collect));

        // Sets the essence boost
        container.set(essenceBoostKey, PersistentDataType.DOUBLE, 0D);

        // Sets the money boost
        container.set(moneyBoostKey, PersistentDataType.DOUBLE, 0D);

        // Sets the enchant boost
        container.set(enchantBoostKey, PersistentDataType.DOUBLE, 0D);

        //Sets the experience
        container.set(experienceKey, PersistentDataType.DOUBLE, 0D);
    }

    @Nullable
    public static Tools getTool(@NotNull ItemStack itemStack) {
        if (!isTool(itemStack))
            return null;

        String toolType = itemStack.getItemMeta().getPersistentDataContainer().get(toolKey, PersistentDataType.STRING);
        if (toolType == null)
            return null;
        return Tools.getTool(toolType);
    }

    public static int getItemLevel(ItemStack itemStack) {
        if (!isTool(itemStack))
            return 0;
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        return container.getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
    }

    public static boolean isMaxLevel(ItemStack itemStack) {
        Level level = getInstance().getLevelManager().getLevel(getTool(itemStack));

        return getItemLevel(itemStack) >= level.getMaxLevel();
    }

    public static double getItemExperience(ItemStack itemStack) {
        if (!isTool(itemStack))
            return 0;

        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        return container.getOrDefault(experienceKey, PersistentDataType.DOUBLE, 0D);
    }

    public static boolean isAutoSell(ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        String mode = container.get(autosellKey, PersistentDataType.STRING);

        ToolMode toolMode = ToolMode.getToolMode(mode);

        return toolMode == ToolMode.AutoSell;
    }

    public static ToolMode changeSellMode(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();


        if (isAutoSell(itemStack)) {
            container.set(autosellKey, PersistentDataType.STRING, ToolMode.toString(ToolMode.Collect));
            itemStack.setItemMeta(meta);
            return ToolMode.Collect;
        }
        container.set(autosellKey, PersistentDataType.STRING, ToolMode.toString(ToolMode.AutoSell));
        itemStack.setItemMeta(meta);
        return ToolMode.AutoSell;
    }

    public static String getItemSellMode(ItemStack itemStack) {
        if (!isTool(itemStack))
            return null;
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();;

        return container.getOrDefault(autosellKey, PersistentDataType.STRING, "Collect");
    }

    public static int getItemPrestige(ItemStack itemStack) {
        if (!isTool(itemStack))
            return 0;
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();;

        return container.getOrDefault(prestigeKey, PersistentDataType.INTEGER, 0);
    }


    public static double getItemEssence(ItemStack itemStack) {
        if (!isTool(itemStack))
            return 0;
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();;

        return container.getOrDefault(essenceKey, PersistentDataType.DOUBLE, 0D);
    }


    public static boolean isTool(@NotNull ItemStack itemStack) {
        if (itemStack.getType() == Material.AIR || itemStack == null)
            return false;

        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        return container.has(toolKey, PersistentDataType.STRING);
    }

    public static void setLevel(@NotNull ItemStack itemStack, int level) {
        if (!isTool(itemStack))
            return;

        int toolLevel = ToolUtils.getItemLevel(itemStack);
        int maxLevel = getInstance().getLevelManager().getLevel(getTool(itemStack)).getMaxLevel();

        int newLevel = level + toolLevel;

        if (newLevel > maxLevel)
            newLevel = maxLevel;


        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(levelKey, PersistentDataType.INTEGER, newLevel);
        itemStack.setItemMeta(meta);
    }

    public static void setPrestige(@NotNull ItemStack itemStack, int prestige) {
        if (!isTool(itemStack))
            return;

        int toolPrestige = getItemPrestige(itemStack);
        int maxPrestige = getInstance().getConfig().getInt("Prestige.max-prestige");

        int newPrestige = prestige + toolPrestige;

        if (newPrestige > maxPrestige)
            newPrestige = maxPrestige;

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(prestigeKey, PersistentDataType.INTEGER, newPrestige);

        container.set(ToolUtils.levelKey, PersistentDataType.INTEGER, 0);

        container.set(ToolUtils.experienceKey, PersistentDataType.DOUBLE, 0D);

        container.set(ToolUtils.prestigeKey, PersistentDataType.INTEGER, newPrestige);

        ConfigurationSection prestigeSection = getInstance().getConfig().getConfigurationSection("Prestige");


        ConfigurationSection section = prestigeSection.getConfigurationSection("boosts");

        if (prestigeSection.getBoolean("boost-essence")) {
            double essenceBoost = container.get(ToolUtils.essenceBoostKey, PersistentDataType.DOUBLE);
            double prestigeEssenceBoost = section.getDouble("essence");
            container.set(ToolUtils.essenceBoostKey, PersistentDataType.DOUBLE, essenceBoost + prestigeEssenceBoost);
        }

        if (prestigeSection.getBoolean("boost-money")) {
            double moneyBoost = container.get(ToolUtils.moneyBoostKey, PersistentDataType.DOUBLE);
            double prestigeMoneyBoost = section.getDouble("money");
            container.set(ToolUtils.moneyBoostKey, PersistentDataType.DOUBLE, moneyBoost + prestigeMoneyBoost);
        }

        if (prestigeSection.getBoolean("boost-enchants")) {
            double enchantBoost = container.get(ToolUtils.enchantBoostKey, PersistentDataType.DOUBLE);
            double prestigeEnchantBoost = section.getDouble("enchants");
            container.set(ToolUtils.enchantBoostKey, PersistentDataType.DOUBLE, enchantBoost + prestigeEnchantBoost);
        }

        itemStack.setItemMeta(meta);
        updateVariables(itemStack);
    }

    public static void setExperience(@NotNull ItemStack itemStack, double newExperience) {
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(experienceKey, PersistentDataType.DOUBLE, newExperience);
        itemStack.setItemMeta(meta);
    }

    private static String createProgessBar(double percentage, int lenght) {
        int progress = (int) (lenght * (percentage / 100));

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lenght; i++) {
            if (progress <= 0) {
                //builder.append(ChatColor.RED + "██████████");
                builder.append(ChatColor.translateAlternateColorCodes('&', "&c&l||||||||||"));
                break;
            }
            if (i < progress) {
                //builder.append(ChatColor.GREEN + "█");
                builder.append(ChatColor.translateAlternateColorCodes('&', "&a&l|"));
            } else {
                //builder.append(ChatColor.RED + "█");
                builder.append(ChatColor.translateAlternateColorCodes('&', "&c&l|"));
            }
        }
        return builder.toString();
    }

    public static void updateVariables(@NotNull ItemStack itemStack) {
        Tools tools = getTool(itemStack);
        //tools.tools.name


        ItemMeta meta = itemStack.getItemMeta();

        FileConfiguration config = getInstance().getConfig();

        String expectedName = config.getString("tools." + tools.name() + ".name");

        int level = getItemLevel(itemStack);
        String prestige = String.valueOf(getItemPrestige(itemStack));
        String essence = Utils.formatNumber(BigDecimal.valueOf(getItemEssence(itemStack)));
        String sellMode = getItemSellMode(itemStack);

        LevelManager levelManager = getInstance().getLevelManager();

        Level levels = levelManager.getLevel(tools);

        //starting-xp + starting-xp * (level * increment-xp)

        double nextLevelXP = levels.getStartingXP() + levels.getStartingXP() * (level * levels.getIncrementXP());

        double percentage = (getItemExperience(itemStack) / nextLevelXP) * 100;

        String formattedPercentage = String.format("%.2f", percentage);


        int progressType = getInstance().getConfig().getInt("progress-type");
        String progressStringType;
        if (progressType == 1) {
            //Percentage
            progressStringType = formattedPercentage + "%";
        } else if (progressType == 2) {
            progressStringType = createProgessBar(percentage, 10) + " " + ChatColor.WHITE + formattedPercentage + ChatColor.WHITE + "%";
        } else {
            progressStringType = createProgessBar(percentage, 10);
        }


        if (expectedName.contains("%level%")) {
            meta.setDisplayName(MessageManager.miniStringParse(expectedName).replace("%level%", String.valueOf(level)));
        } else if (expectedName.contains("%prestige%")) {
            meta.setDisplayName(MessageManager.miniStringParse(expectedName).replace("%prestige%", prestige));
        } else if (expectedName.contains("%essence%")) {
            meta.setDisplayName(MessageManager.miniStringParse(expectedName).replace("%essence%", essence));
        } else if (expectedName.contains("%sell-mode%")) {
            meta.setDisplayName(MessageManager.miniStringParse(expectedName).replace("%sell-mode%", sellMode));
        } else if (expectedName.contains("%progress%")) {
            meta.setDisplayName(MessageManager.miniStringParse(expectedName).replace("%progress%", progressStringType));
        }

        List<String> expectedLore = getInstance().getConfig().getStringList("tools." + tools.name() + ".lore");

        //Edit level
        int levelLine = -1;
        int prestigeLine = -1;
        int essenceLine = -1;
        int sellModeLine = -1;
        int progressLine = -1;
        for (int i = 0; i < expectedLore.size(); i++) {
            String line = expectedLore.get(i);
            if (line.contains("%level%")) {
                levelLine = i;
            } else if (line.contains("%prestige%")) {
                prestigeLine = i;
            } else if (line.contains("%essence%")) {
                essenceLine = i;
            } else if (line.contains("%sell-mode%")) {
                sellModeLine = i;
            } else if (line.contains("%progress%")) {
                progressLine = i;
            }
        }


        List<String> lore = meta.getLore();

        assert lore != null;
        if (levelLine != -1)
            lore.set(levelLine, MessageManager.miniStringParse(expectedLore.get(levelLine).replace("%level%", String.valueOf(level))));
        if (prestigeLine != -1)
            lore.set(prestigeLine, MessageManager.miniStringParse(expectedLore.get(prestigeLine).replace("%prestige%", prestige)));
        if (essenceLine != -1)
            lore.set(essenceLine, MessageManager.miniStringParse(expectedLore.get(essenceLine).replace("%essence%", essence)));
        if (sellModeLine != -1)
            lore.set(sellModeLine, MessageManager.miniStringParse(expectedLore.get(sellModeLine).replace("%sell-mode%", getItemSellMode(itemStack))));
        if (progressLine != -1)
            lore.set(progressLine, MessageManager.miniStringParse(expectedLore.get(progressLine)).replace("%progress%", progressStringType));
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }



}
