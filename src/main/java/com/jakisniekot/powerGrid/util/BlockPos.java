package com.jakisniekot.powerGrid.util;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public record BlockPos(World world, int x, int y, int z) {
    public long chunkKey() {
        return ((long) (x >> 4) & 0xFFFFFFFFL) | (((long) (z >> 4) & 0xFFFFFFFFL) << 32);
    }
}