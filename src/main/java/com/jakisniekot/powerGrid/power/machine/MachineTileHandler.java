package com.jakisniekot.powerGrid.power.machine;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.power.machine.tile.MachineProgress;
import com.jakisniekot.powerGrid.power.machine.tile.MachineState;
import com.jakisniekot.powerGrid.power.machine.tile.MachineTile;
import com.jakisniekot.powerGrid.parse.recipe.Recipe;
import com.jakisniekot.powerGrid.util.BlockPos;
import com.jakisniekot.powerGrid.util.ItemTransfer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class MachineTileHandler extends BukkitRunnable {

    private MainPlugin plugin;

    private Map<Location, MachineTile> machineTiles = new HashMap<>();

    public MachineTileHandler(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (MachineTile tile : machineTiles.values()) {
            if (tile.gui != null) tile.gui.pull();
            tick(tile);
            if (tile.gui != null) tile.gui.refresh();
        }
    }

    public void tick(MachineTile machineTile) {

        if (machineTile.progress.hasRecipe()) {

            Recipe recipe = machineTile.progress.getRecipe();
            if (recipe.canRun(
                    machineTile.machineUUID.getMachineID(),
                    machineTile.input,
                    machineTile.output,
                    machineTile.energyStack.getEnergy()
            )) {

                if (machineTile.progress.isFinished()) {
                    if (ItemTransfer.insertAll(machineTile.output, recipe.getOutputs())) {
                        machineTile.progress.setRecipe(null);
                        machineTile.progress.setMachineProgress(0L);
                        machineTile.state = MachineState.IDLE;
                        machineTile.dirty = true;
                    } else {
                        machineTile.state = MachineState.OUTPUT_FULL;
                    }

                }

                machineTile.progress.add();
                machineTile.energyStack.setEnergy(machineTile.energyStack.getEnergy() - recipe.getEnergyPerTick());
            } else {
                machineTile.state = MachineState.ENERGY_ERROR;
            }


        } else {

            Recipe recipe = findRecipe(machineTile.machineType.typeID, machineTile.input, machineTile.output);
            if (recipe != null) {
                setRecipe(machineTile.progress, recipe);
            }

            machineTile.state = MachineState.WORKING;
            machineTile.dirty = true;

        }
    }

    public Recipe findRecipe(String machineType, ItemStack[] input, ItemStack[] output) {
        for (Recipe r : plugin.recipes().getRecipesForMachine(machineType)) {
            if (r.hasInputs(input) && r.canFitOutputs(output)) return r;
        }
        return null;
    }

    public void setRecipe(MachineProgress progress, Recipe recipe) {
        progress.setRecipe(recipe);
        progress.setMachineProgress(0);
        progress.setNeededMachineProgress(recipe.getNeededProgressTime());
    }

    public void newMachineTile(String machineID, Location location) {
        addMachineTile(
                location,
                new MachineTile(
                        new BlockPos(
                                location.getWorld(),
                                location.getBlockX(),
                                location.getBlockY(),
                                location.getBlockZ()
                        ),
                        plugin.getMachineTypeHandler().getMachineType(machineID),
                        new MachineProgress(0, 0),
                        new ItemStack[]{},
                        new ItemStack[]{}
                )
        );
    }

    public Map<Location, MachineTile> getMachineTiles() {
        return machineTiles;
    }

    public MachineTile getMachineTile(Location location) {
        return machineTiles.get(location);
    }

    public void addMachineTile(Location location, MachineTile machineTile) {
        machineTiles.put(location, machineTile);
    }

    public boolean isMachineTile(Location location) {
        return machineTiles.containsKey(location);
    }

    public void addFromDB() {
        Map<Location, MachineTile> machines = plugin.getMachineDAO().getAll();
        if (machines != null && !machines.isEmpty()) {
            machineTiles.putAll(machines);
        }
    }

    public void removeMachineTile(Location location) {
        MachineTile tile = machineTiles.remove(location);
    }

}
