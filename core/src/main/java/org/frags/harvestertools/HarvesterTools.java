package org.frags.harvestertools;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemoryConfigurationOptions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.frags.harvestertools.commands.commandsmanagers.EssenceCommandManager;
import org.frags.harvestertools.commands.commandsmanagers.MainCommandManager;
import org.frags.harvestertools.commands.tabcompleters.EssenceTab;
import org.frags.harvestertools.commands.tabcompleters.MainCommandTab;
import org.frags.harvestertools.enchants.EnchantsManager;
import org.frags.harvestertools.essence.EssenceManager;
import org.frags.harvestertools.files.*;
import org.frags.harvestertools.listeners.*;
import org.frags.harvestertools.managers.BlockManager;
import org.frags.harvestertools.managers.CropsManager;
import org.frags.harvestertools.managers.LevelManager;
import org.frags.harvestertools.managers.MobManager;
import org.frags.harvestertools.menusystem.MenuListener;
import org.frags.harvestertools.menusystem.PlayerMenuUtility;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.mysql.MySQL;
import org.frags.harvestertools.mysql.SQLGetter;
import org.frags.harvestertools.placeholderapi.EssenceExpansions;
import org.frags.harvestertools.utils.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.frags.harvestertools.managers.MessageManager.setAdventure;

public final class HarvesterTools extends JavaPlugin {

    private static HarvesterTools instance;

    private String minecraftVersion;
    private NMSHandler nmsHandler;

    private FileConfiguration config;

    public boolean useDatabase;

    private File cFile;

    public MessageFile messages;

    public MenuFile menuFile;

    public HoeEnchantsFile hoeEnchantsFile;

    public SwordEnchantsFile swordEnchantsFile;

    public RodEnchantsFile rodEnchantsFile;

    public PickaxeEnchantsFile pickaxeEnchantsFile;

    public DataFile dataFile;

    public ShopFile shopFile;

    public CropsFile cropsFile;

    public MobsFile mobsFile;

    public BlocksFile blocksFile;

    public EventsFile eventsFile;

    private EnchantsManager enchantsManager;

    private EssenceManager essenceManager;

    private MobManager mobManager;

    private BukkitAudiences adventure;

    private CropsManager crops;

    private LevelManager levelManager;

    private BlockManager blockManager;

    private CropBreakUtils cropUtils;

    private FishingUtils fishingUtils;

    private MobSwordUtils mobUtils;

    private PickaxeUtils pickaxeUtils;

    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    public MySQL sql;
    public SQLGetter data;

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    //Vault
    private Economy econ = null;

    public boolean canUseVault = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        this.nmsHandler = createNMSHandler();

        this.adventure = BukkitAudiences.create(this);



        /*this.config = getConfig();
        this.config.options().copyDefaults(true);
        this.cFile = new File(getDataFolder(), "config.yml");
        saveDefaultConfig();
         */

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        if (setupEconomy()) {
            canUseVault = true;
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Detected " + ChatColor.WHITE + "Vault");
        }


        setAdventure(this.adventure);

        this.messages = new MessageFile(this);
        this.menuFile = new MenuFile(this);
        this.dataFile = new DataFile(this);
        this.hoeEnchantsFile = new HoeEnchantsFile(this);
        this.swordEnchantsFile = new SwordEnchantsFile(this);
        this.rodEnchantsFile = new RodEnchantsFile(this);
        this.pickaxeEnchantsFile = new PickaxeEnchantsFile(this);
        this.shopFile = new ShopFile(this);
        this.cropsFile = new CropsFile(this);
        this.mobsFile = new MobsFile(this);
        this.blocksFile = new BlocksFile(this);
        this.eventsFile = new EventsFile(this);

        getCommand("essence").setExecutor(new EssenceCommandManager(this));
        getCommand("essence").setTabCompleter(new EssenceTab());
        getCommand("harvestertools").setExecutor(new MainCommandManager(this));
        getCommand("harvestertools").setTabCompleter(new MainCommandTab());

        useDatabase = getConfig().getBoolean("database.use");

