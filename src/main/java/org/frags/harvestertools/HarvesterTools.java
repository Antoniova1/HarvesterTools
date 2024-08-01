package org.frags.harvestertools;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.frags.harvestertools.commands.commandsmanagers.EssenceCommandManager;
import org.frags.harvestertools.commands.commandsmanagers.MainCommandManager;
import org.frags.harvestertools.enchants.EnchantsManager;
import org.frags.harvestertools.essence.EssenceManager;
import org.frags.harvestertools.files.*;
import org.frags.harvestertools.listeners.JoinListener;
import org.frags.harvestertools.listeners.LeaveListener;
import org.frags.harvestertools.listeners.OpenMenuListener;
import org.frags.harvestertools.menusystem.MenuListener;
import org.frags.harvestertools.menusystem.PlayerMenuUtility;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.mysql.MySQL;
import org.frags.harvestertools.mysql.SQLGetter;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;

import static org.frags.harvestertools.managers.MessageManager.setAdventure;

public final class HarvesterTools extends JavaPlugin {

    private static HarvesterTools instance;

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

    private EnchantsManager enchantsManager;

    private EssenceManager essenceManager;

    private BukkitAudiences adventure;

    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    public MySQL sql;
    public SQLGetter data;

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.adventure = BukkitAudiences.create(this);

        instance = this;


        this.config = getConfig();
        this.config.options().copyDefaults(true);
        this.cFile = new File(getDataFolder(), "config.yml");
        saveDefaultConfig();

        setAdventure(this.adventure);

        this.messages = new MessageFile(this);
        this.menuFile = new MenuFile(this);
        this.dataFile = new DataFile(this);
        this.hoeEnchantsFile = new HoeEnchantsFile(this);
        this.swordEnchantsFile = new SwordEnchantsFile(this);
        this.rodEnchantsFile = new RodEnchantsFile(this);
        this.pickaxeEnchantsFile = new PickaxeEnchantsFile(this);



        getCommand("essence").setExecutor(new EssenceCommandManager(this));
        getCommand("harvestertools").setExecutor(new MainCommandManager(this));

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

        registerEvents();
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

    public static HarvesterTools getInstance() {
        return instance;
    }

    public static PlayerMenuUtility getPlayerMenuUtilityMap(Player player, Tools tool, ItemStack item) {
        PlayerMenuUtility playerMenuUtility;
        if (!(playerMenuUtilityMap.containsKey(player))) {
            playerMenuUtility = new PlayerMenuUtility(player, tool, item);
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
    }

    public EnchantsManager getEnchantsManager() {
        return enchantsManager;
    }

    public EssenceManager getEssenceManager() {
        return essenceManager;
    }
}
