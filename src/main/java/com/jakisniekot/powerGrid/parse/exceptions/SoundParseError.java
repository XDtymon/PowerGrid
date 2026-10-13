package com.jakisniekot.powerGrid.parse.exceptions;

import java.util.logging.Level;

public enum SoundParseError implements Error{

    NO_SOUND_TYPE(Level.SEVERE),

    NO_PITCH(Level.WARNING),
    NO_VOLUME(Level.WARNING),
    NO_CATEGORY(Level.WARNING),
    NO_PLAY_TYPE(Level.WARNING),

    NONE(Level.FINEST);

    private Level level;

    SoundParseError(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

}
