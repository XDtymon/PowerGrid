package com.jakisniekot.powerGrid.actions.wire;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class WireStand {
    private final MainPlugin plugin;
    private final Location location;
    private final ItemStack head;

    public WireStand(MainPlugin plugin, Location location, ItemStack head) {
        this.plugin = plugin;
        this.location = location;
        this.head = head;
    }

    public void place(Location one, Location two) {
        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setInvulnerable(true);
        armorStand.setInvisible(true);
        armorStand.setGravity(false);
        armorStand.setSmall(true);

        Vector direction = one.toVector().subtract(two.toVector());

        double yawDegrees = Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        double horizontalDistance = Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ());
        double pitchDegrees = Math.toDegrees(Math.atan2(direction.getY(), horizontalDistance));

        PersistentDataContainer pdc = armorStand.getPersistentDataContainer();
        pdc.set(KeyUtil.FirstWireStandKey(), PersistentDataType.STRING, LocationIDString.getString(one));
        pdc.set(KeyUtil.SecondWireStandKey(), PersistentDataType.STRING, LocationIDString.getString(two));

        armorStand.getEquipment().setHelmet(head);

        armorStand.setHeadPose(new EulerAngle(
                Math.toRadians(-pitchDegrees),
                Math.toRadians(yawDegrees),
                0
        ));

        armorStand.teleport(new Location(
                armorStand.getWorld(),
                armorStand.getLocation().getX() + 0.5,
                armorStand.getLocation().getY() - 0.6,
                armorStand.getLocation().getZ() + 0.5,
                0,
                0
        ));
    }
}
