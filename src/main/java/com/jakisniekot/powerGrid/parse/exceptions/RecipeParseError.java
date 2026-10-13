package com.jakisniekot.powerGrid.parse.exceptions;

import java.util.logging.Level;

public enum RecipeParseError implements Error {
    //FATAL ERROR
    DEFAULTS_NOT_LOADED(Level.SEVERE),
    FOLDER_ERROR(Level.SEVERE),
    //ERROR
    INPUT_LIST_ITEM(Level.SEVERE),
    OUTPUT_LIST_ITEM(Level.SEVERE),
    DUPED_RECIPE_NAME(Level.SEVERE),
    //WARNING
    PROCESS_TIME(Level.WARNING),
    //NONE
    NONE(Level.FINEST);

    private Level level;

    RecipeParseError(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
