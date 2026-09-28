/*
package com.jakisniekot.powerGrid.machine.gui;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

public class MachineMenu {



    private final Block block;
    private final Gui gui;

    private ItemStack inputSlot;
    private ItemStack fuelSlot;
    private ItemStack outputSlot;

    private int progress = 0;      // aktualny postęp w tickach
    private int fuelTicksLeft = 0; // ile jeszcze "pali się" paliwo

    private BukkitRunnable tickTask;

    public MachineGui(Block block) {
        this.block = block;

        // Layout 3x9: # to obramowanie, i=input, f=fuel, p=progress, o=output
        this.gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# i . f . p . o #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ")))
                .addIngredient('i', createInputSlotItem())
                .addIngredient('f', createFuelSlotItem())
                .addIngredient('p', createProgressItem())
                .addIngredient('o', createOutputSlotItem())
                .build();
    }

    private Item createInputSlotItem() {
        // W realnym kodzie użyj VISlotElement / BoundSlotElement z InvUI,
        // tutaj uproszczony placeholder pokazujący ideę.
        return new SimpleItem(new ItemBuilder(Material.HOPPER).setDisplayName("Wsad"));
    }

    private Item createFuelSlotItem() {
        return new SimpleItem(new ItemBuilder(Material.COAL).setDisplayName("Paliwo"));
    }

    private Item createOutputSlotItem() {
        return new SimpleItem(new ItemBuilder(Material.CHEST).setDisplayName("Wynik"));
    }

    private Item createProgressItem() {
        Material icon = progress > 0 ? Material.CLOCK : Material.GRAY_DYE;
        String pct = (progress * 100 / MachineRecipe.PROCESS_TIME_TICKS) + "%";
        return new SimpleItem(new ItemBuilder(icon).setDisplayName("Postep: " + pct));
    }
}

 */
