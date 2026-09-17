package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockPlaceListener implements Listener {

    private MainPlugin plugin;

    public BlockPlaceListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listener(PlayerInteractEvent event) {
        if (plugin.getConnection() == null) {

            return;
        }

        ItemStack itemStack = event.getItem();
        if (itemStack == null) {

            return;
        }

        if (!itemStack.hasItemMeta()) {

            return;
        }

        if (!itemStack.getItemMeta().getPersistentDataContainer().has(KeyUtil.TypeKey())) {

            return;
        }

        PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();

        switch (pdc.get(KeyUtil.TypeKey(), PersistentDataType.STRING)) {
            case "CONNECTOR":
                break;
            case "RELAY":
                break;
            case "WIRE":
                break;



            case null:
                break;
            default:
                break;
        }
    }
}
