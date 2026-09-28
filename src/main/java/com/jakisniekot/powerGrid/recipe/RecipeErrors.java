package com.jakisniekot.powerGrid.recipe;

import org.bukkit.NamespacedKey;

import java.util.logging.Level;

public enum RecipeErrors {
    INPUT_LIST_ITEM(Level.SEVERE),
    OUTPUT_LIST_ITEM(Level.SEVERE),
    PROGRESS_TIME(Level.WARNING),
    NONE(Level.FINEST);

    private Level level;

    RecipeErrors(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
