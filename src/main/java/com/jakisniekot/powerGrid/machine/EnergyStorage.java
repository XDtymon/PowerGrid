package com.jakisniekot.powerGrid.machine;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.io.ObjectInputFilter;
import java.util.List;
import java.util.Map;

public class EnergyStorage {
    private double energy;
    private double maxEnergy;
    private double maxInputTransfer;
    private double maxOutputTransfer;
    private List<Integer> energyDisplayItems;

    public void getFromConfig(ConfigurationSection section, MainPlugin plugin) {
        this.energy = 0;

        if (section.getInt("maximumEnergy") != 0) {
            this.maxEnergy = section.getInt("maximumEnergy");
        } else {
            this.maxEnergy = 1000;
        }

        if (section.getInt("maxInputTransfer") != 0) {
            this.maxEnergy = section.getInt("maxInputTransfer");
        } else {
            this.maxInputTransfer = 1000;
        }

        if (section.getInt("maxOutputTransfer") != 0) {
            this.maxEnergy = section.getInt("maxOutputTransfer");
        } else {
            this.maxOutputTransfer = 1000;
        }

        this.energyDisplayItems = section.getIntegerList("slots");
    }

    public boolean hasEnergy(Double energy) {
        return energy <= this.energy;
    }

    public void useEnergy(Double energy) {
        this.energy -= energy;
    }

    public void generateEnergy(Double energy) {
        this.energy += energy;
    }

    public double getEnergy() {
        return energy;
    }

    public double getMaxEnergy() {
        return maxEnergy;
    }

    public double getMaxInputTransfer() {
        return maxInputTransfer;
    }

    public double getMaxOutputTransfer() {
        return maxOutputTransfer;
    }

    public List<Integer> getEnergyDisplayItems() {
        return energyDisplayItems;
    }
}