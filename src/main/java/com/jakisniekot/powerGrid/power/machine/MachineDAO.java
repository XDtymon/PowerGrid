
package com.jakisniekot.powerGrid.power.machine;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.power.machine.tile.MachineProgress;
import com.jakisniekot.powerGrid.power.machine.tile.MachineTile;
import com.jakisniekot.powerGrid.util.*;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class MachineDAO {

    private final Connection SQL;
    private MainPlugin plugin;
    private String DB_TYPE;

    public MachineDAO(Connection SQL, String DB_TYPE, MainPlugin plugin) {
        this.SQL = SQL;
        this.DB_TYPE = DB_TYPE;
        this.plugin = plugin;
    }

    public void saveAllMachinery() {

        Map<Location, MachineTile> machines = plugin.getMachineTileHandler().getMachineTiles();

        for (Location machineLocation : machines.keySet()) {
            MachineTile machineTile = machines.get(machineLocation);
            String locationID = LocationIDString.getString(machineLocation);
            insertOrUpdate(
                    locationID,
                    machineTile.machineType,
                    machineTile.energyStack,
                    machineTile.progress,
                    machineTile.input,
                    machineTile.output
            );


        }
    }

    public void insertOrUpdate(
            String locationID,
            MachineType machineType,
            EnergyStack energyStack,
            MachineProgress machineProgress,
            ItemStack[] inputs,
            ItemStack[] outputs
    ) {

        String query = switch (DB_TYPE) {
            case "MYSQL" ->  """
                    INSERT INTO machines
                        (locationID, machineID, energy, maxEnergy, inputEnergy,
                         outputEnergy, progress, recipeID, inputItems, outputItems)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        machineID    = VALUES(machineID),
                        energy       = VALUES(energy),
                        maxEnergy    = VALUES(maxEnergy),
                        inputEnergy  = VALUES(inputEnergy),
                        outputEnergy = VALUES(outputEnergy),
                        progress     = VALUES(progress),
                        recipeID     = VALUES(recipeID),
                        inputItems   = VALUES(inputItems),
                        outputItems  = VALUES(outputItems)
                    """;
            case "SQLITE" -> """
                    INSERT INTO machines
                        (locationID, machineID, energy, maxEnergy, inputEnergy,
                         outputEnergy, progress, recipeID, inputItems, outputItems)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(locationID) DO UPDATE SET
                        machineID    = excluded.machineID,
                        energy       = excluded.energy,
                        maxEnergy    = excluded.maxEnergy,
                        inputEnergy  = excluded.inputEnergy,
                        outputEnergy = excluded.outputEnergy,
                        progress     = excluded.progress,
                        recipeID     = excluded.recipeID,
                        inputItems   = excluded.inputItems,
                        outputItems  = excluded.outputItems
                    """;
            default -> null;
        };

        String recipeID;
        if (!machineProgress.hasRecipe()) {
            recipeID = "none";
        } else {
            recipeID = machineProgress.getRecipe().getId();
        }

        try (PreparedStatement ps = SQL.prepareStatement(query)) {
            ps.setString(1, locationID);
            ps.setString(2, machineType.typeID);
            ps.setLong(3, energyStack.getEnergy());
            ps.setLong(4, energyStack.getMaxEnergy());
            ps.setLong(5, energyStack.getMaxInputTransfer());
            ps.setLong(6, energyStack.getMaxOutputTransfer());
            ps.setLong(7, machineProgress.getMachineProgress());
            ps.setString(8, recipeID);
            ps.setBytes(9, ItemStack.serializeItemsAsBytes(inputs));
            ps.setBytes(10, ItemStack.serializeItemsAsBytes(outputs));
            int rows = ps.executeUpdate();
            plugin.getLogger().info("Saved " + locationID + " rows=" + rows
                    + " autoCommit=" + SQL.getAutoCommit()
                    + " closed=" + SQL.isClosed()
                    + " url=" + SQL.getMetaData().getURL());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public MachineTile get(String id) {
        String query = "SELECT * FROM machines WHERE locationID = ?";

        try (PreparedStatement ps = SQL.prepareStatement(query)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Location location = LocationIDString.getLocation(rs.getString("locationID"));
                    String recipeID = rs.getString("recipeID");
                    com.jakisniekot.powerGrid.parse.recipe.Recipe recipe = plugin.recipes().getRecipe(recipeID);
                    MachineProgress machineProgressObject;
                    if (recipe == null) {
                        recipeID = "none";
                        machineProgressObject = new MachineProgress(
                                0, 0
                        );
                    } else {
                        int machineProgress = rs.getInt("progress");
                        int maxMachineProgress = Math.toIntExact(recipe.getNeededProgressTime());
                        machineProgressObject = new MachineProgress(
                                machineProgress,
                                maxMachineProgress,
                                plugin.recipes().getRecipe(recipeID)
                        );
                    }

                    MachineType machineType = plugin.getMachineTypeHandler().getMachineType(rs.getString("machineID"));
                    if (machineType == null) return null;

                    return new MachineTile(
                            new BlockPos(
                                    location.getWorld(),
                                    location.getBlockX(),
                                    location.getBlockY(),
                                    location.getBlockZ()
                            ),
                            machineType,
                            machineProgressObject,
                            new EnergyStack(
                                    rs.getLong("energy"),
                                    rs.getLong("maxEnergy"),
                                    rs.getLong("inputEnergy"),
                                    rs.getLong("outputEnergy")
                            ),
                            ItemStackChanger.getItemsFromByte(
                                    rs.getBytes("inputItems")
                            ),
                            ItemStackChanger.getItemsFromByte(
                                    rs.getBytes("outputItems")
                            )
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public Map<Location, MachineTile> getAll() {
        Map<Location, MachineTile> result = new HashMap<>();
        String query = "SELECT * FROM machines";

        try (PreparedStatement ps = SQL.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Location location = LocationIDString.getLocation(rs.getString("locationID"));
                String recipeID = rs.getString("recipeID");
                com.jakisniekot.powerGrid.parse.recipe.Recipe recipe = plugin.recipes().getRecipe(recipeID);
                MachineProgress machineProgressObject;
                if (recipe == null) {
                    recipeID = "none";
                    machineProgressObject = new MachineProgress(
                    0, 0
                    );
                } else {
                    int machineProgress = rs.getInt("progress");
                    int maxMachineProgress = Math.toIntExact(recipe.getNeededProgressTime());
                        machineProgressObject = new MachineProgress(
                                machineProgress,
                                maxMachineProgress,
                                plugin.recipes().getRecipe(recipeID)
                        );
                }

                MachineType machineType = plugin.getMachineTypeHandler().getMachineType(rs.getString("machineID"));
                if (machineType == null) return null;

                result.put(
                        location,
                        new MachineTile(
                        new BlockPos(
                                location.getWorld(),
                                location.getBlockX(),
                                location.getBlockY(),
                                location.getBlockZ()
                        ),
                        machineType,
                        machineProgressObject,
                        new EnergyStack(
                                rs.getLong("energy"),
                                rs.getLong("maxEnergy"),
                                rs.getLong("inputEnergy"),
                                rs.getLong("outputEnergy")
                        ),
                        ItemStackChanger.getItemsFromByte(
                                rs.getBytes("inputItems")
                        ),
                        ItemStackChanger.getItemsFromByte(
                                rs.getBytes("outputItems")
                        )

                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void delete(String id) {
        String query = "DELETE FROM machines WHERE locationID = ?";
        try (PreparedStatement ps = SQL.prepareStatement(query)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
