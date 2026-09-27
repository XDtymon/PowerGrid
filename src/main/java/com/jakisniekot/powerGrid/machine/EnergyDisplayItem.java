package com.jakisniekot.powerGrid.machine;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.item.ItemParser;
import org.bukkit.inventory.ItemStack;

public class EnergyDisplayItem {
    private MainPlugin plugin;

    private ItemStack itemStack;

    public EnergyDisplayItem() {

    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
