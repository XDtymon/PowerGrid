package com.jakisniekot.powerGrid.listener;

import com.jakisniekot.powerGrid.power.connector.Connector;
import com.jakisniekot.powerGrid.util.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.power.connector.ConnectorTypes;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockPlaceListener implements Listener {

    private MainPlugin plugin;

    public BlockPlaceListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listener(BlockPlaceEvent event) {
        if (plugin.getConnection() == null) {
            plugin.printLogs("errors.database.noConnection");
            return;
        }

        ItemStack itemStack = event.getItemInHand();

        if (!itemStack.hasItemMeta()) {

            return;
        }

        if (!itemStack.getItemMeta().getPersistentDataContainer().has(KeyUtil.TypeKey())) {

            return;
        }

        PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();

        switch (pdc.get(KeyUtil.TypeKey(), PersistentDataType.STRING)) {
            case "CONNECTOR":
                int maxConnections = 0;
                List<String> allowedWires = new ArrayList<>();

                try {
                    maxConnections = pdc.get(KeyUtil.MaxConnectionAmountKey(), PersistentDataType.INTEGER);
                    allowedWires = pdc.get(KeyUtil.AllowedTypesKey(), PersistentDataType.LIST.strings());

                } catch (IllegalArgumentException _) {
                    return;
                }

                boolean blacklist = false;

                try {
                    blacklist = pdc.get(KeyUtil.AllowedTypesListTypeKey(), PersistentDataType.BOOLEAN);
                } catch (IllegalArgumentException _) {
                }

                if (allowedWires == null) allowedWires = new ArrayList<>();
                if (allowedWires.isEmpty()) blacklist = true;


                String locString = LocationIDString.getString(event.getBlockPlaced().getLocation());
                String itemID = pdc.get(KeyUtil.ItemIDKey(), PersistentDataType.STRING);

                Map<String, String> placeholder = new HashMap<>();
                placeholder.put("{locString}", locString);
                plugin.printLogs("debugLog.connectorPlaced", placeholder);

                Connector connector = new Connector(
                        locString,
                        maxConnections,
                        allowedWires,
                        blacklist,
                        ConnectorTypes.STATIC,
                        itemID
                );

                plugin.getConnectorHandler().addConnector(
                        event.getBlockPlaced().getLocation(),
                        connector
                );

                plugin.networkManager().registerConnector(
                        event.getBlockPlaced().getLocation()
                );

                break;
            case "MACHINE":
                String machineID = pdc.get(KeyUtil.MachineIDKey(), PersistentDataType.STRING);
                locString = LocationIDString.getString(event.getBlockPlaced().getLocation());


                placeholder = new HashMap<>();
                placeholder.put("{locString}", locString);
                plugin.printLogs("debugLog.machinePlaced", placeholder);

                plugin.getMachineTileHandler().newMachineTile(machineID, event.getBlockPlaced().getLocation());

                break;
            case "WIRE", "CONNECTOR_WRENCH", "MULTIMETER":
                event.setCancelled(true);
                break;


            case null:
                break;
            default:
                break;
        }
    }


}
