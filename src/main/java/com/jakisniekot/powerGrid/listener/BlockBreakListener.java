package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.power.connector.Connector;
import com.jakisniekot.powerGrid.power.connector.ConnectorDAO;
import com.jakisniekot.powerGrid.power.machine.MachineDAO;
import com.jakisniekot.powerGrid.power.wire.WireDAO;
import com.jakisniekot.powerGrid.power.machine.tile.MachineTile;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class BlockBreakListener implements Listener {

    private MainPlugin plugin;
    private ConnectorDAO connectorDAO;
    private MachineDAO machineDAO;
    private WireDAO wireDAO;

    public BlockBreakListener(MainPlugin plugin) {
        this.plugin = plugin;
        this.wireDAO = plugin.getWireDAO();
        this.connectorDAO = plugin.getConnectorDAO();
        this.machineDAO = plugin.getMachineDAO();
    }

    @EventHandler
    public void listener(BlockBreakEvent event) {

        if (plugin.getConnectorHandler().isConnector(event.getBlock().getLocation())) {
            Location location = event.getBlock().getLocation();
            Connector connector = plugin.getConnectorHandler().getConnector(location);
            String locString = LocationIDString.getString(event.getBlock().getLocation());

            Map<String, String> placeholder = new HashMap<>();
            placeholder.put("{locString}", locString);

            plugin.printLogs("debugLog.connectorRemoved", placeholder);

            event.setDropItems(false);

            plugin.dropItems(new ItemStack[]{plugin.getItemFromID(connector.itemID())}, location);

            connectorDAO.deleteConnectorWithWire(locString);
            plugin.getConnectorHandler().removeConnector(location);
            plugin.networkManager().unregisterConnector(location);
            return;
        }

        if (plugin.getMachineTileHandler().isMachineTile(event.getBlock().getLocation())) {
            Location location = event.getBlock().getLocation();

            String locString = LocationIDString.getString(location);
            MachineTile machineTile = plugin.getMachineTileHandler().getMachineTile(location);
            ItemStack[] items = Stream.concat(Arrays.stream(machineTile.input), Arrays.stream(machineTile.output))
                    .toArray(ItemStack[]::new);

            Map<String, String> placeholder = new HashMap<>();
            placeholder.put("{locString}", locString);
            plugin.printLogs("debugLog.machineRemoved", placeholder);

            event.setDropItems(false);

            plugin.dropItems(items, location);
            plugin.getMachineTileHandler().removeMachineTile(location);
            return;
        }
    }
}
