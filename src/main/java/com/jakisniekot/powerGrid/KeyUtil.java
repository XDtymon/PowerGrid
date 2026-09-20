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

    public static NamespacedKey WireConnectionKey() {
        return new NamespacedKey("powergrid", "wire_location_1");
    }

    public static NamespacedKey FirstWireStandKey() {
        return new NamespacedKey("powergrid", "wirestand_location_1");
    }

    public static NamespacedKey SecondWireStandKey() {
        return new NamespacedKey("powergrid", "wirestand_location_2");
    }

    public static NamespacedKey WattPerSecondKey() {
        return new NamespacedKey("powergrid", "watt_per_second");
    }

    public static NamespacedKey WireBlocksKey() {
        return new NamespacedKey("powergrid", "wire_blocks");
    }

    public static NamespacedKey ItemIDKey() {
        return new NamespacedKey("powergrid", "item_id");
    }

    public static NamespacedKey MaxConnectionDistanceKey() {
        return new NamespacedKey("powergrid", "max_connection_distance");
    }

    public static NamespacedKey MaxConnectionAmountKey() {
        return new NamespacedKey("powergrid", "max_connection_amount");
    }

    public static NamespacedKey WireParticleKey() {
        return new NamespacedKey("powergrid", "wire_particle");
    }
}
