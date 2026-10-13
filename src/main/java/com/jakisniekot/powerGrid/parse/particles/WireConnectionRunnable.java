package com.jakisniekot.powerGrid.parse.particles;

import com.jakisniekot.powerGrid.util.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.util.LocationIDString;
import com.jakisniekot.powerGrid.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WireConnectionRunnable extends BukkitRunnable {




    private MainPlugin plugin;
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static double MAX_DISTANCE = 32;
    private static final float PARTICLE_PER_BLOCK = 0.5f; // co ile bloków cząsteczka
    private static String PARTICLE_STRING = "REDSTONE|#FF0000|1.2"; // Twój format

    public WireConnectionRunnable(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public static void addActive(UUID uuid) {
        activePlayers.add(uuid);
    }

    public static void removeActive(UUID uuid) {
        activePlayers.remove(uuid);
    }

    public static Set<UUID> getActivePlayers() {
        return activePlayers;
    }

    public static boolean isActive(UUID playerUUID) {
        return activePlayers.contains(playerUUID);
    }

    @Override
    public void run() {
        for (UUID playerUUID : activePlayers) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) continue;

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getItemMeta() == null) continue;

            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();

            if (pdc.get(KeyUtil.MaxConnectionDistanceKey(), PersistentDataType.INTEGER) != null) {
                MAX_DISTANCE = pdc.get(KeyUtil.MaxConnectionDistanceKey(), PersistentDataType.INTEGER);
            }

            String locString = pdc.get(KeyUtil.WireConnectionKey(), PersistentDataType.STRING);
            Location connectorLoc = LocationIDString.getLocation(locString);
            if (connectorLoc == null) continue;

            connectorLoc.add(0.5, 0.5, 0.5);

            Location target = LocationUtil.getTargetLocation(player);

            if (connectorLoc.distance(target) < MAX_DISTANCE) {
                PARTICLE_STRING = pdc.get(KeyUtil.WireParticleKey(), PersistentDataType.STRING);
            } else if (connectorLoc.distance(target) > 128) {
                plugin.getWireActions().cancelConnection(player, player.getInventory().getItemInMainHand());
            } else {
                PARTICLE_STRING = "REDSTONE|#FF0000|0.5";
            }

            double distance = Math.round(connectorLoc.distance(target) * 10.0) / 10.0;


            Map<String, String> placeholder = new HashMap<>();
            placeholder.put("{wireDisplayName}", item.getItemMeta().getDisplayName());
            placeholder.put("{distance}", String.valueOf(distance));
            placeholder.put("{max_distance}", String.valueOf(MAX_DISTANCE));
            plugin.getPlayerUtility().actionbar(player, plugin.lang().getString("wire.actionbar.main"), placeholder);

            WireParticles.spawnParticles(connectorLoc, target, PARTICLE_PER_BLOCK, PARTICLE_STRING);
        }
    }
}
