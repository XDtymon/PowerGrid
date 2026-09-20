package com.jakisniekot.powerGrid.database;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.actions.wire.WireLine;
import com.jakisniekot.powerGrid.util.LocationIDString;
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
import java.util.List;
import java.util.Objects;

public class WireDAO {

    private final Connection sql;
    private final String DB_TYPE;

    private MainPlugin plugin;

    public WireDAO(Connection sql, String DB_TYPE, MainPlugin plugin) {
        this.sql = sql;
        this.DB_TYPE = DB_TYPE;
        this.plugin = plugin;
    }

    public void insertOrUpdate(String loc1, String loc2, int wattTransfer, String itemStackString, String itemID) {
        String query = switch (DB_TYPE) {
            case "SQLITE" -> """
                    INSERT INTO wires (loc1, loc2, watt_transfer, headBlocks, itemID) VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT(loc1, loc2) DO UPDATE SET
                        watt_transfer = excluded.watt_transfer,
                        headBlocks = excluded.headBlocks,
                        itemID = excluded.itemID
                    """;
            case "MYSQL" -> """
                    INSERT INTO wires (loc1, loc2, watt_transfer, headBlocks, itemID) VALUES (?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        watt_transfer = VALUES(watt_transfer),
                        headBlocks = VALUES(headBlocks),
                        itemID = VALUES(itemID);
                    """;
            default -> null;
        };


        try (PreparedStatement ps = sql.prepareStatement(query)) {
            ps.setString(1, loc1);
            ps.setString(2, loc2);
            ps.setInt(3, wattTransfer);
            ps.setString(4, itemStackString);
            ps.setString(5, itemID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Wire> getConnected(String locId) {
        List<Wire> result = new ArrayList<>();
        String query = "SELECT * FROM wires WHERE loc1 = ? OR loc2 = ?";

        try (PreparedStatement ps = sql.prepareStatement(query)) {
            ps.setString(1, locId);
            ps.setString(2, locId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Wire(
                            rs.getString("loc1"),
                            rs.getString("loc2"),
                            rs.getInt("watt_transfer"),
                            rs.getString("headBlocks"),
                            rs.getString("itemID")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Wire> getAll() {
        List<Wire> result = new ArrayList<>();
        String query = "SELECT loc1, loc2, watt_transfer, headBlocks, itemID FROM wires";

        try (PreparedStatement ps = sql.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new Wire(
                        rs.getString("loc1"),
                        rs.getString("loc2"),
                        rs.getInt("watt_transfer"),
                        rs.getString("headBlocks"),
                        rs.getString("itemID")

                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void reinstateWires() {
        List<Wire> wires = getAll();
        for (Wire wire : wires) {
            new WireLine(
                    plugin,
                    LocationIDString.getLocation(wire.loc1()).add(0.5, 0, 0.5),
                    LocationIDString.getLocation(wire.loc2()).add(0.5, 0, 0.5),
                    wire.headItems()
            ).place();
        }
    }

    public boolean isAlreadyConnected(String loc1, String loc2) {
        for (Wire wire : getConnected(loc1)) {
            if (Objects.equals(wire.loc2(), loc2) || Objects.equals(wire.loc1(), loc2)) {
                return true;
            }
        }
        return false;
    }

    public List<Wire> getConnectedAndDelete(String locId) {
        List<Wire> deleted = getConnected(locId);

        String query = "DELETE FROM wires WHERE loc1 = ? OR loc2 = ?";

        try (PreparedStatement ps = sql.prepareStatement(query)) {
            ps.setString(1, locId);
            ps.setString(2, locId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        for (Wire wire : deleted) {
            removeWireStandsFor(wire.loc1(), wire.loc2());
        }

        return deleted;
    }


    public static void removeWireStandsFor(String loc1Id, String loc2Id) {
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                PersistentDataContainer pdc = stand.getPersistentDataContainer();

                String first = pdc.get(KeyUtil.FirstWireStandKey(), PersistentDataType.STRING);
                String second = pdc.get(KeyUtil.SecondWireStandKey(), PersistentDataType.STRING);

                boolean matches = (loc1Id.equals(first) && loc2Id.equals(second))
                        || (loc1Id.equals(second) && loc2Id.equals(first));

                if (matches) {
                    stand.remove();
                }
            }
        }
    }
}