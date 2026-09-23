package com.jakisniekot.powerGrid.machine;

import com.jakisniekot.powerGrid.MainPlugin;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;

public class ItemStorage {
    private Map<String, ItemSlot> storage;

    public void getFromConfig(ConfigurationSection section, MainPlugin plugin) {
        for (String slotName : section.getKeys(false)) {
            ConfigurationSection slotSection = section.getConfigurationSection(slotName);
            if (slotSection == null) {

                //ERROR
                continue;
            }

            int maxAmount = 64;
            if (slotSection.getInt("maxAmount") != 0) maxAmount = slotSection.getInt("maxAmount");

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
                            new ItemStack(Material.AIR),
                            maxAmount,
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

