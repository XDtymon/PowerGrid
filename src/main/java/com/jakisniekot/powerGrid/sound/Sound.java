package com.jakisniekot.powerGrid.sound;

import org.bukkit.SoundCategory;

public class Sound {

    private PlaySoundType playSoundType;
    private org.bukkit.Sound soundType;
    private SoundCategory soundCategory;
    private float volume;
    private float pitch;

    public Sound(
            PlaySoundType playSoundType,
            org.bukkit.Sound soundType,
            SoundCategory soundCategory,
            float soundVolume,
            float soundPitch
    ) {
        this.playSoundType = playSoundType;
        this.soundType = soundType;
        this.soundCategory = soundCategory;
        this.volume = soundVolume;
        this.pitch = soundPitch;
    }

    public float getPitch() {
        return pitch;
    }

    public float getVolume() {
        return volume;
    }

    public PlaySoundType getPlaySoundType() {
        return playSoundType;
    }

    public SoundCategory getSoundCategory() {
        return soundCategory;
    }

    public org.bukkit.Sound getSoundType() {
        return soundType;
    }

    public void setPlaySoundType(PlaySoundType playSoundType) {
        this.playSoundType = playSoundType;
    }
}
