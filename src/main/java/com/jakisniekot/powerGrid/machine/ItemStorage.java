package com.jakisniekot.powerGrid.machine;

import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record ItemStorage (
    Map<Integer, ItemSlot[]> itemSlots
) {}

