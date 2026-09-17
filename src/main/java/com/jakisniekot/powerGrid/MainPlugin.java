package com.jakisniekot.powerGrid;

import com.jakisniekot.powerGrid.command.mainCommandExecutor;
import com.jakisniekot.powerGrid.command.mainCommandTabber;
import com.jakisniekot.powerGrid.database.DatabaseConnection;
import com.jakisniekot.powerGrid.item.ItemFileHandler;
import com.jakisniekot.powerGrid.item.ItemParser;
import com.jakisniekot.powerGrid.lang.LangFileHandler;
import com.jakisniekot.powerGrid.listener.BlockPlaceListener;
import com.jakisniekot.powerGrid.util.ConsoleColors;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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

        //MOTD
        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{version}", plugin.getPluginMeta().getVersion());
        placeholder.put("{fileHandlerStatus}", database.getStatus());
        printLogs("info.startup", placeholder);

        //PluginManager
        PluginManager pm = plugin.getServer().getPluginManager();

        //Items
        items = new ItemParser(plugin).parseItemsFromConfig();

        //Commands
        getCommand("powergrid").setExecutor(new mainCommandExecutor(plugin));
        getCommand("powergrid").setTabCompleter(new mainCommandTabber(plugin));

        //Listeners
        pm.registerEvents(new BlockPlaceListener(plugin), this);



        //Tests
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
            object = getLang().getString(messageID);
        } else {
            object = getLang().getStringList(messageID);
        }

        if (object instanceof String s) {
            plugin.getLogger().log(Level.INFO, ConsoleColors.colorize(s));
        } else {
            assert object != null;
            for (String line : (List<String>) object) {
                plugin.getLogger().log(Level.INFO, ConsoleColors.colorize(line));

            }
        }
    }

    public void printLogs(String messageID, Map<String, String> placeholders) {

        Object object;
        if (getLang().getStringList(messageID).isEmpty()) {
            object = getLang().getString(messageID);
        } else {
            object = getLang().getStringList(messageID);
        }

        if (object == null)  {
            plugin.getLogger().log(Level.INFO, ("Missing message: "+messageID));
            return;
        }

        if (object instanceof String s) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                s = s.replace(entry.getKey(), entry.getValue());
            }
            plugin.getLogger().log(Level.INFO, ConsoleColors.colorize(s));
        } else {
            for (String line : (List<String>) object) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }
                plugin.getLogger().log(Level.INFO, ConsoleColors.colorize(line));

            }
        }
    }

    public String stringReplacer(String text, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        return text;
    }

    public List<String> listReplacer(List<String> list, Map<String, String> placeholders) {
        List<String> newList = new ArrayList<>();

        for (String text : list) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue());
            }

            newList.add(ConsoleColors.colorize(text));
        }


        return newList;
    }

    public Connection getConnection() {
        return database.getConnection();
    }

    public LangFileHandler getLangFileHandler() {
        return langFileHandler;
    }

    public ItemFileHandler getItemFileHandler() {
        return itemFileHandler;
    }

    public List<String> getItemsList() {
        return new ArrayList<>(items.keySet());
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
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public Component colorizerLegacy(String text, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public Component getMessage(String key, Map<String, String> placeholders) {
        String msg = getLang().getString(key, "Missing message: " + key);
        return colorizerLegacy(msg, placeholders);
    }

    public Component getMessage(String key) {
        String msg = getLang().getString(key, "Missing message: " + key);
        return colorizerLegacy(msg);
    }


}
