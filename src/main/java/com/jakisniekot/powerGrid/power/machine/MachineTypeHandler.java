package com.jakisniekot.powerGrid.power.machine;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.parse.exceptions.MachineParseError;
import com.jakisniekot.powerGrid.power.machine.tile.item.ItemSlot;
import com.jakisniekot.powerGrid.power.machine.tile.item.ItemSlotType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MachineTypeHandler {

    private File langFile;
    private FileConfiguration langConfig;

    private MainPlugin plugin;

    private Map<String, MachineType> machineTypes = new HashMap<>();

    public MachineTypeHandler(MainPlugin plugin) {
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

    public void parseMachines() {
        for (String id : plugin.getMachineTypeHandler().getMachineTypesConfig().getKeys(false)) {
            ConfigurationSection section = plugin.getMachineTypeHandler().getMachineTypesConfig().getConfigurationSection(id);
            parseMachine(section, id);
        }
    }

    public void parseMachine(ConfigurationSection machineSection, String id) {
        ConfigurationSection energyStorageSection = machineSection.getConfigurationSection("energyStorage");

        long energy = energyStorageSection.getLong("energy");
        long maxEnergy = energyStorageSection.getLong("maximumEnergy");
        long inputTransfer =energyStorageSection.getLong("maxInputTransfer");
        long outputTransfer = energyStorageSection.getLong("maxOutputTransfer");

        if (energy != 0) {
            plugin.errorHandler().print(MachineParseError.ENERGY_CUSTOM, id);
        }

        if (maxEnergy == 0) {
            plugin.errorHandler().print(MachineParseError.MAX_ENERGY_ERROR, id);
            maxEnergy = 10000;
        }

        if (inputTransfer == 0) {
            plugin.errorHandler().print(MachineParseError.INPUT_TRANSFER_ERROR, id);
            inputTransfer = 50;
        }

        if (outputTransfer == 0) {
            plugin.errorHandler().print(MachineParseError.OUTPUT_TRANSFER_ERROR, id);
            outputTransfer = 50;
        }

        EnergyStack energyStack = new EnergyStack(
            energy, maxEnergy, inputTransfer, outputTransfer
        );


        List<ItemSlot> inputs = new ArrayList<>();
        List<ItemSlot> outputs = new ArrayList<>();

        if (machineSection.getConfigurationSection("itemStorage") == null) {
            plugin.errorHandler().print(MachineParseError.ITEM_STORAGE_NULL, id);
        } else {

            ConfigurationSection itemStorageSection = machineSection.getConfigurationSection("itemStorage");

            Set<Integer> usedInvSlots = new HashSet<>();
            for (String slotID : itemStorageSection.getKeys(false)) {
                ConfigurationSection slotSection = itemStorageSection.getConfigurationSection(slotID);
                int invSlotID = slotSection.getInt("slotID");
                if (invSlotID == 0) {
                    plugin.errorHandler().print(MachineParseError.ITEM_SLOT_NO_ID, id);
                    return;
                } else if (usedInvSlots.contains(invSlotID)) {
                    plugin.errorHandler().print(MachineParseError.ITEM_SLOT_DUPED_ID, id);
                    return;
                }

                usedInvSlots.add(invSlotID);

                ItemSlotType itemSlotType = null;

                try {
                    itemSlotType = ItemSlotType.valueOf(slotSection.getString("itemSlotType"));
                } catch (IllegalArgumentException e) {
                    plugin.errorHandler().print(MachineParseError.NO_ITEM_SLOT_TYPE, id);
                    itemSlotType = ItemSlotType.INPUT;
                }


                ItemSlot itemSlot = new ItemSlot(
                        invSlotID, itemSlotType
                );

                switch (itemSlotType) {
                    case INPUT -> inputs.add(itemSlot);
                    case OUTPUT -> outputs.add(itemSlot);
                }

            }

        }

        if (inputs.isEmpty()) plugin.errorHandler().print(MachineParseError.NO_INPUTS, id);
        if (outputs.isEmpty()) plugin.errorHandler().print(MachineParseError.NO_OUTPUTS, id);

        MachineType machineType = new MachineType(
                id,
                energyStack,
                inputs,
                outputs

        );

        machineTypes.put(id, machineType);

        plugin.errorHandler().print(MachineParseError.NONE, id);
    }

    public Map<String, MachineType> getMachineTypes() {
        return machineTypes;
    }

    public MachineType getMachineType(String type) {
        return machineTypes.get(type);
    }

    public void cos() {
        String s = "as";
        try {
            Integer i = Integer.parseInt(s);

        } catch (NumberFormatException e) {

        }
    }
}