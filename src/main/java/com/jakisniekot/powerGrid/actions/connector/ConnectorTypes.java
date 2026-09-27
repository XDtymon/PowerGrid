package com.jakisniekot.powerGrid.actions.connector;

public enum ConnectorTypes {
    STATIC,
    PUSH,
    PULL;

    /**
     * Kolejny typ w cyklu STATIC -> PUSH -> PULL -> STATIC -> ...
     * Wygodne pod prawy-klik itemkiem, który przełącza tryb connectora.
     */
    public ConnectorTypes next() {
        return switch (this) {
            case STATIC -> PUSH;
            case PUSH -> PULL;
            case PULL -> STATIC;
        };
    }
}