package com.jakisniekot.powerGrid.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class LocationUtil {

    private static final double MAX_AIM_DISTANCE = 3;

    public static Location getTargetLocation(Player player) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();

        RayTraceResult result = player.getWorld().rayTraceBlocks(eye, direction, MAX_AIM_DISTANCE);

        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
        }

        return eye.clone().add(direction.multiply(MAX_AIM_DISTANCE));
    }
}
