package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.particles.WireConnectionRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private MainPlugin plugin;

    public PlayerQuitListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listener(PlayerQuitEvent event) {
        if (WireConnectionRunnable.isActive(event.getPlayer().getUniqueId())) {
            plugin.getWireActions().cancelConnection(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
        }
    }
}
