package com.jakisniekot.powerGrid.network;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Czyta / zapisuje pola energii (energy, max energy, input/output transfer cap) trzymane
 * w PDC bloku, do którego przypięty jest connector. Działa na każdym bloku, którego
 * BlockState jest TileState (czyli ma własny PersistentDataContainer) - w tym systemie
 * to blok "pod connectorem" (maszyna/bateria itp.), a nie sam connector.
 */
public final class EnergyBlockUtil {

    private EnergyBlockUtil() {}

    public static long getEnergy(Location location) {
        return getLong(location, KeyUtil.EnergyKey(), 0L);
    }

    public static void setEnergy(Location location, long value) {
        setLong(location, KeyUtil.EnergyKey(), Math.max(0, value));
    }

    public static long getMaxEnergy(Location location) {
        return getLong(location, KeyUtil.MaxEnergyKey(), 0L);
    }

    public static void setMaxEnergy(Location location, long value) {
        setLong(location, KeyUtil.MaxEnergyKey(), Math.max(0, value));
    }

    public static long getInputTransferCap(Location location) {
        return getLong(location, KeyUtil.InputTransferCapKey(), 0L);
    }

    public static void setInputTransferCap(Location location, long value) {
        setLong(location, KeyUtil.InputTransferCapKey(), Math.max(0, value));
    }

    public static long getOutputTransferCap(Location location) {
        return getLong(location, KeyUtil.OutputTransferCapKey(), 0L);
    }

    public static void setOutputTransferCap(Location location, long value) {
        setLong(location, KeyUtil.OutputTransferCapKey(), Math.max(0, value));
    }

    public static void setInputItems(Location location, ItemStack[] items) {
        setByteArray(location, KeyUtil.InputItemsKey(), ItemStack.serializeItemsAsBytes(items));
    }

    public static ItemStack[] getInputItems(Location location) {
        byte[] data = getByteArray(location, KeyUtil.InputItemsKey(), new byte[]{});
        return data != null
                ? ItemStack.deserializeItemsFromBytes(data)
                : new ItemStack[0];
    }

    public static void setOutputItems(Location location, ItemStack[] items) {
        setByteArray(location, KeyUtil.OutputItemsKey(), ItemStack.serializeItemsAsBytes(items));
    }

    public static ItemStack[] getOutputItems(Location location) {
        byte[] data = getByteArray(location, KeyUtil.OutputItemsKey(), new byte[]{});
        return data != null
                ? ItemStack.deserializeItemsFromBytes(data)
                : new ItemStack[0];
    }

    /**
     * Ile ten blok może realnie oddać teraz: nie więcej niż ma w zapasie i nie więcej
     * niż jego własny output transfer cap.
     */
    public static long getExtractable(Location location) {
        return Math.min(getEnergy(location), getOutputTransferCap(location));
    }

    /**
     * Ile ten blok może realnie przyjąć teraz: nie więcej niż wolnego miejsca i nie
     * więcej niż jego własny input transfer cap.
     */
    public static long getInsertable(Location location) {
        long free = getMaxEnergy(location) - getEnergy(location);
        return Math.min(Math.max(free, 0), getInputTransferCap(location));
    }

    public static void extract(Location location, long amount) {
        if (amount <= 0) return;
        setEnergy(location, getEnergy(location) - amount);
    }

    public static void insert(Location location, long amount) {
        if (amount <= 0) return;
        setEnergy(location, getEnergy(location) + amount);
    }

    private static long getLong(Location location, NamespacedKey key, long def) {
        PersistentDataContainer pdc = getPdc(location);
        if (pdc == null) return def;
        Long value = pdc.get(key, PersistentDataType.LONG);
        return value == null ? def : value;
    }

    private static void setLong(Location location, NamespacedKey key, long value) {
        Block block = location.getBlock();
        BlockState state = block.getState();
        if (!(state instanceof TileState tileState)) return;
        tileState.getPersistentDataContainer().set(key, PersistentDataType.LONG, value);
        tileState.update();
    }

    private static void setByteArray(Location location, NamespacedKey key, byte[] bytes) {
        Block block = location.getBlock();
        BlockState state = block.getState();
        if (!(state instanceof TileState tileState)) return;
        tileState.getPersistentDataContainer().set(key, PersistentDataType.BYTE_ARRAY, bytes);
        tileState.update();
    }

    private static byte[] getByteArray(Location location, NamespacedKey key, byte[] def) {
        PersistentDataContainer pdc = getPdc(location);
        if (pdc == null) return def;
        byte[] value = pdc.get(key, PersistentDataType.BYTE_ARRAY);
        return value == null ? def : value;
    }

    private static PersistentDataContainer getPdc(Location location) {
        Block block = location.getBlock();
        BlockState state = block.getState();
        if (state instanceof TileState tileState) {
            return tileState.getPersistentDataContainer();
        }
        return null;
    }
}