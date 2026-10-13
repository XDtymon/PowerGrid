package com.jakisniekot.powerGrid.util;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {
    public static ItemStack item(String id, ConfigurationSection section, MainPlugin plugin) {
        String itemString = "";

        if (section.getItemStack(id) != null) {
            return section.getItemStack(id);
        } else {
            itemString = section.getString(id);
            if (itemString == null) return  new ItemStack(Material.AIR);
        }

        if (itemString.startsWith("ITEM(") && itemString.endsWith(")")) {
            String inner = itemString.substring("ITEM(".length(), itemString.length() - 1);
            String[] parts = inner.split("\\|");

            Material material;
            try {
                material = Material.valueOf(parts[0].trim());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return new ItemStack(Material.AIR);
            }

            int amount = 1;
            if (parts.length >= 2) {
                try {
                    amount = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return new ItemStack(Material.AIR);
                }
            }

            return new ItemStack(material, amount);

        } else if (itemString.startsWith("ITEMID(") && itemString.endsWith(")")) {
            String inner = itemString.substring("ITEMID(".length(), itemString.length() - 1);
            String[] parts = inner.split("\\|");

            ItemStack itemStack = plugin.getItemFromID(parts[0].trim());
            if (itemStack == null) {
                return new ItemStack(Material.AIR);
            }

            if (parts.length >= 2) {
                try {
                    itemStack.setAmount(Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return new ItemStack(Material.AIR);
                }
            }

            return itemStack;
        }

        return new ItemStack(Material.AIR);
    }
}