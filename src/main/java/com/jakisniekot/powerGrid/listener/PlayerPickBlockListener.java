package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.util.LocationIDString;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.eclipse.sisu.launch.Main;
import org.intellij.lang.annotations.JdkConstants;

public class PlayerPickBlockListener implements Listener {

    private MainPlugin plugin;

    public PlayerPickBlockListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listener(PlayerPickBlockEvent event) {
        if (plugin.getConnectorDAO().get(LocationIDString.getString(event.getBlock().getLocation())) != null) {
                event
                        .getPlayer()
                        .getInventory()
                        .setItemInMainHand(
                                plugin.getItemFromID(
                                    plugin.getConnectorDAO()
                                            .get(LocationIDString.getString(event.getBlock().getLocation())
                                            ).itemID()
                                )
                        );
        }
    }
}
