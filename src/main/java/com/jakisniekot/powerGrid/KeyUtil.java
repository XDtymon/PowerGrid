package com.jakisniekot.powerGrid;

import org.bukkit.NamespacedKey;

public class KeyUtil {

    /**
     * Power type (przedmioty)
     */
    public static NamespacedKey TypeKey() {
        return new NamespacedKey("powergrid", "type");
    }

    /**
     * UUID
     */
    public static NamespacedKey UUIDKey() {
        return new NamespacedKey("powergrid", "uuid");
    }

    /**
     * Whitelista (badź blacklista) allowed wireów
     */
    public static NamespacedKey AllowedTypesKey() {
        return new NamespacedKey("powergrid", "allowed_types");
    }

    /**
     * Blacklist czy whitelist ahh key
     */
    public static NamespacedKey AllowedTypesListTypeKey() {
        return new NamespacedKey("powergrid", "allowed_types_list_type");
    }

    /**
     * Key łączenia wireów (1 connector)
     */
    public static NamespacedKey WireConnectionKey() {
        return new NamespacedKey("powergrid", "wire_location_1");
    }

    /**
     * Key dla wirestanda (1 connector)
     */
    public static NamespacedKey FirstWireStandKey() {
        return new NamespacedKey("powergrid", "wirestand_location_1");
    }

    /**
     * Key dla wirestanda (2 connector)
     */
    public static NamespacedKey SecondWireStandKey() {
        return new NamespacedKey("powergrid", "wirestand_location_2");
    }

    /**
     * transfer
     */
    public static NamespacedKey WattPerSecondKey() {
        return new NamespacedKey("powergrid", "watt_per_second");
    }

    /**
     * Bloki z których jest budowany wire
     */
    public static NamespacedKey WireBlocksKey() {
        return new NamespacedKey("powergrid", "wire_blocks");
    }

    /**
     * ID przedmiotu
     */
    public static NamespacedKey ItemIDKey() {
        return new NamespacedKey("powergrid", "item_id");
    }

    /**
     * Maksymalna długość kabla
     */
    public static NamespacedKey MaxConnectionDistanceKey() {
        return new NamespacedKey("powergrid", "max_connection_distance");
    }

    /**
     * Maksymalna ilość połączeń connectora
     */
    public static NamespacedKey MaxConnectionAmountKey() {
        return new NamespacedKey("powergrid", "max_connection_amount");
    }

    /**
     * Particle przy łączeniu kabli
     */
    public static NamespacedKey WireParticleKey() {
        return new NamespacedKey("powergrid", "wire_particle");
    }

    // ---- Energia bloku pod connectorem ----

    /**
     * Aktualna ilość energii przechowywana w bloku (PDC bloku, nie itemu).
     */
    public static NamespacedKey EnergyKey() {
        return new NamespacedKey("powergrid", "energy");
    }

    /**
     * Maksymalna pojemność energii tego bloku.
     */
    public static NamespacedKey MaxEnergyKey() {
        return new NamespacedKey("powergrid", "max_energy");
    }

    /**
     * Ile energii ten blok może przyjąć w jednym cyklu transferu (connector PULL).
     */
    public static NamespacedKey InputTransferCapKey() {
        return new NamespacedKey("powergrid", "input_transfer_cap");
    }

    /**
     * Ile energii ten blok może oddać w jednym cyklu transferu (connector PUSH).
     */
    public static NamespacedKey OutputTransferCapKey() {
        return new NamespacedKey("powergrid", "output_transfer_cap");
    }
}
