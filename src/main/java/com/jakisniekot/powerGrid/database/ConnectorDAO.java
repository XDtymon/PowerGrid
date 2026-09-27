package com.jakisniekot.powerGrid.database;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.actions.connector.ConnectorTypes;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ConnectorDAO {

    private final Connection SQL;
    private MainPlugin plugin;
    private String DB_TYPE;

    public ConnectorDAO(Connection SQL, String DB_TYPE, MainPlugin plugin) {
        this.SQL = SQL;
        this.DB_TYPE = DB_TYPE;
        this.plugin = plugin;
    }

    public void insertOrUpdate(String id, int maxConnections, List<String> allowedWires, boolean blacklist, ConnectorTypes connectorTypes, String itemID) {

        String query = switch (DB_TYPE) {
            case "SQLITE" -> """
                    INSERT INTO connectors (id, max_connections, allowed_wires, blacklist, connectorType, itemID) VALUES (?, ?, ?, ?, ?, ?)
                    ON CONFLICT(id) DO UPDATE SET
                        max_connections = excluded.max_connections,
                        allowed_wires = excluded.allowed_wires,
                        blacklist = excluded.blacklist,
                        connectorType = excluded.connectorType,
                        itemID = excluded.itemID
                    """;
            case "MYSQL" -> """
                    INSERT INTO connectors (id, max_connections, allowed_wires, blacklist, connectorType, itemID)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        max_connections = VALUES(max_connections),
                        allowed_wires = VALUES(allowed_wires),
                        blacklist = VALUES(blacklist),
                        connectorType = VALUES(connectorType),
                        itemID = VALUES(itemID);
                    """;
            default -> null;
        };


        try (PreparedStatement ps = SQL.prepareStatement(query)) {
            ps.setString(1, id);
            ps.setInt(2, maxConnections);
            ps.setString(3, String.join(",", allowedWires));
            ps.setBoolean(4, blacklist);
            ps.setString(5, connectorTypes.name());
            ps.setString(6, itemID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateType(String id, ConnectorTypes newType) {
        String query = "UPDATE connectors SET connectorType = ? WHERE id = ?";

        try (PreparedStatement ps = SQL.prepareStatement(query)) {
            ps.setString(1, newType.name());
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Connector get(String id) {
        String query = "SELECT * FROM connectors WHERE id = ?";

        try (PreparedStatement ps = SQL.prepareStatement(query)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    List<String> wires = rs.getString("allowed_wires") == null
                            || rs.getString("allowed_wires").isEmpty()
                            ? new ArrayList<>()
                            : new ArrayList<>(Arrays.asList(rs.getString("allowed_wires").split(",")));

                    return new Connector(
                            rs.getString("id"),
                            rs.getInt("max_connections"),
                            wires,
                            rs.getBoolean("blacklist"),
                            ConnectorTypes.valueOf(rs.getString("connectorType")),
                            rs.getString("itemID")
                    );
                }
            }
        } catch (SQLException e) {
        }

        return null;
    }

    public List<Connector> getAll() {
        List<Connector> result = new ArrayList<>();
        String query = "SELECT * FROM connectors";

        try (PreparedStatement ps = SQL.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String rawWires = rs.getString("allowed_wires");
                List<String> wires = (rawWires == null || rawWires.isEmpty())
                        ? new ArrayList<>()
                        : new ArrayList<>(Arrays.asList(rawWires.split(",")));

                result.add(new Connector(
                        rs.getString("id"),
                        rs.getInt("max_connections"),
                        wires,
                        rs.getBoolean("blacklist"),
                        ConnectorTypes.valueOf(rs.getString("connectorType")),
                        rs.getString("itemID")
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void delete(String id) {
        String query = "DELETE FROM connectors WHERE id = ?";
        try (PreparedStatement ps = SQL.prepareStatement(query)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteConnectorWithWire(String id) {
        delete(id);
        plugin.getWireDAO().getConnectedAndDelete(id);
    }
}