package com.jakisniekot.powerGrid.util;

import com.jakisniekot.powerGrid.MainPlugin;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.Map;

public class PlayerUtility {

    private MainPlugin plugin;

    public PlayerUtility(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public void actionbar(Player player, String message, Map<String, String> placeholder) {
        player.sendActionBar(TextUtility.stringReplacer(message, placeholder));
    }
}
