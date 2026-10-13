package com.jakisniekot.powerGrid.power.network;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

/**
 * AI MADE CLASS
 */
public class NetworkTickTask extends BukkitRunnable {

    private final MainPlugin plugin;
    private final NetworkManager manager;
    private final int interval;

    /**
     * @param interval co ile ticków liczyć sieci (1 = każdy tick). Limity są skalowane
     *                 razy interval, więc wattTransfer zawsze oznacza energię na tick.
     */
    public NetworkTickTask(MainPlugin plugin, NetworkManager manager, int interval) {
        this.plugin = plugin;
        this.manager = manager;
        this.interval = Math.max(1, interval);
    }

    public void start() {
        runTaskTimer(plugin, interval, interval);
    }

    @Override
    public void run() {
        // kopia, bo sieci mogą się zmieniać (np. gracz zniszczy connector w trakcie)
        for (EnergyNetwork network : new ArrayList<>(manager.getNetworks())) {
            try {
                network.tick(plugin, interval);;
            } catch (Exception e) {
                plugin.getLogger().warning("Błąd ticku sieci " + network.id() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}