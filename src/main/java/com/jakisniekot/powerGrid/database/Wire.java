package com.jakisniekot.powerGrid.database;

public record Wire(
        String loc1,
        String loc2,
        int wattTransfer,
        String headItems,
        String itemID
) {}