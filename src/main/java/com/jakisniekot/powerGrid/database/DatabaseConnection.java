package com.jakisniekot.powerGrid.database;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DatabaseConnection {

    private MainPlugin plugin;

    private String databaseInfo = "ERROR";

    private Connection connection;

    public DatabaseConnection(MainPlugin plugin) {
        this.plugin = plugin;

    }

    public void connect() {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration lang = plugin.getLang();

        String forceURL = config.getString("databaseConnection.forceURL");
        String user = config.getString("databaseConnection.user");
        String password = config.getString("databaseConnection.password");
        String name = config.getString("databaseConnection.name");
        String port = config.getString("databaseConnection.port");
        String host = config.getString("databaseConnection.host");

        Map<String, String> placeholder = new HashMap<>();
        String url = "jdbc:mysql://" + host + ":" + port + "/" + name + "?useSSL=false&allowPublicKeyRetrieval=true";

        if (forceURL != null) {
            url = forceURL;
            placeholder.put("{forceURL}", forceURL);
        } else if (user == null || password == null || name == null || port == null || host == null) {

            placeholder.put("{status}", lang.getString("info.database.status.error"));
            placeholder.put("{user}", lang.getString("info.database.status.error"));
            placeholder.put("{password}", lang.getString("info.database.status.error"));
            placeholder.put("{name}", lang.getString("info.database.status.error"));
            placeholder.put("{port}", lang.getString("info.database.status.error"));
            placeholder.put("{host}", lang.getString("info.database.status.error"));
            databaseInfo = plugin.stringReplacer(
                    lang.getString("info.database.motd"),
                    placeholder
            );

            return;
        } else {
            placeholder.put("{user}", user);
            placeholder.put("{password}", password);
            placeholder.put("{name}", name);
            placeholder.put("{port}", port);
            placeholder.put("{host}", host);
        }





        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            placeholder.put("{status}", lang.getString("info.database.status.true"));

            databaseInfo = plugin.stringReplacer(
                    lang.getString("info.database.motd"),
                    placeholder
            );

            this.connection = conn;

        } catch (SQLException e) {

            switch (e.getErrorCode()) {
                case 1045 -> placeholder.put("{status}", lang.getString("info.database.exitCodes.1045"));
                case 1049 -> placeholder.put("{status}", lang.getString("info.database.exitCodes.1049"));
                case 0, -1 -> placeholder.put("{status}", lang.getString("info.database.status.false"));
                default -> placeholder.put("{status}", lang.getString("info.database.fatal"));
            }

            databaseInfo = plugin.stringReplacer(
                    lang.getString("info.database.motd"),
                    placeholder
            );
            /*
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            plugin.getLogger().log(Level.INFO, String.valueOf(e.getErrorCode()));
            }, 20L * 5);
             */

        }
    }

    public String getStatus() {
        return databaseInfo;
    }

    public Connection getConnection() {
        return connection;
    }

}