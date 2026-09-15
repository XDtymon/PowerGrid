package com.jakisniekot.powerGrid;

import com.jakisniekot.powerGrid.command.mainCommandExecutor;
import com.jakisniekot.powerGrid.database.DatabaseConnection;
import com.jakisniekot.powerGrid.item.ItemFileHandler;
import com.jakisniekot.powerGrid.item.ItemParser;
import com.jakisniekot.powerGrid.lang.LangFileHandler;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MainPlugin extends JavaPlugin {

    private MainPlugin plugin;

    private LangFileHandler langFileHandler;
    private ItemFileHandler itemFileHandler;
    private DatabaseConnection database;

    private Map<String, ItemStack> items;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        //Instances
        this.langFileHandler = new LangFileHandler(plugin);
        this.itemFileHandler = new ItemFileHandler(plugin);
        this.database = new DatabaseConnection(plugin);

        //Files
        langFileHandler.setupLangFiles();
        itemFileHandler.setupItemFiles();
        saveDefaultConfig();

        //Database
        database.connect();

        //Items
        items = new ItemParser(plugin).parseItemsFromConfig();

        //Commands
        getCommand("pg").setExecutor(new mainCommandExecutor(plugin));


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        //Files
        saveConfig();

    }

    public void printLogs(String messageID) {
        Object object;
        if (getLang().getStringList(messageID).isEmpty()) {
            object = getLang().getStringList(messageID);
        } else {
            object = getLang().getString(messageID);
        }

        if (object instanceof String s) {
            System.out.println(s);
        } else {
            assert object != null;
            for (String line : (List<String>) object) {
                System.out.println(line);
            }
        }
    }

    public void printLogs(String messageID, Map<String, String> placeholders) {
        String msg = getLang().getString(messageID, "Missing message: " + messageID);
        Object object;
        if (getLang().getStringList(messageID).isEmpty()) {
            object = getLang().getStringList(messageID);
        } else {
            object = getLang().getString(messageID);
        }

        if (object instanceof String s) {
            System.out.println(s);
        } else {
            assert object != null;
            for (String line : (List<String>) object) {
                System.out.println(line);
            }
        }
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace(entry.getKey(), entry.getValue());
        }

        System.out.println(msg);
    }

    public LangFileHandler getLangFileHandler() {
        return langFileHandler;
    }

    public ItemFileHandler getItemFileHandler() {
        return itemFileHandler;
    }

    public ItemStack getItemFromID(String id) {
        return items.get(id);
    }

    public boolean isExistingItem(String id) {
        return items.containsKey(id);
    }

    public FileConfiguration getLang() {
        return langFileHandler.getLang();
    }

    public Component colorizerLegacy(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
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
