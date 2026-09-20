package com.jakisniekot.powerGrid.machine.multiblock;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.machine.MachineFileHandler;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Multiblock {

    private MainPlugin plugin;

    private Map<Character, Material> keys;
    private List<List<List<Material>>> structure;

    public Multiblock (
            MainPlugin plugin,
            String id
    ) {
        MachineFileHandler machineFileHandler = plugin.getMachineFileHandler();
        FileConfiguration machines = machineFileHandler.getMachineTypesConfig();

        ConfigurationSection keys = machines.getConfigurationSection("machines." + id + ".multiblock.keys");
        for (String key : keys.getKeys(false)) {
            Material material;

            try {
                material = Material.valueOf(keys.getString(key));
            } catch (IllegalArgumentException e) {
                material = Material.AIR;
            }

            char c = key.charAt(0);

            this.keys.put(c, material);
        }

        ConfigurationSection structure = machines.getConfigurationSection("machines." + id + ".multiblock.structure");

        List<List<List<Material>>> structureList = new ArrayList<>();

        for (String layer : structure.getKeys(false)) {

            List<List<Material>> newLayer = new ArrayList<>();

            for (String line : structure.getStringList(layer)) {

                List<Material> newLine = new ArrayList<>();

                for (char c : line.toCharArray()) {

                    if (c == ' ') {
                        newLine.add(Material.AIR);
                        continue;
                    } else {
                        try {
                            Material material = this.keys.get(c);
                            newLine.add(material);
                            continue;
                        } catch (NullPointerException _) {
                            newLine.add(Material.AIR);
                        }
                    }
                }

                newLayer.add(newLine);
            }

            structureList.add(newLayer);
        }

        this.structure = structureList;
    }
}
