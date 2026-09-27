package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.actions.connector.ConnectorTypes;
import com.jakisniekot.powerGrid.database.Connector;
import com.jakisniekot.powerGrid.network.EnergyBlockUtil;
import com.jakisniekot.powerGrid.particles.WireConnectionRunnable;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class PlayerInteractListener implements Listener {

    private MainPlugin plugin;
    private Connection connection;

    public PlayerInteractListener(MainPlugin plugin) {
        this.plugin = plugin;
        this.connection = plugin.getConnection();
    }



    @EventHandler
    public void listener(PlayerInteractEvent event) {
        if (plugin.getConnection() == null) {
            plugin.printLogs("errors.database.noConnection");
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
            case "WIRE":
                if (!event.getPlayer().isSneaking()) {
                    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                        return;
                    }

                    String locString = LocationIDString.getString(event.getInteractionPoint());
                    Connector connector = plugin.getConnectorDAO().get(locString);

                    if (connector == null) {
                        event.setCancelled(true);
                        return;
                    }

                    String wireType = event.getItem().getItemMeta().getPersistentDataContainer().get(KeyUtil.ItemIDKey(), PersistentDataType.STRING);
                    List<String> allowedWireTypes = connector.allowedWires();
                    boolean blacklist = plugin.getConnectorDAO().get(locString).blacklist();

                    if (blacklist) {
                        if (allowedWireTypes.contains(wireType)) {
                            return;
                        }
                    } else {
                        if (!allowedWireTypes.contains(wireType)) {
                            return;
                        }
                    }

                    //MAX CONNECTION STUFF

                    event.setCancelled(true);

                    plugin.getWireActions().addLocation(event.getPlayer(), getTargetLocation(event.getPlayer()), event.getItem());
                } else {
                    if (WireConnectionRunnable.isActive(event.getPlayer().getUniqueId())) {
                        plugin.getPlayerUtility().actionbar(event.getPlayer(), plugin.getLang().getString("wire.actionbar.cancel"), new HashMap<>());
                        plugin.getWireActions().cancelConnection(event.getPlayer(), event.getItem());
                    }

                }

                break;

            case "WIRE_WRENCH":
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                String id = LocationIDString.getString(event.getClickedBlock().getLocation());
                Connector connector = plugin.getConnectorDAO().get(id);

                if (connector != null) {
                    ConnectorTypes newType = connector.connectorTypes().next(); // STATIC -> PUSH -> PULL -> STATIC
                    plugin.getConnectorDAO().updateType(id, newType);
                    event.getPlayer().sendMessage("Connector: " + newType.name());
                }
                break;
            case "MULTIMETER":
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                String locationString = LocationIDString.getString(event.getClickedBlock().getLocation());
                EnergyBlockUtil energyBlockUtil = new EnergyBlockUtil();

                break;

            case null:
                break;
            default:
                break;
        }
    }

    private Location getTargetLocation(Player player) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();

        double MAX_AIM_DISTANCE = 3.0;
        RayTraceResult result = player.getWorld().rayTraceBlocks(eye, direction, MAX_AIM_DISTANCE);

        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
        }

        return eye.clone().add(direction.multiply(MAX_AIM_DISTANCE));
    }
}
