package org.frags.harvestertools.essence;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.files.DataFile;
import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EssenceManager {

    private final HarvesterTools plugin;
    private final boolean useDatabase;
    private DataFile data;

    private HashMap<UUID, Double> playerEssence = new HashMap<>();

    public EssenceManager(HarvesterTools plugin) {
        this.plugin = plugin;
        this.useDatabase = plugin.useDatabase;
        if (!useDatabase)
            this.data = plugin.dataFile;

    }


    public void createPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (useDatabase) {
            try {
                if (!exists(uuid)) {
                    PreparedStatement ps = plugin.sql.getConnection().prepareStatement("INSERT IGNORE INTO harvestertools (UUID) VALUES (?)");
                    ps.setString(1, uuid.toString());
                    ps.executeUpdate();
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean exists(UUID uuid) {
        try {
            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("SELECT * FROM harvestertools WHERE UUID =?");
            ps.setString(1, uuid.toString());

            ResultSet results = ps.executeQuery();
            boolean result = results.next();
            ps.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void loadPlayer(UUID uuid) {
        playerEssence.put(uuid, getStoredEssence(uuid));
    }

    public void addEssence(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        double newAmount = amount + getEssence(player);
        playerEssence.replace(uuid, newAmount);
    }


    public boolean withdrawEssence(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        if (amount > getEssence(player)) {
            //Player does not have that much money
            return false;
        } else {
            double newAmount = getEssence(player) - amount;
            playerEssence.replace(uuid, newAmount);
            return true;
        }
    }

    public double getEssence(Player player) {
        if (player != null && player.isOnline()) {
            return playerEssence.getOrDefault(player.getUniqueId(), 0D);
        } else {
            return getStoredEssence(player.getUniqueId());
        }
    }

    public double getEssence(OfflinePlayer player) {
        if (player.isOnline()) {
            return playerEssence.getOrDefault(player.getUniqueId(), 0D);
        } else {
            return getStoredEssence(player.getUniqueId());
        }
    }


    public String getFormattedEssence(Player player) {
        double amount;
        if (player.isOnline()) {
            amount = playerEssence.getOrDefault(player.getUniqueId(), 0D);
        } else {
            amount = getStoredEssence(player.getUniqueId());
        }
        return Utils.formatNumber(new BigDecimal(amount));
    }

    public String getFormattedEssence(OfflinePlayer player) {
        double amount;
        if (player.isOnline()) {
            amount = playerEssence.getOrDefault(player.getUniqueId(), 0D);
        } else {
            amount = getStoredEssence(player.getUniqueId());
        }
        return Utils.formatNumber(new BigDecimal(amount));
    }

    private void setEssence(Player player) {
        UUID uuid = player.getUniqueId();
        if (useDatabase) {
            try {
                PreparedStatement ps = plugin.sql.getConnection().prepareStatement("UPDATE harvestertools SET ESSENCE=? WHERE UUID=?");
                ps.setDouble(1, getEssence(player));
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            data.getConfig().set(uuid.toString(), getEssence(player));
            data.saveConfig();
        }
    }


    private double getStoredEssence(UUID uuid) {
        //This is only fired to load into the hashmap
        if (useDatabase) {
            try {
                PreparedStatement ps = plugin.sql.getConnection().prepareStatement("SELECT ESSENCE FROM harvestertools WHERE UUID=?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double result = rs.getDouble("ESSENCE");
                    ps.close();
                    return result;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            if (data.getConfig().contains(uuid.toString())) {
                return data.getConfig().getDouble(uuid.toString());
            }
        }
        return 0;
    }

    public void reloadPlayerEssence(Player player) {
        //This is fired every 2 hours in JoinListener to avoid balance loss when the server shuts down because an error.
        setEssence(player);
    }

    public void unload(Player player) {
        //This is fired on QuitEvent and onDisable
        setEssence(player);
        playerEssence.remove(player.getUniqueId());
    }


}