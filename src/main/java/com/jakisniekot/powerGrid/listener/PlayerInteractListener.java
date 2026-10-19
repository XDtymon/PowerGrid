package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.power.machine.gui.GuiConfig;
import com.jakisniekot.powerGrid.power.machine.gui.MachineGui;
import com.jakisniekot.powerGrid.power.machine.tile.MachineTile;
import com.jakisniekot.powerGrid.util.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.power.connector.ConnectorTypes;
import com.jakisniekot.powerGrid.power.connector.Connector;
import com.jakisniekot.powerGrid.parse.particles.WireConnectionRunnable;
import org.bukkit.Location;
import org.bukkit.block.Block;
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
import java.util.Map;

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

        Block block = event.getClickedBlock();
        Location location = null;
        if (block != null) location = block.getLocation();

        if (location != null && plugin.getMachineTileHandler().isMachineTile(location)) {
            MachineGui.open(
                    event.getPlayer(),
                    plugin.getMachineTileHandler().getMachineTile(location),
                    plugin.getGuiConfig(),
                    plugin.getLogger()
            );
            event.setCancelled(true);
        }


        switch (pdc.get(KeyUtil.TypeKey(), PersistentDataType.STRING)) {
            case "WIRE":
                if (!event.getPlayer().isSneaking()) {
                    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                        return;
                    }

                    if (event.getClickedBlock() == null) return;
                    if (!plugin.getConnectorHandler().isConnector(event.getClickedBlock().getLocation())) return;
                    Connector connector = plugin.getConnectorHandler().getConnector(event.getClickedBlock().getLocation());

                    if (connector == null) {
                        event.setCancelled(true);
                        return;
                    }

                    String wireType = event.getItem().getItemMeta().getPersistentDataContainer().get(KeyUtil.ItemIDKey(), PersistentDataType.STRING);
                    List<String> allowedWireTypes = connector.allowedWires();
                    boolean blacklist = connector.blacklist();

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

                    plugin.getWireActions().addLocation(
                            event.getPlayer(),
                            event.getClickedBlock().getLocation(),
                            event.getItem()
                    );

                } else {
                    if (WireConnectionRunnable.isActive(event.getPlayer().getUniqueId())) {
                        plugin.getPlayerUtility().actionbar(event.getPlayer(), plugin.lang().getString("wire.actionbar.cancel"), new HashMap<>());
                        plugin.getWireActions().cancelConnection(event.getPlayer(), event.getItem());
                    }

                }

                break;

            case "CONNECTOR_WRENCH":
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                Connector connector = plugin.getConnectorHandler().getConnector(event.getClickedBlock().getLocation());

                plugin.getSoundFileHandler().playSound("wrenchUse", event.getClickedBlock().getLocation(), event.getPlayer());

                if (connector != null) {
                    ConnectorTypes newType = connector.connectorTypes().next(); // STATIC -> PUSH -> PULL -> STATIC
                    connector.nextConnectorType();
                    switch (newType) {
                        case PULL:
                            plugin.getPlayerUtility().actionbar(event.getPlayer(), plugin.lang().getString("utilItems.connectorWrench.pull"), new HashMap<>());
                            break;
                        case PUSH:
                            plugin.getPlayerUtility().actionbar(event.getPlayer(), plugin.lang().getString("utilItems.connectorWrench.push"), new HashMap<>());
                            break;
                        case STATIC:
                            plugin.getPlayerUtility().actionbar(event.getPlayer(), plugin.lang().getString("utilItems.connectorWrench.static"), new HashMap<>());
                            break;
                    }

                }

                break;

            case "MULTIMETER":
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                if (event.getClickedBlock() == null) return;

                if (plugin.getConnectorHandler().isConnector(location)) {
                    connector = plugin.getConnectorHandler().getConnector(location);
                    ConnectorTypes newType = connector.connectorTypes();
                    switch (newType) {
                        case PULL:
                            plugin.getPlayerUtility().actionbar(event.getPlayer(), plugin.lang().getString("utilItems.connectorWrench.pull"), new HashMap<>());
                            break;
                        case PUSH:
                            plugin.getPlayerUtility().actionbar(event.getPlayer(), plugin.lang().getString("utilItems.connectorWrench.push"), new HashMap<>());
                            break;
                        case STATIC:
                            plugin.getPlayerUtility().actionbar(event.getPlayer(), plugin.lang().getString("utilItems.connectorWrench.static"), new HashMap<>());
                            break;
                    }

                    return;
                }

                if (plugin.getMachineTileHandler().isMachineTile(location)) {

                    MachineTile machineTile = plugin.getMachineTileHandler().getMachineTile(location);

                    long maxEnergy = machineTile.energyStack.getMaxEnergy();

                    plugin.getSoundFileHandler().playSound("multimeterUse", location, event.getPlayer());
                    if (maxEnergy == 0) {
                        plugin.getPlayerUtility().actionbar(event.getPlayer(), plugin.lang().getString("utilItems.multimeter.notAnEnergyBlock"), new HashMap<>());
                        return;
                    }

                    long energy = machineTile.energyStack.getEnergy();
                    long input = machineTile.energyStack.getMaxInputTransfer();
                    long output = machineTile.energyStack.getMaxOutputTransfer();

                    Map<String, String> placeholder = new HashMap<>();
                    placeholder.put("{energy}", String.valueOf(energy));
                    placeholder.put("{maxEnergy}", String.valueOf(maxEnergy));
                    placeholder.put("{input}", String.valueOf(input));
                    placeholder.put("{output}", String.valueOf(output));

                    plugin.getPlayerUtility().actionbar(event.getPlayer(), plugin.lang().getString("utilItems.multimeter.success"), placeholder);

                }


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

        Location location = eye.clone().add(direction.multiply(MAX_AIM_DISTANCE));
        return new Location(
                location.getWorld(),
                location.getBlockX() - 0.5,
                location.getBlockY() - 0.5,
                location.getBlockZ() - 0.5
        );
    }
}
