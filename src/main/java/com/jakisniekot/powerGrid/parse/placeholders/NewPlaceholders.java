package com.jakisniekot.powerGrid.parse.placeholders;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewPlaceholders extends me.clip.placeholderapi.expansion.PlaceholderExpansion {

    private final MainPlugin plugin;

    private final String energySymbol;

    public NewPlaceholders(MainPlugin plugin) {
        this.plugin = plugin;
        this.energySymbol = plugin.getConfig().getString("energy.energySymbol");
    }

    @Override
    public @NotNull String getIdentifier() {
        return "pg";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // nie wyrejestrowuj przy /papi reload
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return switch (params.toLowerCase()) {
                case "energy_symbol" -> energySymbol;
                default -> null;
            };
        } else {
            return "";
        }

    }
}