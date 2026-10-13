package com.jakisniekot.powerGrid.power.machine.tile.item;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class ItemStorage {
    private Map<String, ItemSlot> storage;

    public void getFromConfig(ConfigurationSection section, MainPlugin plugin) {
        for (String slotName : section.getKeys(false)) {
            ConfigurationSection slotSection = section.getConfigurationSection(slotName);
            if (slotSection == null) {

                //ERROR
                continue;
            }

            int slotID;
            if (slotSection.getInt("slotID") != 0) {
                slotID = slotSection.getInt("slotID");
            } else {

                //ERROR
                continue;
            }

            ItemSlotType itemSlotType;

            try {
                itemSlotType = ItemSlotType.valueOf(slotSection.getString("itemSlotType"));
            } catch (IllegalArgumentException e) {

                //ERROR
                continue;
            }



            storage.put(
                    slotName,
                    new ItemSlot(
                            slotID,
                            itemSlotType
                    )
            );
        }
    }

    public Map<String, ItemSlot> getStorage() {
        return storage;
    }

    public ItemSlot getSlot(String slotID) {
        return storage.get(slotID);
    }
}

