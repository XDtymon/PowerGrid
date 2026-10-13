package com.jakisniekot.powerGrid.power.network;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.power.connector.ConnectorTypes;
import com.jakisniekot.powerGrid.power.connector.Connector;
import com.jakisniekot.powerGrid.power.connector.ConnectorHandler;
import com.jakisniekot.powerGrid.power.machine.EnergyStack;
import com.jakisniekot.powerGrid.power.machine.tile.MachineTile;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.*;

/**
 * Jedna sieć = spójny graf. Węzły to connectory (Location bloku), krawędzie to kable.
 * Każdy tick liczymy maksymalny przepływ (Dinic) ze źródeł (PUSH) do odbiorników (PULL),
 * gdzie przepustowość krawędzi = wattTransfer kabla. Dzięki temu wąskie gardła
 * automatycznie zmniejszają przepływ.
 *
 * AI MADE CLASS
 */
public class EnergyNetwork {

    public record Edge(Location a, Location b, int limit, String itemID) {
        public Location other(Location from) {
            return from.equals(a) ? b : a;
        }

        public boolean connects(Location x, Location y) {
            return (a.equals(x) && b.equals(y)) || (a.equals(y) && b.equals(x));
        }
    }

    private static final BlockFace[] FACES = {
            BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
            BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final UUID id = UUID.randomUUID();
    private final Map<Location, Set<Edge>> adjacency = new HashMap<>();

    private long lastTransferred;
    private int rotation;

    // ------------------------------------------------------------------ graf

    public UUID id() {
        return id;
    }

    public Set<Location> nodes() {
        return adjacency.keySet();
    }

    public boolean hasNode(Location loc) {
        return adjacency.containsKey(loc);
    }

    public boolean isEmpty() {
        return adjacency.isEmpty();
    }

    public void addNode(Location loc) {
        adjacency.computeIfAbsent(loc, k -> new HashSet<>());
    }

    public int degree(Location loc) {
        Set<Edge> set = adjacency.get(loc);
        return set == null ? 0 : set.size();
    }

    public Set<Edge> edgesOf(Location loc) {
        return adjacency.getOrDefault(loc, Collections.emptySet());
    }

    public Edge getEdge(Location a, Location b) {
        for (Edge e : edgesOf(a)) {
            if (e.connects(a, b)) return e;
        }
        return null;
    }

    /** Każda krawędź dokładnie raz. */
    public Set<Edge> edges() {
        Set<Edge> all = new LinkedHashSet<>();
        for (Set<Edge> set : adjacency.values()) all.addAll(set);
        return all;
    }

    public void addEdge(Edge edge) {
        addNode(edge.a());
        addNode(edge.b());
        adjacency.get(edge.a()).add(edge);
        adjacency.get(edge.b()).add(edge);
    }

    public boolean removeEdge(Location a, Location b) {
        Edge e = getEdge(a, b);
        if (e == null) return false;
        adjacency.get(e.a()).remove(e);
        adjacency.get(e.b()).remove(e);
        return true;
    }

    /** Usuwa węzeł razem ze wszystkimi jego kablami. */
    public void removeNode(Location loc) {
        Set<Edge> set = adjacency.remove(loc);
        if (set == null) return;
        for (Edge e : set) {
            Set<Edge> otherSet = adjacency.get(e.other(loc));
            if (otherSet != null) otherSet.remove(e);
        }
    }

    /** Wchłania całą drugą sieć (scalanie). */
    void absorb(EnergyNetwork other) {
        for (Map.Entry<Location, Set<Edge>> en : other.adjacency.entrySet()) {
            adjacency.computeIfAbsent(en.getKey(), k -> new HashSet<>()).addAll(en.getValue());
        }
        other.adjacency.clear();
    }

    /** Przenosi podany zbiór węzłów (cały komponent) do nowej sieci (dzielenie). */
    EnergyNetwork extract(Set<Location> component) {
        EnergyNetwork n = new EnergyNetwork();
        for (Location loc : component) {
            Set<Edge> edges = adjacency.remove(loc);
            if (edges != null) n.adjacency.put(loc, edges);
        }
        return n;
    }

    /** Wszystkie węzły osiągalne z start. */
    Set<Location> reachableFrom(Location start) {
        Set<Location> seen = new HashSet<>();
        Deque<Location> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            Location cur = queue.poll();
            for (Edge e : edgesOf(cur)) {
                Location next = e.other(cur);
                if (seen.add(next)) queue.add(next);
            }
        }
        return seen;
    }

    /** Ile energii przeszło przez sieć w ostatnim ticku (debug / GUI). */
    public long getLastTransferred() {
        return lastTransferred;
    }

    // ------------------------------------------------------------------ tick

