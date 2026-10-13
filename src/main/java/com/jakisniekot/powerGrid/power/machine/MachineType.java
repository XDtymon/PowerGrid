package com.jakisniekot.powerGrid.power.machine;

import com.jakisniekot.powerGrid.power.machine.tile.item.ItemSlot;

import java.util.List;

public class MachineType {

    public final String typeID;
    public EnergyStack energyStack;
    public List<ItemSlot> inputSlots;
    public List<ItemSlot> outputSlots;

    public MachineType(
            String typeID,
            EnergyStack energyStack,
            List<ItemSlot> inputSlots,
            List<ItemSlot> outputSlots
    ) {
        this.typeID = typeID;
        this.energyStack = energyStack;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
    }
}
