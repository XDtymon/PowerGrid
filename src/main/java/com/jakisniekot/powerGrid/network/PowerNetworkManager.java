package com.jakisniekot.powerGrid.network;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.database.Connector;
import com.jakisniekot.powerGrid.database.ConnectorDAO;
import com.jakisniekot.powerGrid.database.Wire;
import com.jakisniekot.powerGrid.database.WireDAO;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trzyma w pamięci, które connectory są ze sobą połączone wirami ("sieci"), bez potrzeby
 * przeliczania całej bazy danych za każdym razem, gdy coś się zmienia.
 *
 * - onConnectorAdded / onConnectorRemoved: nowy connector (jeszcze bez wira) od razu
 *   dostaje własną, jednoelementową sieć.
 * - onWireAdded: łączy (mergeuje) sieci obu końców wira.
 * - onWireRemoved: przelicza od nowa (flood-fill) tylko węzły starej sieci, sprawdzając,
 *   czy usunięcie tego jednego wira ją rozdzieliło - operacja lokalna, nie dotyka reszty
 *   grafu połączeń w innych sieciach.
 *
 * Wywołania onWireAdded / onWireRemoved / onConnectorAdded / onConnectorRemoved powinny
 * następować zaraz po odpowiednich zapisach w ConnectorDAO / WireDAO (patrz integracja
 * w WireItemActions oraz listenerach place/break bloków).
 */
public class PowerNetworkManager {

    private final MainPlugin plugin;
    private final ConnectorDAO connectorDAO;
    private final WireDAO wireDAO;

    // connectorId -> zbiór id connectorów, z którymi ma bezpośredni wire (odzwierciedla tabelę wires)
    private final Map<String, Set<String>> adjacency = new ConcurrentHashMap<>();

    // connectorId -> sieć, do której aktualnie należy
    private final Map<String, PowerNetwork> networkByConnector = new ConcurrentHashMap<>();

    // networkId -> sieć (do iterowania podczas ticku transferu)
    private final Map<UUID, PowerNetwork> networks = new ConcurrentHashMap<>();

    public PowerNetworkManager(MainPlugin plugin, ConnectorDAO connectorDAO, WireDAO wireDAO) {
        this.plugin = plugin;
        this.connectorDAO = connectorDAO;
        this.wireDAO = wireDAO;
    }

    /**
     * Pełny rebuild z bazy danych. Wywołać raz przy starcie (po reloadWires) - później
     * wszystko powinno zostawać zsynchronizowane przez hooki onXxx poniżej.
     */
    public synchronized void rebuildFromDatabase() {
        adjacency.clear();
        networkByConnector.clear();
        networks.clear();

        List<Connector> connectors = connectorDAO.getAll();
        for (Connector connector : connectors) {
            adjacency.putIfAbsent(connector.id(), ConcurrentHashMap.newKeySet());
        }

        List<Wire> wires = wireDAO.getAll();
        for (Wire wire : wires) {
            link(wire.loc1(), wire.loc2());
        }

        Set<String> visited = new HashSet<>();
        for (String connectorId : adjacency.keySet()) {
            if (visited.contains(connectorId)) continue;
            Set<String> component = flood(connectorId, adjacency.keySet());
            visited.addAll(component);
            createNetworkFor(component);
        }

        plugin.getLogger().info("[PowerGrid] Przeliczono " + networks.size() + " sieci z "
                + connectors.size() + " connectorów i " + wires.size() + " wirów.");
    }

    public synchronized void onConnectorAdded(String connectorId) {
        adjacency.putIfAbsent(connectorId, ConcurrentHashMap.newKeySet());
        if (!networkByConnector.containsKey(connectorId)) {
            PowerNetwork network = new PowerNetwork(UUID.randomUUID());
            network.add(connectorId);
            networks.put(network.getId(), network);
            networkByConnector.put(connectorId, network);
        }
    }

