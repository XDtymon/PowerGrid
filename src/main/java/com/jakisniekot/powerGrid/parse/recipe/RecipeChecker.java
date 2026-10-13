package com.jakisniekot.powerGrid.parse.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * SEMI AI MADE CLASS
 */
public class RecipeChecker {
    private final Recipe recipe;

    public RecipeChecker(Recipe recipe) {
        this.recipe = recipe;
    }

    public boolean canStart(ItemStack[] inputSlots, ItemStack[] outputSlots, long energy, String machineID) {
        if (recipe.getInputs() == null || recipe.getOutputs() == null) return false;

        return hasEnergy(energy)
                && hasInputs(inputSlots)
                && hasOutputSpace(outputSlots)
                && isWhitelistedMachine(machineID);
    }

    private boolean isWhitelistedMachine(String machineID) {
        return recipe.getAvailableMachinesID().contains(machineID);
    }

    private boolean hasEnergy(long energy) {
        return energy >= recipe.getEnergyPerTick();
    }

    private boolean hasInputs(ItemStack[] inputSlots) {
        // kopia ilości, żeby ten sam slot nie został policzony dwa razy
        int[] remaining = new int[inputSlots.length];
        for (int i = 0; i < inputSlots.length; i++) {
            remaining[i] = isEmpty(inputSlots[i]) ? 0 : inputSlots[i].getAmount();
        }

        for (ItemStack required : recipe.getInputs()) {
            if (isEmpty(required)) continue;

            int needed = required.getAmount();
            for (int i = 0; i < inputSlots.length && needed > 0; i++) {
                if (remaining[i] > 0 && required.isSimilar(inputSlots[i])) {
                    int taken = Math.min(remaining[i], needed);
                    remaining[i] -= taken;
                    needed -= taken;
                }
            }
            if (needed > 0) return false;
        }
        return true;
    }

    private boolean hasOutputSpace(ItemStack[] outputSlots) {
        // klonujemy sloty, żeby zasymulować wkładanie itemów
        ItemStack[] simulated = new ItemStack[outputSlots.length];
        for (int i = 0; i < outputSlots.length; i++) {
            simulated[i] = isEmpty(outputSlots[i]) ? null : outputSlots[i].clone();
        }

        for (ItemStack output : recipe.getOutputs()) {
            if (isEmpty(output)) continue;

            int left = output.getAmount();
            int maxStack = output.getMaxStackSize();

            // 1. dopełnij istniejące stacki
            for (int i = 0; i < simulated.length && left > 0; i++) {
                ItemStack slot = simulated[i];
                if (slot != null && slot.isSimilar(output)) {
                    int add = Math.min(maxStack - slot.getAmount(), left);
                    if (add > 0) {
                        slot.setAmount(slot.getAmount() + add);
                        left -= add;
                    }
                }
            }

            // 2. użyj pustych slotów
            for (int i = 0; i < simulated.length && left > 0; i++) {
                if (simulated[i] == null) {
                    int add = Math.min(maxStack, left);
                    ItemStack copy = output.clone();
                    copy.setAmount(add);
                    simulated[i] = copy;
                    left -= add;
                }
            }

            if (left > 0) return false;
        }
        return true;
    }

    private boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() <= 0;
    }
}