    /**
     * @param interval co ile ticków wywoływany jest tick sieci; limity (kabli i maszyn)
     *                 są mnożone przez tę wartość, więc energia/tick się zgadza.
     * @return ile energii przesłano
     */
    public long tick(MainPlugin plugin, int interval) {
        lastTransferred = 0;
        if (adjacency.size() < 1) return 0;

        ConnectorHandler ch = plugin.getConnectorHandler();
        List<Location> nodes = new ArrayList<>(adjacency.keySet());

        Set<MachineTile> usedSources = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<MachineTile> usedSinks = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Port> sources = new ArrayList<>();
        List<Port> sinks = new ArrayList<>();

        // 1) źródła (PUSH)
        for (Location loc : nodes) {
            Connector c = ch.getConnector(loc);
            if (c == null || c.connectorTypes() != ConnectorTypes.PUSH || !isLoaded(loc)) continue;

            Port port = new Port(loc);
            for (MachineTile m : adjacentMachines(plugin, loc)) {
                if (m == null || m.energyStack == null || usedSources.contains(m)) continue;
                long amount = extractable(m.energyStack, interval);
                if (amount <= 0) continue;
                usedSources.add(m);
                port.add(m, amount);
            }
            if (port.total > 0) sources.add(port);
        }
        if (sources.isEmpty()) return 0;

        // 2) odbiorniki (PULL) - maszyna będąca źródłem nie jest jednocześnie odbiornikiem
        for (Location loc : nodes) {
            Connector c = ch.getConnector(loc);
            if (c == null || c.connectorTypes() != ConnectorTypes.PULL || !isLoaded(loc)) continue;

            Port port = new Port(loc);
            for (MachineTile m : adjacentMachines(plugin, loc)) {
                if (m == null || m.energyStack == null
                        || usedSources.contains(m) || usedSinks.contains(m)) continue;
                long amount = insertable(m.energyStack, interval);
                if (amount <= 0) continue;
                usedSinks.add(m);
                port.add(m, amount);
            }
            if (port.total > 0) sinks.add(port);
        }
        if (sinks.isEmpty()) return 0;

        // rotacja kolejności -> żaden connector nie jest stale faworyzowany
        Collections.rotate(sources, -rotation);
        Collections.rotate(sinks, -rotation);
        rotation++;

        // 3) graf przepływowy: 0 = super-źródło, 1 = super-ujście, 2.. = connectory
        Map<Location, Integer> index = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) index.put(nodes.get(i), i + 2);

        Flow flow = new Flow(nodes.size() + 2);
        for (Edge e : edges()) {
            long cap = (long) Math.max(0, e.limit()) * interval;
            flow.addArc(index.get(e.a()), index.get(e.b()), cap, cap); // kabel działa w obie strony
        }
        for (Port p : sources) p.arc = flow.addArc(0, index.get(p.loc), p.total, 0);
        for (Port p : sinks) p.arc = flow.addArc(index.get(p.loc), 1, p.total, 0);

        long total = flow.maxFlow(0, 1);
        if (total <= 0) return 0;

        // 4) rozdzielenie wyniku na maszyny
        for (Port p : sources) {
            long amount = p.arc.flow();
            for (int i = 0; i < p.machines.size() && amount > 0; i++) {
                MachineTile m = p.machines.get(i);
                long take = Math.min(amount, p.caps.get(i));
                m.energyStack.setEnergy(m.energyStack.getEnergy() - take);
                m.dirty = true;
                amount -= take;
            }
        }
        for (Port p : sinks) {
            long amount = p.arc.flow();
            for (int i = 0; i < p.machines.size() && amount > 0; i++) {
                MachineTile m = p.machines.get(i);
                long give = Math.min(amount, p.caps.get(i));
                m.energyStack.setEnergy(m.energyStack.getEnergy() + give);
                m.dirty = true;
                amount -= give;
            }
        }

