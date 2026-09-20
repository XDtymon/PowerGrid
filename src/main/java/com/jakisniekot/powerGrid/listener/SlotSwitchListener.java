package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.particles.WireConnectionRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class SlotSwitchListener implements Listener {

    private MainPlugin plugin;

    public SlotSwitchListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listener(PlayerItemHeldEvent event) {
        if (WireConnectionRunnable.isActive(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
