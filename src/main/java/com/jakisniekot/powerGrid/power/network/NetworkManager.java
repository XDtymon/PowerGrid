package com.jakisniekot.powerGrid.power.network;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.power.connector.Connector;
import com.jakisniekot.powerGrid.power.wire.Wire;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.Location;

import java.util.*;

/**
 * Trzyma wszystkie sieci. Zajmuje się wyłącznie grafem w pamięci -
 * zapis do bazy (WireDAO.insertOrUpdate / getConnectedAndDelete) robisz sam w miejscu wywołania.
 *
 * AI MADE CLASS
 */
public class NetworkManager {

    public enum Result {
        OK,
        SAME_CONNECTOR,
        DIFFERENT_WORLD,
        NO_CONNECTOR,
        ALREADY_CONNECTED,
        MAX_CONNECTIONS,
        WIRE_NOT_ALLOWED
    }

    private final MainPlugin plugin;
    private final Set<EnergyNetwork> networks = new HashSet<>();
    private final Map<Location, EnergyNetwork> byNode = new HashMap<>();

    public NetworkManager(MainPlugin plugin) {
        this.plugin = plugin;
    }

    /** Normalizacja do współrzędnych bloku (bez yaw/pitch/ułamków) - klucz we wszystkich mapach. */
    public static Location key(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    // ------------------------------------------------------------------ odczyt

    public Collection<EnergyNetwork> getNetworks() {
        return Collections.unmodifiableSet(networks);
    }

    public EnergyNetwork getNetworkAt(Location connectorLoc) {
        return byNode.get(key(connectorLoc));
    }

    public boolean areConnected(Location a, Location b) {
        EnergyNetwork na = getNetworkAt(a);
        return na != null && na == getNetworkAt(b);
    }

    // ------------------------------------------------------------------ ładowanie

    /** Wywołaj w onEnable PO załadowaniu connectorów (ConnectorHandler.addFromDB). */
    public void loadFromDB() {
        networks.clear();
        byNode.clear();

        for (Location loc : plugin.getConnectorHandler().getConnectors().keySet()) {
            registerConnector(loc);
        }

        for (Wire w : plugin.getWireDAO().getAll()) {
            Location a = key(LocationIDString.getLocation(w.loc1()));
            Location b = key(LocationIDString.getLocation(w.loc2()));
            if (!byNode.containsKey(a) || !byNode.containsKey(b)) continue; // kabel bez connectora - pomijamy
            link(a, b, w.wattTransfer(), w.itemID());
        }
    }

    // ------------------------------------------------------------------ connectory

    /** Wołaj po postawieniu connectora (po connectorHandler.addConnector). */
    public void registerConnector(Location loc) {
        Location k = key(loc);
        if (byNode.containsKey(k)) return;
        EnergyNetwork net = new EnergyNetwork();
        net.addNode(k);
        networks.add(net);
        byNode.put(k, net);
    }

    /**
     * Wołaj po zniszczeniu connectora. Usuwa węzeł i jego kable z grafu,
     * a jeśli sieć się rozpadła - dzieli ją na osobne.
     * Pamiętaj o wireDAO.getConnectedAndDelete(...) i connectorHandler.removeConnector(...).
     */
    public void unregisterConnector(Location loc) {
        Location k = key(loc);
        EnergyNetwork net = byNode.remove(k);
        if (net == null) return;

        net.removeNode(k);
        if (net.isEmpty()) {
            networks.remove(net);
        } else {
            splitIfNeeded(net);
        }
    }

    // ------------------------------------------------------------------ kable

    /**
     * Łączy dwa connectory kablem. Jeśli należą do różnych sieci - scala je.
     * Zapis do bazy (wireDAO.insertOrUpdate) zrób, gdy zwróci OK.
     */
    public Result connect(Location l1, Location l2, int wattTransfer, String wireItemID) {
        Location a = key(l1);
        Location b = key(l2);

        if (a.equals(b)) return Result.SAME_CONNECTOR;
        if (!Objects.equals(a.getWorld(), b.getWorld())) return Result.DIFFERENT_WORLD;

        Connector ca = plugin.getConnectorHandler().getConnector(a);
        Connector cb = plugin.getConnectorHandler().getConnector(b);
        if (ca == null || cb == null) return Result.NO_CONNECTOR;

        registerConnector(a);
        registerConnector(b);

        EnergyNetwork na = byNode.get(a);
        if (na.getEdge(a, b) != null) return Result.ALREADY_CONNECTED;

        if (isFull(ca, a) || isFull(cb, b)) return Result.MAX_CONNECTIONS;
        if (!isWireAllowed(ca, wireItemID) || !isWireAllowed(cb, wireItemID)) return Result.WIRE_NOT_ALLOWED;

        link(a, b, wattTransfer, wireItemID);
        return Result.OK;
    }

    /** Usuwa jeden kabel. Sieć może się rozdzielić na dwie. */
    public boolean disconnect(Location l1, Location l2) {
        Location a = key(l1);
        Location b = key(l2);
        EnergyNetwork net = byNode.get(a);
        if (net == null || net != byNode.get(b)) return false;
        if (!net.removeEdge(a, b)) return false;
        splitIfNeeded(net);
        return true;
    }

    // ------------------------------------------------------------------ wewnętrzne

    private boolean isFull(Connector c, Location loc) {
        // maxConnections <= 0 traktuję jako "bez limitu"
        return c.maxConnections() > 0 && byNode.get(loc).degree(loc) >= c.maxConnections();
    }

    private boolean isWireAllowed(Connector c, String wireItemID) {
        List<String> list = c.allowedWires();
        if (list == null || list.isEmpty()) return true; // pusta lista = wszystko dozwolone
        boolean contains = list.contains(wireItemID);
        return c.blacklist() != contains; // blacklist: nie może być na liście, whitelist: musi być
    }

    /** Dodaje krawędź bez walidacji; scala sieci, jeśli były różne. */
    private void link(Location a, Location b, int watt, String itemID) {
        EnergyNetwork na = byNode.get(a);
        EnergyNetwork nb = byNode.get(b);

        if (na != nb) {
            EnergyNetwork big = na.nodes().size() >= nb.nodes().size() ? na : nb;
            EnergyNetwork small = big == na ? nb : na;
            Set<Location> moved = new HashSet<>(small.nodes());
            big.absorb(small);
            for (Location loc : moved) byNode.put(loc, big);
            networks.remove(small);
            na = big;
        }
        na.addEdge(new EnergyNetwork.Edge(a, b, watt, itemID));
    }

    /** Jeśli graf sieci przestał być spójny - rozdziela go na osobne sieci. */
    private void splitIfNeeded(EnergyNetwork net) {
        Set<Location> unvisited = new HashSet<>(net.nodes());
        List<Set<Location>> components = new ArrayList<>();

        while (!unvisited.isEmpty()) {
            Location start = unvisited.iterator().next();
            Set<Location> comp = net.reachableFrom(start);
            unvisited.removeAll(comp);
            components.add(comp);
        }
        if (components.size() <= 1) return;

        components.sort(Comparator.comparingInt((Set<Location> s) -> s.size()).reversed());
        // największy komponent zostaje w oryginalnej sieci
        for (int i = 1; i < components.size(); i++) {
            EnergyNetwork fresh = net.extract(components.get(i));
            networks.add(fresh);
            for (Location loc : components.get(i)) byNode.put(loc, fresh);
        }
    }
}