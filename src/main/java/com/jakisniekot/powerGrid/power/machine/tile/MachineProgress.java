package com.jakisniekot.powerGrid.power.machine.tile;

import com.jakisniekot.powerGrid.parse.recipe.Recipe;

public class MachineProgress {

    private long machineProgress;

    /**
     * Max machine progress
     */
    private long neededMachineProgress;

    private Recipe recipe;

    public MachineProgress(
            int machineProgress,
            int neededMachineProgress
    ) {
        this.machineProgress = machineProgress;
        this.neededMachineProgress = neededMachineProgress;
    }

    public MachineProgress(
            int machineProgress,
            int neededMachineProgress,
            Recipe recipe
    ) {
        this.machineProgress = machineProgress;
        this.neededMachineProgress = neededMachineProgress;
    }

    public boolean hasRecipe() {
        return recipe != null;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public void setMachineProgress(long machineProgress) {
        this.machineProgress = machineProgress;
    }

    public void setNeededMachineProgress(long neededMachineProgress) {
        this.neededMachineProgress = neededMachineProgress;
    }

    public boolean isFinished() {
        return machineProgress >= neededMachineProgress;
    }

    public void add() {
        setMachineProgress(machineProgress + 1L);
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public long getMachineProgress() {
        return machineProgress;
    }

    public long getNeededMachineProgress() {
        return neededMachineProgress;
    }

    public double getPercentage() {
        return Math.round((float) machineProgress / neededMachineProgress) * 100;
    }
}
