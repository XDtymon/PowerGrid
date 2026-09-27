package com.jakisniekot.powerGrid.network;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jedna spójna grupa connectorów połączonych ze sobą wirami.
 * Sieć nie trzyma samych krawędzi (te zostają w WireDAO) - tylko zbiór id connectorów,
 * które do niej należą, żeby PowerTransferTask mógł potraktować każdy connector PUSH
 * jako możliwe źródło dla każdego connectora PULL w tym samym zbiorze.
 */
public class PowerNetwork {

    private final UUID id;
    private final Set<String> connectorIds = ConcurrentHashMap.newKeySet();

    public PowerNetwork(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public Set<String> getConnectorIds() {
        return connectorIds;
    }

    public void add(String connectorId) {
        connectorIds.add(connectorId);
    }

    public void remove(String connectorId) {
        connectorIds.remove(connectorId);
    }

    public boolean isEmpty() {
        return connectorIds.isEmpty();
    }

    public int size() {
        return connectorIds.size();
    }
}