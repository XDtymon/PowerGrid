package com.jakisniekot.powerGrid.machine;

import com.jakisniekot.powerGrid.MainPlugin;

public class MachineType {

    private MainPlugin plugin;

    private MachineFileHandler machineFileHandler;

    private Integer[] unclickableSlots;

    public MachineType(MainPlugin plugin) {
        this.plugin = plugin;

        this.machineFileHandler = plugin.getMachineFileHandler();
    }




}
