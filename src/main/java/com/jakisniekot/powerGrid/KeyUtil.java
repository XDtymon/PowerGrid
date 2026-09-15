package com.jakisniekot.powerGrid;

import org.bukkit.NamespacedKey;

public class KeyUtil {

    public static NamespacedKey TypeKey() {
        return new NamespacedKey("powergrid", "type");
    }

    public static NamespacedKey UUIDKey() {
        return new NamespacedKey("powergrid", "uuid");
    }

    public static NamespacedKey AllowedTypesKey() {
        return new NamespacedKey("powergrid", "allowed_types");
    }

    public static NamespacedKey AllowedTypesListTypeKey() {
        return new NamespacedKey("powergrid", "allowed_types_list_type");
    }

    public static NamespacedKey WattPerSecondKey() {
        return new NamespacedKey("powergrid", "watt_per_second");
    }

    public static NamespacedKey MaxConnectionDistanceKey() {
        return new NamespacedKey("powergrid", "max_connection_distance");
    }
}
