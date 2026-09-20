package com.jakisniekot.powerGrid.actions.wire;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.path.ConnectionPath;
import com.jakisniekot.powerGrid.util.ItemStackString;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class WireLine {
    private MainPlugin plugin;
    private final Location one;
    private final Location two;
    private final List<ItemStack> blocks;

    public WireLine(MainPlugin plugin, Location one, Location two, String blocks) {
        this.plugin = plugin;
        this.one = one;
        this.two = two;
        this.blocks = ItemStackString.getList(blocks);
    }

    public void place() {
        Location[] locations = ConnectionPath.getLocations(
                one,
                two,
                (int) Math.round(one.distance(two) * 2.2)
        );


        Location[] newLocations = Arrays.copyOfRange(locations, 1, locations.length - 1);

        int b = 0;

        for (int i = 0; i < newLocations.length; i++) {
            ItemStack helmet = blocks.get(b);
            new WireStand(plugin, newLocations[i], helmet).place(one, two);
            b++;
            if (b >= blocks.size()) b = 0;
        }
    }
}
