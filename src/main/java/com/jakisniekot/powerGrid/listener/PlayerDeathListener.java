package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.parse.particles.WireConnectionRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    private MainPlugin plugin;

    public PlayerDeathListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listener(PlayerDeathEvent event) {
        if (WireConnectionRunnable.isActive(event.getPlayer().getUniqueId())) {
            plugin.getWireActions().cancelConnection(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
        }
    }
}
