package org.frags.harvestertools.mysql;

import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class SQLGetter {

    private final HarvesterTools plugin;

    public SQLGetter(HarvesterTools plugin) {
        this.plugin = plugin;
    }


    public void createTable() {
        PreparedStatement ps;
        try {
            ps = plugin.sql.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS harvestertools "
                    + "(UUID VARCHAR(100),ESSENCE DOUBLE(255),PRIMARY KEY (UUID))");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
