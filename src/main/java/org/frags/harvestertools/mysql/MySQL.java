package org.frags.harvestertools.mysql;

import org.bukkit.configuration.ConfigurationSection;
import org.frags.harvestertools.HarvesterTools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    private Connection connection;

    public MySQL(HarvesterTools plugin) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("database");
        host = section.getString("host");
        port = section.getString("port");
        database = section.getString("database");
        username = section.getString("username");
        password = section.getString("password");
    }


    public boolean isConnected() {
        return (connection != null);
    }

    public void connect() throws SQLException {
        if (!isConnected()) {
            connection = DriverManager.getConnection("jdbc:mysql://" +
                            host + ":" + port + "/" + database + "?useSSL=false",
                    username, password);
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
