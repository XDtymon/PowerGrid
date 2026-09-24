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

    private Map<Character, Material> keys = new HashMap<>();
    private List<List<List<Material>>> structure = new ArrayList<>();

    public Multiblock (
            ConfigurationSection section
    ) {

        ConfigurationSection keys = section.getConfigurationSection("multiblock.keys");

        Material controllerMaterial;
        boolean controllerUsed = false;

        try {
            controllerMaterial = Material.valueOf(section.getString("item.material"));
        } catch (IllegalArgumentException e) {
            controllerMaterial = Material.STONE;
        }

        if (!controllerMaterial.isBlock()) {
            controllerMaterial = Material.STONE;
        }

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

        ConfigurationSection structure = section.getConfigurationSection("multiblock.structure");

        List<List<List<Material>>> structureList = new ArrayList<>();

        for (String layer : structure.getKeys(false)) {

            List<List<Material>> newLayer = new ArrayList<>();

            for (String line : structure.getStringList(layer)) {

                List<Material> newLine = new ArrayList<>();

                for (char c : line.toCharArray()) {

                    if (c == ' ') {
                        newLine.add(Material.AIR);
                        continue;
                    } else if (c == '@') {
                        if (controllerUsed) {
                            newLine.add(Material.STONE);
                        } else {
                            newLine.add(controllerMaterial);
                            controllerUsed = true;
                        }
                    } else {
                        Material material = this.keys.get(c);
                        if (material == null) {
                            material = Material.AIR;
                        }
                        newLine.add(material);
                    }
                }

                newLayer.add(newLine);
            }

            structureList.add(newLayer);
        }

        this.structure = structureList;
    }

    public List<List<List<Material>>> getStructure() {
        return structure;
    }
}
