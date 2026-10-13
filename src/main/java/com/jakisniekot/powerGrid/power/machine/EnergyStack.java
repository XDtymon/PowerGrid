package com.jakisniekot.powerGrid.power.machine;

import org.bukkit.configuration.ConfigurationSection;

public class EnergyStack {
    private long energy;
    private long maxEnergy;
    private long maxInputTransfer;
    private long maxOutputTransfer;

    private int slotID;

    public EnergyStack(
            Long energy,
            Long maxEnergy,
            Long maxInputTransfer,
            Long maxOutputTransfer
    ) {
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.maxInputTransfer = maxInputTransfer;
        this.maxOutputTransfer = maxOutputTransfer;
    }

    public void parse(ConfigurationSection section) {
        this.energy = section.getLong("energy" ,0L);
        this.maxEnergy = section.getLong("maximumEnergy", 100L);
        this.maxInputTransfer = section.getLong("maxInputTransfer", 0L);
        this.maxOutputTransfer = section.getLong("maxOutputTransfer", 0L);

        this.slotID = section.getInt("slot");
    }

    public EnergyStack copy() {
        EnergyStack c = new EnergyStack(energy, maxEnergy, maxInputTransfer, maxOutputTransfer);
        c.slotID = this.slotID;
        return c;
    }

    public boolean hasAvailableEnergy(long changeEnergy) {
        return energy >= changeEnergy;
    }

    public long getExtractable() {
        return Math.min(this.energy, this.maxOutputTransfer);
    }

    public long getInsertable() {
        return Math.min(this.energy, this.maxInputTransfer);
    }

    public long takeEnergy(long energy) {
        if (energy <= 0L) return 0L;
        long newEnergy = this.energy - energy;
        if (this.energy - energy < 0) this.energy = 0;
        if (energy > maxOutputTransfer) {
            takeEnergyLiteral(maxOutputTransfer);
            return energy - maxOutputTransfer;
        }

        setEnergy(newEnergy);
        return 0L;
    }

    public void takeEnergyLiteral(long energy) {
        this.energy = this.energy - energy;
    }

    public long addEnergy(long energy) {
        if (energy <= 0) return 0L;
        long newEnergy = this.energy + energy;
        if (energy > maxInputTransfer) {
            addEnergyLiteral(maxInputTransfer);
            return newEnergy - maxInputTransfer;
        }

        setEnergy(newEnergy);
        return 0L;
    }

    public void addEnergyLiteral(long energy) {
        this.energy = this.energy + energy;
    }

    public void setEnergy(long energy) {
        this.energy = energy;
    }

    public long getEnergy() {
        return energy;
    }

    public void setMaxEnergy(long maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    public long getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxInputTransfer(long maxInputTransfer) {
        this.maxInputTransfer = maxInputTransfer;
    }

    public long getMaxInputTransfer() {
        return maxInputTransfer;
    }

    public void setMaxOutputTransfer(long maxOutputTransfer) {
        this.maxOutputTransfer = maxOutputTransfer;
    }

    public long getMaxOutputTransfer() {
        return maxOutputTransfer;
    }

    public void setSlotID(int slotID) {
        this.slotID = slotID;
    }

    public int getSlotID() {
        return slotID;
    }
}
