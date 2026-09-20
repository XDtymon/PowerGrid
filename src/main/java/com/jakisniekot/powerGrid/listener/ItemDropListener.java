package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.particles.WireConnectionRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {
    private MainPlugin plugin;

    public ItemDropListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listener(PlayerDropItemEvent event) {
        if (WireConnectionRunnable.isActive(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
