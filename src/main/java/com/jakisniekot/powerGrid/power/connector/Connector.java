package com.jakisniekot.powerGrid.power.connector;

import java.util.List;

public class Connector {

    private final String id;
    private final int maxConnections;
    private final List<String> allowedWires;
    private final boolean blacklist;
    private ConnectorTypes connectorTypes;
    private final String itemID;


    public Connector(
            String id,
            int maxConnections,
            List<String> allowedWires,
            boolean blacklist,
            ConnectorTypes connectorTypes,
            String itemID
    ) {
        this.id = id;
        this.maxConnections = maxConnections;
        this.allowedWires = allowedWires;
        this.blacklist = blacklist;
        this.connectorTypes = connectorTypes;
        this.itemID = itemID;
    }

    public String id() {
        return id;
    }

    public int maxConnections() {
        return maxConnections;
    }

    public List<String> allowedWires() {
        return allowedWires;
    }

    public boolean blacklist() {
        return blacklist;
    }

    public ConnectorTypes connectorTypes() {
        return connectorTypes;
    }

    public String itemID() {
        return itemID;
    }



    public void nextConnectorType() {
        this.connectorTypes = connectorTypes.next();
    }
}