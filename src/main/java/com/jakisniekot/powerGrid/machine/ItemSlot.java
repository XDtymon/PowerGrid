package com.jakisniekot.powerGrid.machine;

import org.bukkit.inventory.ItemStack;

public record ItemSlot(
        ItemStack itemStack,
        int maxAmount
) {
}
