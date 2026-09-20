package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.particles.WireConnectionRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportationListener implements Listener {
    private MainPlugin plugin;

    public PlayerTeleportationListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listener(PlayerTeleportEvent event) {
        if (WireConnectionRunnable.isActive(event.getPlayer().getUniqueId())) {
            plugin.getWireActions().cancelConnection(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
        }
    }
}
