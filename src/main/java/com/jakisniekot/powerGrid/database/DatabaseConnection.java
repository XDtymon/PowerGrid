package com.jakisniekot.powerGrid.database;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private MainPlugin plugin;

    public DatabaseConnection(MainPlugin plugin) {
        this.plugin = plugin;

    }

    public void connect() {
        FileConfiguration config = plugin.getConfig();

        String url = config.getString("databaseConnection.url");
        String user = config.getString("databaseConnection.user");
        String password = config.getString("databaseConnection.password");

        try {
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                System.out.println("success.database.databaseConnectionEstablished");
            }
        } catch (SQLException e) {
            plugin.printLogs("errors.database.noDatabaseConnection");
            e.printStackTrace();
        }
    }
}