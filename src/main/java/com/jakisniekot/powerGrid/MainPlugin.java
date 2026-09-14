package com.jakisniekot.powerGrid;

import com.jakisniekot.powerGrid.database.DatabaseConnection;
import com.jakisniekot.powerGrid.lang.LangFileHandler;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MainPlugin extends JavaPlugin {

    private MainPlugin plugin;

    private LangFileHandler langFileHandler;
    private DatabaseConnection database;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        //Instances
        this.langFileHandler = new LangFileHandler(plugin);
        this.database = new DatabaseConnection(plugin);

        //Files
        langFileHandler.setupLangFiles();
        saveDefaultConfig();

        //Database
        database.connect();

        //Commands
        //getCommand("")


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        //Files
        saveConfig();

    }


    public LangFileHandler getLangFileHandler() {
        return langFileHandler;
    }

    public FileConfiguration getLang() {
        return langFileHandler.getLang();
    }

    public Component colorizerLegacy(String key) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(key);
    }

    public Component getMessage(String key, Map<String, String> placeholders) {
        String msg = getLang().getString(key, "Missing message: " + key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace(entry.getKey(), entry.getValue());
        }
        return colorizerLegacy(msg);
    }

    public Component getMessage(String key) {
        String msg = getLang().getString(key, "Missing message: " + key);
        return colorizerLegacy(msg);
    }
}
