package com.jakisniekot.powerGrid.database;
/*
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RelayDAO {

    private final Connection SQL;
    private final String DB_TYPE;

    public RelayDAO(Connection SQL, String DB_TYPE) {
        this.SQL = SQL;
        this.DB_TYPE = DB_TYPE;
    }

    public void insertOrUpdate(String id, int maxConnections) {
        String query = switch (DB_TYPE) {
            case "SQLITE" -> """
                    INSERT INTO relays (id, max_connections) VALUES (?, ?)
                    ON CONFLICT(id) DO UPDATE SET max_connections = excluded.max_connections
                    """;
            case "MYSQL" -> """
                    INSERT INTO relays (id, max_connections) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE max_connections = VALUES(max_connections)
                    """;
            default -> null;
        };

        try (PreparedStatement ps = SQL.prepareStatement(query)) {
            ps.setString(1, id);
            ps.setInt(2, maxConnections);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Relay get(String id) {
        String query = "SELECT * FROM relays WHERE id = ?";
        try (PreparedStatement ps = SQL.prepareStatement(query)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Relay(rs.getString("id"), rs.getInt("max_connections"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

 */