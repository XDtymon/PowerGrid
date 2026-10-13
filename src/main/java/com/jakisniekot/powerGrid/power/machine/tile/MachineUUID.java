package com.jakisniekot.powerGrid.power.machine.tile;

import java.util.UUID;

public class MachineUUID {
    private final UUID ownerUUID;
    private final UUID machineUUID;
    private final String machineID;

    public MachineUUID(
            UUID ownerUUID,
            UUID machineUUID,
            String machineID
    ) {
        this.ownerUUID = ownerUUID;
        this.machineID = machineID;
        this.machineUUID = machineUUID;
    }

    public String getMachineID() {
        return machineID;
    }

    public UUID getMachineUUID() {
        return machineUUID;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }
}