        lastTransferred = total;
        return total;
    }

    // ------------------------------------------------------------------ diagnostyka

    /** Zwraca linie z opisem, dlaczego (nie) płynie energia. Wyślij je graczowi / do konsoli. */
    public List<String> diagnose(MainPlugin plugin, int interval) {
        List<String> out = new ArrayList<>();
        out.add("Sieć " + id + ": węzły=" + adjacency.size() + ", kable=" + edges().size()
                + ", ostatnio przesłano=" + lastTransferred);

        ConnectorHandler ch = plugin.getConnectorHandler();
        for (Location loc : adjacency.keySet()) {
            String pos = loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
            Connector c = ch.getConnector(loc);
            if (c == null) {
                out.add(pos + " -> BRAK connectora w ConnectorHandler (inny klucz Location?)");
                continue;
            }
            out.add(pos + " tryb=" + c.connectorTypes()
                    + ", chunk=" + (isLoaded(loc) ? "załadowany" : "NIE załadowany")
                    + ", kable=" + degree(loc));

            List<MachineTile> machines = adjacentMachines(plugin, loc);
            if (machines.isEmpty()) {
                out.add("    brak maszyny obok (getMachine zwróciło null dla wszystkich 6 stron)");
            }
            for (MachineTile m : machines) {
                EnergyStack s = m.energyStack;
                if (s == null) {
                    out.add("    maszyna bez energyStack");
                    continue;
                }
                out.add("    maszyna: energia=" + s.getEnergy() + "/" + s.getMaxEnergy()
                        + ", maxIn=" + s.getMaxInputTransfer() + ", maxOut=" + s.getMaxOutputTransfer()
                        + ", extractable=" + extractable(s, interval)
                        + ", insertable=" + insertable(s, interval));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ pomocnicze

    private static boolean isLoaded(Location loc) {
        return loc.getWorld() != null
                && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    private static List<MachineTile> adjacentMachines(MainPlugin plugin, Location loc) {
        List<MachineTile> list = new ArrayList<>(2);
        for (BlockFace f : FACES) {
            Location n = new Location(
                    loc.getWorld(),
                    loc.getBlockX() + f.getModX(),
                    loc.getBlockY() + f.getModY(),
                    loc.getBlockZ() + f.getModZ()
            );
            MachineTile m = plugin.getMachineTileHandler().getMachineTile(n);
            if (m != null) list.add(m);
        }
        return list;
    }

    private static long extractable(EnergyStack s, int interval) {
        return Math.max(0L, Math.min(s.getEnergy(), s.getMaxOutputTransfer() * interval));
    }

    private static long insertable(EnergyStack s, int interval) {
        return Math.max(0L, Math.min(s.getMaxEnergy() - s.getEnergy(), s.getMaxInputTransfer() * interval));
    }

    /** Connector PUSH/PULL razem z maszynami obok i ich limitami w tym ticku. */
    private static final class Port {
        final Location loc;
        final List<MachineTile> machines = new ArrayList<>(2);
        final List<Long> caps = new ArrayList<>(2);
        long total;
        Arc arc;

        Port(Location loc) {
            this.loc = loc;
        }

        void add(MachineTile m, long cap) {
            machines.add(m);
            caps.add(cap);
            total += cap;
        }
    }

    // ------------------------------------------------------------------ Dinic

    private static final class Arc {
        final int to;
        final int rev;
        final long original;
        long cap;

        Arc(int to, int rev, long cap) {
            this.to = to;
            this.rev = rev;
            this.cap = cap;
            this.original = cap;
        }

        long flow() {
            return original - cap;
        }
    }

    private static final class Flow {
        private final List<List<Arc>> g = new ArrayList<>();
        private final int[] level;
        private final int[] it;

        Flow(int n) {
            for (int i = 0; i < n; i++) g.add(new ArrayList<>());
            level = new int[n];
            it = new int[n];
        }

        Arc addArc(int u, int v, long cap, long revCap) {
            Arc a = new Arc(v, g.get(v).size(), cap);
            Arc b = new Arc(u, g.get(u).size(), revCap);
            g.get(u).add(a);
            g.get(v).add(b);
            return a;
        }

        long maxFlow(int s, int t) {
            long total = 0;
            while (bfs(s, t)) {
                Arrays.fill(it, 0);
                long f;
                while ((f = dfs(s, t, Long.MAX_VALUE)) > 0) total += f;
            }
            return total;
        }

        private boolean bfs(int s, int t) {
            Arrays.fill(level, -1);
            Deque<Integer> q = new ArrayDeque<>();
            level[s] = 0;
            q.add(s);
            while (!q.isEmpty()) {
                int u = q.poll();
                for (Arc a : g.get(u)) {
                    if (a.cap > 0 && level[a.to] < 0) {
                        level[a.to] = level[u] + 1;
                        q.add(a.to);
                    }
                }
            }
            return level[t] >= 0;
        }

        private long dfs(int u, int t, long f) {
            if (u == t) return f;
            List<Arc> arcs = g.get(u);
            for (; it[u] < arcs.size(); it[u]++) {
                Arc a = arcs.get(it[u]);
                if (a.cap > 0 && level[a.to] == level[u] + 1) {
                    long d = dfs(a.to, t, Math.min(f, a.cap));
                    if (d > 0) {
                        a.cap -= d;
                        g.get(a.to).get(a.rev).cap += d;
                        return d;
                    }
                }
            }
            return 0;
        }
    }
}