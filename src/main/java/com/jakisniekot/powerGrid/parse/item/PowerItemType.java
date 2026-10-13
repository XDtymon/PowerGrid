package com.jakisniekot.powerGrid.parse.item;

import org.bukkit.NamespacedKey;

public enum PowerItemType {

    CONNECTOR_WRENCH(),
    MULTIMETER(),
    CONNECTOR(),
    WIRE(),
    RELAY(),
    MACHINE(),
    NONE();

    private NamespacedKey namespacedKey;

    PowerItemType() {

    }
}
