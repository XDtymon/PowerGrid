package com.jakisniekot.powerGrid.particles;

import com.jakisniekot.powerGrid.path.ConnectionPath;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class WireParticles {
    public static void spawnParticles(Location loc1, Location loc2, float particlePerBlock, String particleString) {
        double distance = loc2.distance(loc1);
        Particle particle = null;

        Location[] locations = ConnectionPath.getLocations(loc1, loc2, (int) Math.round(distance / particlePerBlock));

        if (particleString.contains("|")) {
            String[] strings = particleString.split("\\|");

            try {
                particle = Particle.valueOf(strings[0]);
            } catch (IllegalArgumentException e) {
                return;
            }

            Particle.DustOptions dustOptions = null;

            if (strings.length == 2) {

                dustOptions = new Particle.DustOptions(
                        fromHex(strings[1]),
                        1
                );

            } else if (strings.length == 3) {
                float size = 1;

                try {
                    size = Float.parseFloat(strings[2]);
                } catch (NumberFormatException _) {

                }


                dustOptions = new Particle.DustOptions(
                        fromHex(strings[1]),
                        size
                );

            } else {

            }

            for (Location location : locations) {
                location.getWorld().spawnParticle(
                        particle,
                        location,
                        1,
                        0, 0, 0,
                        0,
                        dustOptions
                );
            }

        } else {
            try {
                particle = Particle.valueOf(particleString);
            } catch (IllegalArgumentException e) {
                return;
            }

            for (Location location : locations) {
                location.getWorld().spawnParticle(
                        particle,
                        location,
                        1,
                        0, 0, 0,
                        0
                );
            }
        }


    }

    public static Color fromHex(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return Color.fromRGB(r, g, b);
    }
}
