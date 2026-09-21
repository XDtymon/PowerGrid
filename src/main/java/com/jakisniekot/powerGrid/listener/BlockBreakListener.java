package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.database.Connector;
import com.jakisniekot.powerGrid.database.ConnectorDAO;
import com.jakisniekot.powerGrid.database.WireDAO;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BlockBreakListener implements Listener {

    private MainPlugin plugin;
    private ConnectorDAO connectorDAO;
    private WireDAO wireDAO;

    public BlockBreakListener(MainPlugin plugin) {
        this.plugin = plugin;
        this.wireDAO = plugin.getWireDAO();
        this.connectorDAO = plugin.getConnectorDAO();
    }

    @EventHandler
    public void listener(BlockBreakEvent event) {
        if (connectorDAO.get(LocationIDString.getString(event.getBlock().getLocation())) != null) {

            Connector connector = connectorDAO.get(LocationIDString.getString(event.getBlock().getLocation()));
            String locString = LocationIDString.getString(event.getBlock().getLocation());

            Map<String, String> placeholder = new HashMap<>();
            placeholder.put("{locString}", locString);
            plugin.printLogs("debugLog.connectorRemoved", placeholder);
            event.setDropItems(false);

            Location location = event.getBlock().getLocation();
            plugin.dropItems(new ItemStack[]{plugin.getItemFromID(connector.itemID())}, location);

            connectorDAO.deleteConnectorWithWire(locString);
        }
    }
}
