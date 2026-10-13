package com.jakisniekot.powerGrid.power.displays.wire;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.power.wire.Wire;
import com.jakisniekot.powerGrid.util.ConnectionPath;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WireDisplays {
    private MainPlugin plugin;


    public WireDisplays(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public void placeLine(Location one, Location two, List<Material> blocks) {
        Location[] locations = ConnectionPath.getLocations(
                one,
                two,
                (int) Math.round(one.distance(two) / 0.425)
        );


        Location[] newLocations = Arrays.copyOfRange(locations, 1, locations.length - 1);

        int b = 0;

        for (int i = 0; i < newLocations.length; i++) {
            ItemStack helmet = new ItemStack(blocks.get(b));
            new WireStand(plugin, newLocations[i], helmet).place(one, two);
            b++;
            if (b >= blocks.size()) b = 0;
        }
    }

    public void reinstateWires() {
        List<Wire> wires = plugin.getWireDAO().getAll();
        for (Wire wire : wires) {
            List<Material> list = new ArrayList<>();
            for (String item : wire.headItems().split("\\|")) {
                try {
                    list.add(Material.valueOf(item));
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }

            placeLine(
                    LocationIDString.getLocation(wire.loc1()),
                    LocationIDString.getLocation(wire.loc2()),
                    list
            );
        }
    }
}
