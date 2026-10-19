package com.jakisniekot.powerGrid.power.machine.tile;

import com.jakisniekot.powerGrid.power.machine.EnergyStack;
import com.jakisniekot.powerGrid.power.machine.MachineType;
import com.jakisniekot.powerGrid.power.machine.gui.MachineGui;
import com.jakisniekot.powerGrid.util.BlockPos;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;

public class MachineTile extends MachineType {

    public final BlockPos pos;
    public MachineType machineType;
    public EnergyStack energyStack;
    public MachineProgress progress;
    public MachineUUID machineUUID;
    public ItemStack[] input;
    public ItemStack[] output;
    public MachineState state = MachineState.IDLE;
    public MachineGui gui;
    public boolean dirty;

    public MachineTile(
            BlockPos pos,
            MachineType machineType,
            MachineProgress progress,
            ItemStack[] inputs,
            ItemStack[] outputs
    ) {
        this(pos, machineType, progress, machineType.energyStack.copy(), inputs, outputs);
    }

    public MachineTile(
            BlockPos pos,
            MachineType machineType,
            MachineProgress progress,
            EnergyStack energyStack,
            ItemStack[] inputs,
            ItemStack[] outputs
    ) {
        super(
                machineType.typeID,
                energyStack,
                machineType.inputSlots,
                machineType.outputSlots
        );

        this.pos = pos;
        this.machineType = machineType;
        this.progress = progress;
        this.energyStack = energyStack;

        this.input = inputs;
        this.output = outputs;
    }

    //ItemStack rest = ItemTransfer.insert(input, handItem);
    // rest to reszta, którą oddajesz graczowi

    // gracz/lejek wyciąga z outputu
    //ItemStack out = ItemTransfer.extractAny(output, 64);
}
