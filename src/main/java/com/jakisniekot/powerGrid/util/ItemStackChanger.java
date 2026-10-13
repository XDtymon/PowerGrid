package com.jakisniekot.powerGrid.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemStackChanger {

    public static ItemStack[] getItemsFromByte(byte[] items) {
        return ItemStack.deserializeItemsFromBytes(items);
    }

    public static ItemStack[] getItemsFromBlob(Blob blob) throws SQLException {
        byte[] bytes = blob.getBytes(1, (int) blob.length());
        blob.free();
        return getItemsFromByte(bytes);
    }

    public static byte[] getBlobFromItems(ItemStack[] items) {
        return ItemStack.serializeItemsAsBytes(items);
    }

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
