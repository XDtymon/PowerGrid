package com.jakisniekot.powerGrid.database;

import com.jakisniekot.powerGrid.actions.connector.ConnectorTypes;

import java.util.List;

public record Connector(
        String id,
        int maxConnections,
        List<String> allowedWires,
        boolean blacklist,
        ConnectorTypes connectorTypes,
        String itemID
) {}