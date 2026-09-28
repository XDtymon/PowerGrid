package com.jakisniekot.powerGrid.recipe;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.util.ItemStringUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Recipe {
    private ItemStack[] inputs;
    private ItemStack[] outputs;
    private long processTime;
    private long energyPerTick;
    private Set<String> availableMachines;

    public RecipeErrors parseRecipe(ConfigurationSection section, MainPlugin plugin) {
        if (section.getConfigurationSection("inputs") == null) {
            return RecipeErrors.INPUT_LIST_ITEM;
        }

        if (section.getConfigurationSection("outputs") == null) {
            return RecipeErrors.OUTPUT_LIST_ITEM;
        }

        Set<String> inputsList = section.getConfigurationSection("inputs").getKeys(false);
        Set<String> outputsList = section.getConfigurationSection("outputs").getKeys(false);
        long processTime = section.getLong("processTime", 20);
        long energyPerTick = section.getLong("energyPerTick", 0);

        List<String> machinesList = section.getStringList("machines");
        this.availableMachines = new HashSet<>(machinesList);

        ItemStringUtil itemStringUtil = new ItemStringUtil();


        ItemStack[] inputs = new ItemStack[inputsList.size()];
        ItemStack[] outputs = new ItemStack[outputsList.size()];

        int i = 0;

        for (String ID : inputsList) {
            ItemStack itemStack = section.getConfigurationSection("inputs").getItemStack(ID);
            if (itemStack == null) {
                String itemString = section.getConfigurationSection("inputs").getString(ID);
                if (itemString == null) {
                    return RecipeErrors.INPUT_LIST_ITEM;
                }
                itemStack = itemStringUtil.item(itemString, plugin);
                inputs[i] = itemStack;

            } else {
                inputs[i] = itemStack;
            }

            i++;
        }

        i = 0;



        for (String ID : inputsList) {
            ItemStack itemStack = section.getConfigurationSection("outputs").getItemStack(ID);
            if (itemStack == null) {
                String itemString = section.getConfigurationSection("outputs").getString(ID);
                if (itemString == null) {
                    return RecipeErrors.INPUT_LIST_ITEM;
                }
                itemStack = itemStringUtil.item(itemString, plugin);
                outputs[i] = itemStack;

            } else {
                outputs[i] = itemStack;
            }

            i++;
        }

        this.energyPerTick = energyPerTick;
        this.processTime = processTime;
        this.inputs = inputs;
        this.outputs = outputs;

        return RecipeErrors.NONE;
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

    public long getProcessTime() {
        return processTime;
    }

}
