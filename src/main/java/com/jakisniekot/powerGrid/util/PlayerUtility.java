package com.jakisniekot.powerGrid.util;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.entity.Player;

import java.util.Map;

public class PlayerUtility {

    private MainPlugin plugin;

    public PlayerUtility(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public void actionbar(Player player, String message, Map<String, String> placeholder) {
        player.sendActionBar(TextUtility.color(message, placeholder));
    }
}
