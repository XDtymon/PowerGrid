package com.jakisniekot.powerGrid.path;

import org.bukkit.Location;
import org.bukkit.World;

public class ConnectionPath {

    /*
    private MainPlugin plugin;

    public ConnectionPath(MainPlugin plugin) {
        this.plugin = plugin;
    }
     */


    public static Location[] getLocations(Location start, Location end, int amount) {

        if (start.getWorld() == null || end.getWorld() == null) {
            throw new IllegalArgumentException("Lokacje muszą mieć przypisany świat");
        }

        if (!start.getWorld().equals(end.getWorld())) {
            throw new IllegalArgumentException("Lokacje muszą być w tym samym świecie");
        }

        Location[] locations = new Location[amount];
        World world = start.getWorld();

        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double dz = end.getZ() - start.getZ();

        float dyaw = end.getYaw() - start.getYaw();
        float dpitch = end.getPitch() - start.getPitch();

        for (int i = 0; i < amount; i++) {
            double t = (double) i / (amount - 1); // od 0.0 do 1.0

            double x = start.getX() + dx * t;
            double y = start.getY() + dy * t;
            double z = start.getZ() + dz * t;

            float yaw = start.getYaw() + dyaw * (float) t;
            float pitch = start.getPitch() + dpitch * (float) t;

            locations[i] = new Location(world, x, y, z, yaw, pitch);
        }

        return locations;
    }
}
