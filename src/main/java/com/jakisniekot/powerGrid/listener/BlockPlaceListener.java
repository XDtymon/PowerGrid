package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private MainPlugin plugin;

    public BlockPlaceListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listener(BlockPlaceEvent event) {
        return;
    }
}
