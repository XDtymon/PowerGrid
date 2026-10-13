package com.jakisniekot.powerGrid.parse.sounds;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.parse.exceptions.SoundParseError;
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

public class SoundHandler {

    private File langFile;
    private FileConfiguration langConfig;
    private Map<String, Sound> soundMap = new HashMap<>();

    private MainPlugin plugin;

    public SoundHandler(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupSoundFile() {
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

            if (NamespacedKey.fromString(soundTypeString) == null) {
                plugin.errorHandler().print(SoundParseError.NO_SOUND_TYPE, id);
                continue;
            }

            soundType = Registry.SOUNDS.get(Key.key(soundTypeString));

            try {
                playSoundType = PlaySoundType.valueOf(soundPlayType);
            } catch (IllegalArgumentException e) {
                plugin.errorHandler().print(SoundParseError.NO_PLAY_TYPE, id);
                playSoundType = PlaySoundType.WORLD;
            }

            try {
                soundCategory = SoundCategory.valueOf(soundCategoryString);
            } catch (IllegalArgumentException e) {
                plugin.errorHandler().print(SoundParseError.NO_CATEGORY, id);
                soundCategory = SoundCategory.MASTER;
            }


            if (soundVolume == 0.0) {
                plugin.errorHandler().print(SoundParseError.NO_VOLUME, id);
                soundVolume = 1.0f;
            }

            if (soundPitch == 0.0) {
                plugin.errorHandler().print(SoundParseError.NO_PITCH, id);
                soundPitch = 1.0f;
            }

            if (soundType == null) {
                plugin.errorHandler().print(SoundParseError.NO_SOUND_TYPE, id);
                continue;
            }

            Sound sound = new Sound(
                    playSoundType,
                    soundType,
                    soundCategory,
                    soundVolume,
                    soundPitch
            );

            plugin.errorHandler().print(SoundParseError.NONE, id);

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