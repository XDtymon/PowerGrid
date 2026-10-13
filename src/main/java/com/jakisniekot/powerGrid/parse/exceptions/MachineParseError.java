package com.jakisniekot.powerGrid.parse.exceptions;

import java.util.logging.Level;

public enum MachineParseError implements Error{

    ITEM_SLOT_DUPED_ID(Level.SEVERE),
    ITEM_SLOT_NO_ID(Level.SEVERE),


    NO_ITEM_SLOT_TYPE(Level.WARNING),
    ITEM_STORAGE_NULL(Level.WARNING),

    INPUT_TRANSFER_ERROR(Level.FINE),
    OUTPUT_TRANSFER_ERROR(Level.FINE),
    ENERGY_CUSTOM(Level.FINE),
    MAX_ENERGY_ERROR(Level.FINE),
    NO_INPUTS(Level.FINE),
    NO_OUTPUTS(Level.FINE),

    NONE(Level.FINEST);

    private Level level;

    MachineParseError(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

}
