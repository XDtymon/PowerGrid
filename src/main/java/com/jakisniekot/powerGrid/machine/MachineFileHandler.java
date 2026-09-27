package com.jakisniekot.powerGrid.machine;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MachineFileHandler {

    private File langFile;
    private FileConfiguration langConfig;

    private MainPlugin plugin;

    public MachineFileHandler(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupMachineFiles() {
        langFile = new File(plugin.getDataFolder(), "machines.yml");

        if (!langFile.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("machines.yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        InputStream defStream = plugin.getResource("machines.yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getMachineTypesConfig() {
        return langConfig;
    }

}