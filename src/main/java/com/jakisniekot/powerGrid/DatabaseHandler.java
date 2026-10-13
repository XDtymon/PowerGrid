package com.jakisniekot.powerGrid;

import com.jakisniekot.powerGrid.util.TextUtility;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DatabaseHandler {

    private MainPlugin plugin;

    private String databaseInfo = "ERROR";
    private String DB_TYPE;
    private Connection connection;

    public DatabaseHandler(MainPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.DB_TYPE = config.getString("databaseType");
    }

    public void connect() {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration lang = plugin.lang();


        if (Objects.equals(DB_TYPE, "MYSQL")) {

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

                placeholder.put("{status}", lang.getString("info.database.status.fatal"));
                placeholder.put("{user}", lang.getString("info.database.status.fatal"));
                placeholder.put("{password}", lang.getString("info.database.status.fatal"));
                placeholder.put("{name}", lang.getString("info.database.status.fatal"));
                placeholder.put("{port}", lang.getString("info.database.status.fatal"));
                placeholder.put("{host}", lang.getString("info.database.status.fatal"));

                databaseInfo = TextUtility.consoleString(
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





            try {
                this.connection = DriverManager.getConnection(url, user, password);

                placeholder.put("{status}", lang.getString("info.database.status.true"));

                databaseInfo = TextUtility.consoleString(
                        lang.getString("info.database.motd"),
                        placeholder
                );


            } catch (SQLException e) {

                switch (e.getErrorCode()) {
                    case 1045 -> placeholder.put("{status}", lang.getString("info.database.exitCodes.1045"));
                    case 1049 -> placeholder.put("{status}", lang.getString("info.database.exitCodes.1049"));
                    case 0, -1 -> placeholder.put("{status}", lang.getString("info.database.status.false"));
                    default -> placeholder.put("{status}", lang.getString("info.database.status.fatal"));
                }

                databaseInfo = TextUtility.consoleString(
                        lang.getString("info.database.motd"),
                        placeholder
                );
            /*
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            plugin.getLogger().log(Level.INFO, String.valueOf(e.getErrorCode()));
            }, 20L * 5);
             */

            }
        } else if (Objects.equals(DB_TYPE, "SQLITE")) {

            Map<String, String> placeholder = new HashMap<>();

            placeholder.put("{user}", "LOCAL");
            placeholder.put("{password}", "LOCAL");
            placeholder.put("{name}", "LOCAL");
            placeholder.put("{port}", "LOCAL");
            placeholder.put("{host}", "LOCAL");
            placeholder.put("{status}", lang.getString("info.database.status.false"));

            File dbFile = new File(plugin.getDataFolder(), "database.db");
            if (!dbFile.exists()) {
                plugin.getDataFolder().mkdirs();
                try {
                    dbFile.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to create database.db: " + e.getMessage());
                }
            }

            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            try {
                connection = DriverManager.getConnection(url);

                databaseInfo = TextUtility.consoleString(
                        lang.getString("info.database.motd"),
                        placeholder
                );

            } catch (SQLException e) {
                switch (e.getErrorCode()) {
                    case 1045 -> placeholder.put("{status}", lang.getString("info.database.exitCodes.1045"));
                    case 1049 -> placeholder.put("{status}", lang.getString("info.database.exitCodes.1049"));
                    case 0, -1 -> placeholder.put("{status}", lang.getString("info.database.status.false"));
                    default -> placeholder.put("{status}", lang.getString("info.database.status.fatal"));
                }

                databaseInfo = TextUtility.consoleString(
                        lang.getString("info.database.motd"),
                        placeholder
                );
            }
        }

    }

    public String getStatus() {
        return databaseInfo;
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTables() {
        if (connection == null) {
            plugin.printLogs("errors.database.noConnection");
            return;
        }
        String connectors = """
        CREATE TABLE IF NOT EXISTS connectors (
            id VARCHAR(64) PRIMARY KEY,
            max_connections INTEGER NOT NULL DEFAULT 0,
            allowed_wires VARCHAR(64),
            blacklist BOOLEAN NOT NULL DEFAULT FALSE,
            connectorType VARCHAR(64),
            itemID VARCHAR(64)
        );
        """;
        String wires = """
        CREATE TABLE IF NOT EXISTS wires (
            loc1 VARCHAR(64) NOT NULL,
            loc2 VARCHAR(64) NOT NULL,
            watt_transfer INTEGER NOT NULL DEFAULT 0,
            headBlocks VARCHAR(64),
            itemID VARCHAR(64),
            PRIMARY KEY (loc1, loc2)
        );
        """;
        String machines = """
        CREATE TABLE IF NOT EXISTS machines (
            locationID   VARCHAR(64)  NOT NULL PRIMARY KEY,
            machineID    VARCHAR(64)  NOT NULL,
            energy       BIGINT       NOT NULL DEFAULT 0,
            maxEnergy    BIGINT       NOT NULL DEFAULT 0,
            inputEnergy  BIGINT       NOT NULL DEFAULT 0,
            outputEnergy BIGINT       NOT NULL DEFAULT 0,
            progress     INT          NOT NULL DEFAULT 0,
            recipeID     VARCHAR(64)  NULL,
            inputItems   BLOB         NULL,
            outputItems  BLOB         NULL
        );
        """;

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(connectors);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(wires);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(machines);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}