    public synchronized void onConnectorRemoved(String connectorId) {
        Set<String> neighbours = adjacency.remove(connectorId);
        if (neighbours != null) {
            for (String neighbour : neighbours) {
                Set<String> back = adjacency.get(neighbour);
                if (back != null) back.remove(connectorId);
            }
        }

        PowerNetwork oldNetwork = networkByConnector.remove(connectorId);
        if (oldNetwork == null) return;

        oldNetwork.remove(connectorId);
        if (oldNetwork.isEmpty()) {
            networks.remove(oldNetwork.getId());
            return;
        }

        resplit(oldNetwork);
    }

    public synchronized void onWireAdded(String loc1, String loc2) {
        onConnectorAdded(loc1);
        onConnectorAdded(loc2);
        link(loc1, loc2);

        PowerNetwork networkA = networkByConnector.get(loc1);
        PowerNetwork networkB = networkByConnector.get(loc2);
        if (networkA == networkB) return;

        PowerNetwork bigger = networkA.size() >= networkB.size() ? networkA : networkB;
        PowerNetwork smaller = bigger == networkA ? networkB : networkA;

        for (String connectorId : smaller.getConnectorIds()) {
            bigger.add(connectorId);
            networkByConnector.put(connectorId, bigger);
        }
        networks.remove(smaller.getId());
    }

    public synchronized void onWireRemoved(String loc1, String loc2) {
        unlink(loc1, loc2);

        PowerNetwork network = networkByConnector.get(loc1);
        if (network == null) return;

        resplit(network);
    }

    /**
     * Przelicza od nowa (flood-fill) węzły, które wcześniej były w `network`, używając
     * aktualnego stanu adjacency (już bez usuniętej krawędzi). Usunięcie jednego wira
     * albo nic nie zmienia w spójności, albo dzieli sieć dokładnie na dwie części -
     * dlatego dotyka tylko węzłów tej jednej, starej sieci.
     */
    private void resplit(PowerNetwork network) {
        Set<String> nodes = new HashSet<>(network.getConnectorIds());
        if (nodes.isEmpty()) return;

        Set<String> visited = new HashSet<>();
        boolean first = true;
        for (String node : nodes) {
            if (visited.contains(node)) continue;
            Set<String> component = flood(node, nodes);
            visited.addAll(component);

            if (first) {
                network.getConnectorIds().retainAll(component);
                for (String id : component) networkByConnector.put(id, network);
                first = false;
            } else {
                createNetworkFor(component);
            }
        }
    }

    private void createNetworkFor(Set<String> component) {
        PowerNetwork network = new PowerNetwork(UUID.randomUUID());
        for (String id : component) {
            network.add(id);
            networkByConnector.put(id, network);
        }
        networks.put(network.getId(), network);
    }

    private Set<String> flood(String start, Set<String> allowed) {
        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(start);
        visited.add(start);

        while (!stack.isEmpty()) {
            String current = stack.pop();
            for (String neighbour : adjacency.getOrDefault(current, Collections.emptySet())) {
                if (!allowed.contains(neighbour)) continue;
                if (visited.add(neighbour)) {
                    stack.push(neighbour);
                }
            }
        }
        return visited;
    }

    private void link(String loc1, String loc2) {
        adjacency.computeIfAbsent(loc1, k -> ConcurrentHashMap.newKeySet()).add(loc2);
        adjacency.computeIfAbsent(loc2, k -> ConcurrentHashMap.newKeySet()).add(loc1);
    }

    private void unlink(String loc1, String loc2) {
        Set<String> a = adjacency.get(loc1);
        if (a != null) a.remove(loc2);
        Set<String> b = adjacency.get(loc2);
        if (b != null) b.remove(loc1);
    }

    public Collection<PowerNetwork> getNetworks() {
        return networks.values();
    }

    public PowerNetwork getNetwork(String connectorId) {
        return networkByConnector.get(connectorId);
    }

    public ConnectorDAO getConnectorDAO() {
        return connectorDAO;
    }
}