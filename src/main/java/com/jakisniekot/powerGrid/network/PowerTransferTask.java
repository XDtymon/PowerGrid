package com.jakisniekot.powerGrid.network;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.actions.connector.ConnectorTypes;
import com.jakisniekot.powerGrid.database.Connector;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Co X ticków (config: energy.transfer-interval-ticks) przechodzi po każdej sieci,
 * wyciąga energię z bloków pod connectorami PUSH i wkłada ją do bloków pod connectorami
 * PULL w tej samej sieci - każdy blok ograniczony własnym input/output transfer cap
 * i własną pojemnością (trzymanymi w jego PDC, patrz EnergyBlockUtil).
 *
 * energy.transfer-interval-ticks w config.yml:
 *   1  -> transfer co tick (najszybsza aktualizacja)
 *   20 -> transfer raz na sekundę (wolniejsza aktualizacja, mniej obciąża serwer)
 * Ile energii faktycznie płynie na cykl ustala się per-blok przez input/output transfer
 * cap (EnergyBlockUtil), nie tu - dzięki temu jeden interwał obsługuje różne maszyny
 * z różnymi limitami.
 */
public class PowerTransferTask extends BukkitRunnable {

    private final MainPlugin plugin;
    private final PowerNetworkManager networkManager;

    public PowerTransferTask(MainPlugin plugin, PowerNetworkManager networkManager) {
        this.plugin = plugin;
        this.networkManager = networkManager;
    }

    @Override
    public void run() {
        for (PowerNetwork network : networkManager.getNetworks()) {
            if (network.size() < 2) continue; // sam ze sobą nic nie wymieni

            List<EnergyNode> sources = new ArrayList<>();
            List<EnergyNode> sinks = new ArrayList<>();

            for (String connectorId : network.getConnectorIds()) {
                Connector connector = networkManager.getConnectorDAO().get(connectorId);
                if (connector == null) continue;

                Location location = LocationIDString.getLocation(connectorId);
                if (location == null || location.getWorld() == null) continue;
                if (!location.isChunkLoaded()) continue;

                if (connector.connectorTypes() == ConnectorTypes.PUSH) {
                    long extractable = EnergyBlockUtil.getExtractable(location);
                    if (extractable > 0) sources.add(new EnergyNode(location, extractable));
                } else if (connector.connectorTypes() == ConnectorTypes.PULL) {
                    long insertable = EnergyBlockUtil.getInsertable(location);
                    if (insertable > 0) sinks.add(new EnergyNode(location, insertable));
                }
            }

            if (sources.isEmpty() || sinks.isEmpty()) continue;
            distribute(sources, sinks);
        }
    }

    /**
     * Rozlewa energię ze źródeł do odbiorników w rundach, tak żeby kilka bloków PULL
     * dzieliło output jednego PUSH mniej więcej po równo, zamiast pierwszy z brzegu
     * zgarniał wszystko.
     */
    private void distribute(List<EnergyNode> sources, List<EnergyNode> sinks) {
        boolean progress = true;
        while (progress) {
            progress = false;

            List<EnergyNode> activeSinks = sinks.stream().filter(s -> s.remaining > 0).toList();
            if (activeSinks.isEmpty()) break;

            for (EnergyNode source : sources) {
                if (source.remaining <= 0) continue;

                long share = Math.max(1, source.remaining / activeSinks.size());
                for (EnergyNode sink : activeSinks) {
                    if (source.remaining <= 0) break;
                    if (sink.remaining <= 0) continue;

                    long amount = Math.min(share, Math.min(source.remaining, sink.remaining));
                    if (amount <= 0) continue;

                    source.remaining -= amount;
                    sink.remaining -= amount;

                    EnergyBlockUtil.extract(source.location, amount);
                    EnergyBlockUtil.insert(sink.location, amount);

                    progress = true;
                }
            }
        }
    }

    /**
     * Uruchamia task z interwałem wczytanym z config.yml (energy.transfer-interval-ticks,
     * domyślnie 1 = co tick). Wywołać raz w MainPlugin#onEnable po zbudowaniu sieci.
     */
    public static BukkitTask start(MainPlugin plugin, PowerNetworkManager networkManager) {
        long interval = Math.max(1, plugin.getConfig().getLong("energy.transfer-interval-ticks", 1));
        return new PowerTransferTask(plugin, networkManager).runTaskTimer(plugin, interval, interval);
    }

    private static final class EnergyNode {
        private final Location location;
        private long remaining;

        private EnergyNode(Location location, long remaining) {
            this.location = location;
            this.remaining = remaining;
        }
    }
}