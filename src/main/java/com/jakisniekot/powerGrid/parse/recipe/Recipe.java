package com.jakisniekot.powerGrid.parse.recipe;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.parse.exceptions.RecipeParseError;
import com.jakisniekot.powerGrid.util.ItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Recipe {
    private String id;
    private ItemStack[] inputs;
    private ItemStack[] outputs;
    private long neededProgressTime;
    private long energyPerTick;
    private Set<String> availableMachines;

    public Recipe(ConfigurationSection section, MainPlugin plugin, String id) {

        if (section.getConfigurationSection("inputs") == null) {
            plugin.errorHandler().print(RecipeParseError.INPUT_LIST_ITEM, id);
            return;
        }

        if (section.getConfigurationSection("outputs") == null) {
            plugin.errorHandler().print(RecipeParseError.OUTPUT_LIST_ITEM, id);
            return;
        }

        Set<String> inputsList = section.getConfigurationSection("inputs").getKeys(false);
        Set<String> outputsList = section.getConfigurationSection("outputs").getKeys(false);
        long processTime = section.getLong("processTime");
        long energyPerTick = section.getLong("energyPerTick", 0L);

        if (processTime == 0) {
            processTime = 20;
            plugin.errorHandler().print(RecipeParseError.PROCESS_TIME, id);
        }



        List<String> machinesList = section.getStringList("machines");
        this.availableMachines = new HashSet<>(machinesList);

        ItemStack[] inputs = new ItemStack[inputsList.size()];
        ItemStack[] outputs = new ItemStack[outputsList.size()];




        int i = 0;

        for (String ID : inputsList) {
            inputs[i] = ItemUtil.item(
                    ID,
                    section.getConfigurationSection("inputs"),
                    plugin
            );

            i++;
        }

        i = 0;

        for (String ID : inputsList) {
            inputs[i] = ItemUtil.item(
                    ID,
                    section.getConfigurationSection("outputs"),
                    plugin
            );

            i++;
        }

        this.energyPerTick = energyPerTick;
        this.neededProgressTime = processTime;
        this.inputs = inputs;
        this.outputs = outputs;

        this.id = id;

        plugin.errorHandler().print(RecipeParseError.NONE, id);
    }

    public Set<String> getAvailableMachinesID() {
        return availableMachines;
    }

    public ItemStack[] getInputs() {
        return inputs;
    }

    public ItemStack[] getOutputs() {
        return outputs;
    }

    public long getEnergyPerTick() {
        return energyPerTick;
    }

    public long getNeededProgressTime() {
        return neededProgressTime;
    }

    public String getId() {
        return id;
    }

    /*
            AI MADE SAMPLE |
                          \/
         */
    public boolean isForMachine(String machineId) {
        return availableMachines.contains(machineId);
    }

    /** Czy w inputach maszyny jest wszystko, czego wymaga recipe. */
    public boolean hasInputs(ItemStack[] machineInput) {
        // pracujemy na licznikach, żeby dwa wymagania na ten sam item nie liczyły się podwójnie
        int[] available = new int[machineInput.length];
        for (int i = 0; i < machineInput.length; i++) {
            available[i] = machineInput[i] == null ? 0 : machineInput[i].getAmount();
        }

        for (ItemStack required : inputs) {
            int needed = required.getAmount();
            for (int i = 0; i < machineInput.length && needed > 0; i++) {
                ItemStack have = machineInput[i];
                if (have == null || available[i] <= 0 || !have.isSimilar(required)) continue;
                int take = Math.min(available[i], needed);
                available[i] -= take;
                needed -= take;
            }
            if (needed > 0) return false;
        }
        return true;
    }

    public boolean canFitOutputs(ItemStack[] machineOutput) {
        ItemStack[] sim = new ItemStack[machineOutput.length];
        for (int i = 0; i < sim.length; i++) {
            sim[i] = machineOutput[i] == null ? null : machineOutput[i].clone();
        }

        for (ItemStack out : outputs) {
            int left = out.getAmount();

            for (ItemStack slot : sim) {
                if (left <= 0) break;
                if (slot != null && slot.isSimilar(out)) {
                    int space = slot.getMaxStackSize() - slot.getAmount();
                    int add = Math.min(space, left);
                    slot.setAmount(slot.getAmount() + add);
                    left -= add;
                }
            }

            for (int i = 0; i < sim.length && left > 0; i++) {
                if (sim[i] == null) {
                    int add = Math.min(out.getMaxStackSize(), left);
                    ItemStack copy = out.clone();
                    copy.setAmount(add);
                    sim[i] = copy;
                    left -= add;
                }
            }
            if (left > 0) return false;
        }
        return true;
    }

    public boolean canRun(String machineId, ItemStack[] in, ItemStack[] out, long energy) {
        return isForMachine(machineId)
                && energy >= energyPerTick
                && hasInputs(in)
                && canFitOutputs(out);
    }
}
