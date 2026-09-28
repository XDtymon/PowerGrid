package com.jakisniekot.powerGrid.util;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemStringUtil {
    public ItemStack item(String itemString, MainPlugin plugin) {
       if (itemString.startsWith("ITEM(") && itemString.endsWith(")")) {
            List<String> list = List.of(itemString.split("\\|"));
            Material material;
            int amount;

            if (list.size() == 1) {

                try {
                    material = Material.valueOf(list.getFirst());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    return new ItemStack(Material.AIR);
                }


                ItemStack itemStack = new ItemStack(material);
                return itemStack;

            } else if (list.size() == 2) {

                try {
                    material = Material.valueOf(list.getFirst());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    return new ItemStack(Material.AIR);
                }

                list.removeFirst();

                try {
                    amount = Integer.parseInt(list.getFirst());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return new ItemStack(Material.AIR);
                }

                ItemStack itemStack = new ItemStack(material);
                itemStack.setAmount(amount);
                return itemStack;
            }


       } else if (itemString.startsWith("ITEMID(") && itemString.endsWith(")")) {
           String id = itemString.replace("ITEMID(", "").replace(")", "");
           ItemStack itemStack = plugin.getItemFromID(id);
           if (itemStack == null) return new ItemStack(Material.AIR);
           return itemStack;
       } else {

            return new ItemStack(Material.AIR);
       }

        return new ItemStack(Material.AIR);
    }
}
