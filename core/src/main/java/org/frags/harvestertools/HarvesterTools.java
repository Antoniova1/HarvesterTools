package org.frags.harvestertools;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfigurationOptions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
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
import org.frags.harvestertools.toolsmanagers.HoeManager;
import org.frags.harvestertools.toolsmanagers.PickaxeManager;
import org.frags.harvestertools.toolsmanagers.RodManager;
import org.frags.harvestertools.toolsmanagers.SwordManager;
import org.frags.harvestertools.utils.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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

    public static ItemStack carameloAzul;
    public static ItemStack carameloNaranja;
    public static ItemStack carameloMorado;
    public static ItemStack carameloDorado;
    public static ItemStack carameloVerde;

    private static final UUID RANDOM_UUID = UUID.fromString("92864445-51c5-4c3b-9039-517c9927d1b4");


    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    public final HashMap<String, HoeManager> hoeManagerMap = new HashMap<>();
    public final HashMap<String, SwordManager> swordManagerMap = new HashMap<>();
    public final HashMap<String, RodManager> rodManagerMap = new HashMap<>();
    public final HashMap<String, PickaxeManager> pickaxeManagerMap = new HashMap<>();

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

        createCarameloAzul();
        createCarameloNaranja();
        createCarameloMorado();
        createCarameloDorado();
        createCarameloVerde();


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

    public static void createCarameloMorado() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        PlayerProfile profile = getProfile("http://textures.minecraft.net/texture/5bf88af96d30085a6d084376f1018627b7b4512d26af21e750e4d09cd261d6be");
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwnerProfile(profile);

        skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&l&nCaramelo Morado"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Un dulce místico con destellos oscuros, hallado en las"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7profundidades de la tierra durante Halloween. Su esencia"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7parece estar impregnada de magia antigua."));
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7¡Úsalo para tradear y desenterrar secretos sombríos!"));

        skullMeta.setLore(lore);

        itemStack.setItemMeta(skullMeta);
        carameloMorado = itemStack;
    }

    public static void createCarameloVerde() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        PlayerProfile profile = getProfile("http://textures.minecraft.net/texture/210f26213fe751c1c63074beca2c9b6074293216f7282e2985c4c1ef8ff18ccb");
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwnerProfile(profile);

        skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&l&nCaramelo Verde"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Un dulce especial cubierto de un brillo esmeralda. Se"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7dice que solo aparece durante la temporada de &6Halloween&7,"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7escondido entre los cultivos más misteriosos."));
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7¡Úsalo para tradear y conseguir espeluznantes recompensas!"));

        skullMeta.setLore(lore);

        itemStack.setItemMeta(skullMeta);
        carameloVerde = itemStack;
    }


    public static void createCarameloAzul() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        PlayerProfile profile = getProfile("http://textures.minecraft.net/texture/acae2920a19ead5dce963a301164e95a5a2f5fe2a3b7b9bfb0eb5deb559f0040");
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwnerProfile(profile);

        skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&l&nCaramelo Azul"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Un raro dulce con un brillo profundo y cristalino, encontrado"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7solo en las aguas embrujadas durante &6Halloween&7. Dicen"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7que su sabor es tan frío como el océano nocturno."));
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7¡Úsalo para tradear y descubrir tesoros escalofriantes!"));

        skullMeta.setLore(lore);

        itemStack.setItemMeta(skullMeta);
        carameloAzul = itemStack;
    }

    public static void createCarameloNaranja() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        PlayerProfile profile = getProfile("http://textures.minecraft.net/texture/72b65a206b2ea931f96c1cf18e0e0ccefb2771f4ecae550c80c429fe477c8036");
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwnerProfile(profile);

        skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&l&nCaramelo Naranja"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Un dulce cálido y chispeante, infundido con la energía de"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7los espíritus caídos. Solo aparece tras derrotar a"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7criaturas tenebrosas durante Halloween."));
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7¡Úsalo para tradear y reclamar espeluznantes recompensas de los más valientes!"));

        skullMeta.setLore(lore);


        itemStack.setItemMeta(skullMeta);
        carameloNaranja = itemStack;
    }

    public static void createCarameloDorado() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        PlayerProfile profile = getProfile("http://textures.minecraft.net/texture/e4b3cc149377e1cd7990d562d58154d400f92dc22483213c3c060e7d35e0960f");
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwnerProfile(profile);

        skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l&nCaramelo Dorado"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Un dulce legendario con un resplandor dorado, obtenido solo"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7por los más dedicados durante Halloween. Se dice que contiene"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7la esencia de todos los elementos: cosecha, pesca, minería y combate."));
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7¡Úsalo para tradear y desbloquear las recompensas más valiosas y codiciadas!"));

        skullMeta.setLore(lore);


        itemStack.setItemMeta(skullMeta);
        carameloDorado = itemStack;
    }


    private static PlayerProfile getProfile(String url) {
        PlayerProfile profile = Bukkit.createPlayerProfile(RANDOM_UUID); // Get a new player profile
        PlayerTextures textures = profile.getTextures();
        URL urlObject;
        try {
            urlObject = new URL(url); // The URL to the skin, for example: https://textures.minecraft.net/texture/18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Invalid URL", exception);
        }
        textures.setSkin(urlObject); // Set the skin of the player profile to the URL
        profile.setTextures(textures); // Set the textures back to the profile
        return profile;
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
        getServer().getPluginManager().registerEvents(new ObtainExperienceListener(this), this);
        getServer().getPluginManager().registerEvents(new ObtainEssenceListener(this), this);
        getServer().getPluginManager().registerEvents(new ObtainMoneyListener(this), this);
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

    public HoeManager getHoeManager(Player player) {
        return hoeManagerMap.get(player.getName());
    }

    public SwordManager getSwordManager(Player player) {
        return swordManagerMap.get(player.getName());
    }

    public RodManager getRodManager(Player player) {
        return rodManagerMap.get(player.getName());
    }

    public PickaxeManager getPickaxeManager(Player player) {
        return pickaxeManagerMap.get(player.getName());
    }
}
