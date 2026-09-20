package com.jakisniekot.powerGrid.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackString {

    public static List<ItemStack> getList(String listString) {
        List<ItemStack> list = new ArrayList<>();
        for (String string : listString.split("\\|")) {
            try {
                list.add(new ItemStack(Material.valueOf(string)));
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        if (list.isEmpty()) list.add(new ItemStack(Material.STONE));

        return list;
    }

    public static List<ItemStack> getList(List<String> strings) {
        List<ItemStack> list = new ArrayList<>();
        for (String string : strings) {
            try {
                list.add(new ItemStack(Material.valueOf(string)));
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        if (list.isEmpty()) list.add(new ItemStack(Material.STONE));

        return list;
    }

    public static String getString(List<ItemStack> list) {
        StringBuilder sb = new StringBuilder();
        for (ItemStack item : list) {
            sb.append(item.getType());
        }

        return sb.toString();
    }


}
