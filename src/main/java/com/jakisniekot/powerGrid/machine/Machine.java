package com.jakisniekot.powerGrid.machine;

import com.jakisniekot.powerGrid.MainPlugin;

import java.util.UUID;

public class Machine {

    private final MachineType machineType;
    private final UUID uuid;
    private final EnergyStorage energyStorage;
    private final ItemStorage itemStorage;

    public Machine(
            MachineType machineType,
            UUID uuid,
            EnergyStorage energyStorage,
            ItemStorage itemStorage
    ) {
        this.machineType = machineType;
        this.uuid = uuid;
        this.energyStorage = energyStorage;
        this.itemStorage = itemStorage;
    }

}

