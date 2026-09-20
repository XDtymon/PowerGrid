package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.particles.WireConnectionRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

public class InventoryClickListener implements Listener {

    private MainPlugin plugin;

    public InventoryClickListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listener(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof PlayerInventory)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (WireConnectionRunnable.isActive(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
