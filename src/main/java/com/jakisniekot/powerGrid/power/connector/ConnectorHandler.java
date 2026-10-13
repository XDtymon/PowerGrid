package com.jakisniekot.powerGrid.power.connector;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class ConnectorHandler {
    private MainPlugin plugin;

    private Map<Location, Connector> connectors = new HashMap<>();

    public ConnectorHandler(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public Connector getConnector(Location location) {
        return connectors.get(location);
    }

    public boolean isConnector(Location location) {
        return connectors.containsKey(location);
    }

    public void addConnector(Location location, Connector connector) {
        connectors.put(location, connector);
    }

    public void removeConnector(Location location) {
        connectors.remove(location);
    }

    public Map<Location, Connector> getConnectors() {
        return connectors;
    }

    public void addFromDB() {
        Map<Location, Connector> connectors = plugin.getConnectorDAO().getAll();
        if (connectors != null && !connectors.isEmpty()) {
            connectors.putAll(connectors);
        }
    }
}
