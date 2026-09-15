package com.jakisniekot.powerGrid.item;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ItemFileHandler {

    private File itemFile;
    private FileConfiguration itemConfig;

    private MainPlugin plugin;

    public ItemFileHandler(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupItemFiles() {
        String path = "items.yml";
        itemFile = new File(plugin.getDataFolder(), path);

        if (!itemFile.exists()) {
            itemFile.getParentFile().mkdirs();
            plugin.saveResource(path, false);
        }

        itemConfig = YamlConfiguration.loadConfiguration(itemFile);

        InputStream defStream = plugin.getResource(path);
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            itemConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getItemConfig() {
        return itemConfig;
    }
}
