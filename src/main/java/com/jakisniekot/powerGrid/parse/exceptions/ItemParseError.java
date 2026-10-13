package com.jakisniekot.powerGrid.parse.exceptions;

import java.util.logging.Level;

public enum ItemParseError implements Error {

    MATERIAL_ERROR(Level.SEVERE),
    ITEM_TYPE_ERROR(Level.SEVERE),
    FATAL_ERROR(Level.SEVERE),
    ITEM_NAME_ERROR(Level.WARNING),
    NONE(Level.FINEST);

    private Level level;

    ItemParseError(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
