package com.jakisniekot.powerGrid.item;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.NamespacedKey;

public enum PowerItemType {


    CONNECTOR(),
    WIRE(),
    RELAY();

    private NamespacedKey namespacedKey;

    PowerItemType() {

    }




}
