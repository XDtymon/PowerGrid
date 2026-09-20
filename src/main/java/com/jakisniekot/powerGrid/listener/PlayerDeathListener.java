package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.actions.wire.WireItemActions;
import com.jakisniekot.powerGrid.particles.WireConnectionRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

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
