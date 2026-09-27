package com.jakisniekot.powerGrid.sound;

import com.jakisniekot.powerGrid.MainPlugin;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SoundFileHandler {

    private File langFile;
    private FileConfiguration langConfig;
    private Map<String, Sound> soundMap = new HashMap<>();

    private MainPlugin plugin;

    public SoundFileHandler(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupMachineFiles() {
        langFile = new File(plugin.getDataFolder(), "sound.yml");

        if (!langFile.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("sound.yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        InputStream defStream = plugin.getResource("sound.yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getSoundConfig() {
        return langConfig;
    }

    public void parseSounds() {
        Set<String> stringList = getSoundConfig().getKeys(false);
        if (stringList.isEmpty()) return;

        for (String id : stringList)  {
            ConfigurationSection configurationSection = getSoundConfig().getConfigurationSection(id);
            if (configurationSection == null) continue;
            String soundPlayType = configurationSection.getString("playType");
            String soundTypeString = configurationSection.getString("type");
            String soundCategoryString = configurationSection.getString("category");
            float soundVolume = Float.parseFloat(configurationSection.getString("volume"));
            float soundPitch = Float.parseFloat(configurationSection.getString("pitch"));

            PlaySoundType playSoundType = null;
            org.bukkit.Sound soundType = null;
            SoundCategory soundCategory = null;

            if (NamespacedKey.fromString(soundTypeString) == null) continue;

            soundType = Registry.SOUNDS.get(Key.key(soundTypeString));
            try {
                playSoundType = PlaySoundType.valueOf(soundPlayType);
            } catch (IllegalArgumentException e) {

                continue;
            }

            try {
                soundCategory = SoundCategory.valueOf(soundCategoryString);
            } catch (IllegalArgumentException e) {

                soundCategory = SoundCategory.MASTER;
                //continue;
            }


            if (soundVolume == 0.0) {

                soundVolume = 1.0f;
            }

            if (soundPitch == 0.0) {

                soundPitch = 1.0f;
            }

            if (soundType == null) {

                continue;
            }

            Sound sound = new Sound(
                    playSoundType,
                    soundType,
                    soundCategory,
                    soundVolume,
                    soundPitch
            );


            this.soundMap.put(id, sound);
        }
    }

    public void playSound(String id, Location playSoundLocation, Player player) {
        Sound sound = soundMap.get(id);
        PlaySoundType playSoundType = sound.getPlaySoundType();
        if (player == null) playSoundType = PlaySoundType.WORLD;
        if (playSoundLocation == null) playSoundType = PlaySoundType.PLAYER;
        if (player == null && playSoundLocation == null) {

            return;
        }

        if (playSoundType == PlaySoundType.WORLD) {
            playSoundLocation.getWorld().playSound(
                    playSoundLocation,
                    sound.getSoundType(),
                    sound.getSoundCategory(),
                    sound.getVolume(),
                    sound.getPitch()
            );
        }
    }
}