        if (useDatabase) {
            this.sql = new MySQL(this);
            this.data = new SQLGetter(this);
            try {
                sql.connect();
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "No se ha podido conectar a la database, revisa la información!");
                return;
            }
            if (sql.isConnected()) {
                Bukkit.getLogger().info("Database connected!");
                data.createTable();
            }
        } else {
          //Create data file
            this.dataFile = new DataFile(this);
        }

        this.essenceManager = new EssenceManager(this);

        this.enchantsManager = new EnchantsManager(this);

        this.mobManager = new MobManager(this);

        this.crops = new CropsManager(this);

        this.levelManager = new LevelManager(this);

        this.blockManager = new BlockManager(this);

        this.cropUtils = new CropBreakUtils(this);

        this.fishingUtils = new FishingUtils(this);

        this.mobUtils = new MobSwordUtils(this);

        this.pickaxeUtils = new PickaxeUtils(this);

        new ToolUtils();

        registerEvents();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EssenceExpansions(this).register();
            essenceManager.loadEssenceTops();
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            getEssenceManager().unload(player);
        }

        if (useDatabase) {
            sql.disconnect();
        }


    }

    public void reloadObjects() {
        this.nmsHandler = createNMSHandler();

        this.enchantsManager = new EnchantsManager(this);

        this.mobManager = new MobManager(this);

        this.crops = new CropsManager(this);

        this.levelManager = new LevelManager(this);

        this.blockManager = new BlockManager(this);

        this.cropUtils = new CropBreakUtils(this);

        this.fishingUtils = new FishingUtils(this);

        this.mobUtils = new MobSwordUtils(this);

        this.pickaxeUtils = new PickaxeUtils(this);

        new ToolUtils();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static HarvesterTools getInstance() {
        return instance;
    }

    public static PlayerMenuUtility getPlayerMenuUtilityMap(Player player) {
        PlayerMenuUtility playerMenuUtility;
        if (!(playerMenuUtilityMap.containsKey(player))) {
            playerMenuUtility = new PlayerMenuUtility(player);
            playerMenuUtilityMap.put(player, playerMenuUtility);

            return playerMenuUtility;
        } else {
            return playerMenuUtilityMap.get(player);
        }
    }

    public static PlayerMenuUtility createPlayerMenuUtility(Player player, Tools tool, ItemStack item) {
        return new PlayerMenuUtility(player, tool, item);
    }


    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new OpenMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new CropBrokenListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerFishListener(this), this);
        getServer().getPluginManager().registerEvents(new MobSwordListener(this), this);
        getServer().getPluginManager().registerEvents(new HarvesterPickaxeListener(this), this);
        getServer().getPluginManager().registerEvents(new ToolDropListener(), this);
        getServer().getPluginManager().registerEvents(new PrepareToolsListener(), this);
    }

    private String getMinecraftVersion() {
        if (minecraftVersion != null) {
            return minecraftVersion;
        } else {
            String bukkitGetVersionOutput = Bukkit.getVersion();
            Matcher matcher = Pattern.compile("\\(MC: (?<version>[\\d]+\\.[\\d]+(\\.[\\d]+)?)\\)").matcher(bukkitGetVersionOutput);
            if (matcher.find()) {
                return minecraftVersion = matcher.group("version");
            } else {
                throw new RuntimeException("Could not determine Minecraft version from Bukkit.getVersion(): " + bukkitGetVersionOutput);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private NMSHandler createNMSHandler() throws RuntimeException {
        String clazzName = "org.frags.harvestertools.nms_" + getMinecraftVersion()
                .replace(".", "_") + ".NMSHandlerImpl";
        try {
            Class<? extends NMSHandler> clazz = (Class<? extends NMSHandler>) Class.forName(clazzName);
            return clazz.getConstructor().newInstance();
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException("Can't instantiate NMSHandlerImpl for version " + getMinecraftVersion() +
                    " (class " + clazzName + " not found. This usually means that this Minecraft version is not " +
                    "supported by this version of the plugin.)", exception);
        } catch (InvocationTargetException exception) {
            throw new RuntimeException("Can't instantiate NMSHandlerImpl for version " + getMinecraftVersion() +
                    " (constructor in class " + clazzName + " threw an exception)", exception);
        } catch (InstantiationException exception) {
            throw new RuntimeException("Can't instantiate NMSHandlerImpl for version " + getMinecraftVersion() +
                    " (class " + clazzName + " is abstract)", exception);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Can't instantiate NMSHandlerImpl for version " + getMinecraftVersion() +
                    " (no-args constructor in class " + clazzName + " is not accessible)", exception);
        } catch (NoSuchMethodException exception) {
            throw new RuntimeException("Can't instantiate NMSHandlerImpl for version " + getMinecraftVersion() +
                    " (no no-args constructor found in class " + clazzName + ")", exception);
        }
    }

    public NMSHandler getNmsHandler() {
        return nmsHandler;
    }

    public EnchantsManager getEnchantsManager() {
        return enchantsManager;
    }

    public EssenceManager getEssenceManager() {
        return essenceManager;
    }

    public CropsManager getCropsManager() {
        return crops;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public Economy getEcon() {
        return econ;
    }

    public CropBreakUtils getCropUtils() {
        return cropUtils;
    }

    public FishingUtils getFishingUtils() {
        return fishingUtils;
    }

    public PickaxeUtils getPickaxeUtils() {
        return pickaxeUtils;
    }

    public MobManager getMobManager() {
        return mobManager;
    }

    public MobSwordUtils getMobUtils() {
        return mobUtils;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

}
