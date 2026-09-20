package com.jakisniekot.powerGrid.machine;

import java.util.List;
import java.util.Map;

public record EnergyStorage(
        double energy,
        double maxEnergy,
        double maxInputTransfer,
        double maxOutputTransfer,
        List<Integer> energyDisplayItems
) {}