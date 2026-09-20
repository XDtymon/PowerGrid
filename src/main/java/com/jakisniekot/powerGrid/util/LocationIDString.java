package com.jakisniekot.powerGrid.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationIDString {

    public static Location getLocation(String string) {
        String[] strings = string.split("\\|");

        World world;

        int x;
        int y;
        int z;

        try {
            world = Bukkit.getWorld(strings[0]);

            x = Integer.parseInt(strings[1]);
            y = Integer.parseInt(strings[2]);
            z = Integer.parseInt(strings[3]);
        } catch (NumberFormatException e) {
            return null;
        }

        return new Location(world, x, y, z);
    }

    public static String getString(Location location) {
        return location.getWorld().getName() + "|" +location.getBlockX() + "|" + location.getBlockY() + "|" + location.getBlockZ();
    }
}
