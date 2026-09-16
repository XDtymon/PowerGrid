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

import java.util.ArrayList;
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

        //Items
        items = new ItemParser(plugin).parseItemsFromConfig();

        //Commands
        getCommand("powergrid").setExecutor(new mainCommandExecutor(plugin));


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
            plugin.getLogger().log(Level.INFO, s);
        } else {
            assert object != null;
            for (String line : (List<String>) object) {
                plugin.getLogger().log(Level.INFO, line);

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
            plugin.getLogger().log(Level.INFO, s);
        } else {
            for (String line : (List<String>) object) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }
                plugin.getLogger().log(Level.INFO, line);

            }
        }


;
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
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public Component colorizerLegacy(String text, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
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
