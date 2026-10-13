package com.jakisniekot.powerGrid.util;

import org.bukkit.inventory.ItemStack;

/**
 * AI MADE CLASS
 */
public final class ItemTransfer {

    private ItemTransfer() {}

    // ---------- WKŁADANIE ----------

    /**
     * Wkłada item do slotów (najpierw dopełnia istniejące stacki, potem puste sloty).
     * @return to, co się nie zmieściło (null, jeśli weszło wszystko)
     */
    public static ItemStack insert(ItemStack[] slots, ItemStack item) {
        if (item == null || item.getAmount() <= 0) return null;
        int left = item.getAmount();

        for (ItemStack slot : slots) {
            if (left <= 0) break;
            if (slot == null || !slot.isSimilar(item)) continue;
            int add = Math.min(slot.getMaxStackSize() - slot.getAmount(), left);
            if (add <= 0) continue;
            slot.setAmount(slot.getAmount() + add);
            left -= add;
        }

        for (int i = 0; i < slots.length && left > 0; i++) {
            if (slots[i] != null) continue;
            int add = Math.min(item.getMaxStackSize(), left);
            ItemStack copy = item.clone();
            copy.setAmount(add);
            slots[i] = copy;
            left -= add;
        }

        if (left <= 0) return null;
        ItemStack rest = item.clone();
        rest.setAmount(left);
        return rest;
    }

    /** Czy cały item się zmieści (nic nie zmienia). */
    public static boolean canInsert(ItemStack[] slots, ItemStack item) {
        return insert(copy(slots), item) == null;
    }

    /** Czy wszystkie itemy zmieszczą się razem (nic nie zmienia). */
    public static boolean canInsertAll(ItemStack[] slots, ItemStack[] items) {
        ItemStack[] sim = copy(slots);
        for (ItemStack item : items) {
            if (insert(sim, item) != null) return false;
        }
        return true;
    }

    /** Wkłada wszystkie itemy albo żadnego. @return true, jeśli włożono */
    public static boolean insertAll(ItemStack[] slots, ItemStack[] items) {
        ItemStack[] sim = copy(slots);
        for (ItemStack item : items) {
            if (insert(sim, item) != null) return false;
        }
        System.arraycopy(sim, 0, slots, 0, slots.length);
        return true;
    }

    // ---------- WYCIĄGANIE ----------

    /** Ile sztuk danego itemu jest w slotach. */
    public static int count(ItemStack[] slots, ItemStack template) {
        int total = 0;
        for (ItemStack slot : slots) {
            if (slot != null && slot.isSimilar(template)) total += slot.getAmount();
        }
        return total;
    }

    /**
     * Usuwa do `amount` sztuk itemu pasującego do template.
     * @return ile faktycznie usunięto
     */
    public static int remove(ItemStack[] slots, ItemStack template, int amount) {
        int left = amount;
        for (int i = 0; i < slots.length && left > 0; i++) {
            ItemStack slot = slots[i];
            if (slot == null || !slot.isSimilar(template)) continue;
            int take = Math.min(slot.getAmount(), left);
            slot.setAmount(slot.getAmount() - take);
            if (slot.getAmount() <= 0) slots[i] = null;
            left -= take;
        }
        return amount - left;
    }

    /**
     * Wyciąga do `amount` sztuk itemu pasującego do template.
     * @return wyciągnięty stack (może być mniejszy niż amount) albo null, jeśli nic nie ma
     */
    public static ItemStack extract(ItemStack[] slots, ItemStack template, int amount) {
        int taken = remove(slots, template, amount);
        if (taken <= 0) return null;
        ItemStack result = template.clone();
        result.setAmount(taken);
        return result;
    }

    /** Wyciąga cokolwiek z pierwszego niepustego slotu (do lejków/wyjmowania z GUI). */
    public static ItemStack extractAny(ItemStack[] slots, int maxAmount) {
        for (int i = 0; i < slots.length; i++) {
            ItemStack slot = slots[i];
            if (slot == null) continue;
            int take = Math.min(slot.getAmount(), maxAmount);
            ItemStack result = slot.clone();
            result.setAmount(take);
            slot.setAmount(slot.getAmount() - take);
            if (slot.getAmount() <= 0) slots[i] = null;
            return result;
        }
        return null;
    }

    /** Czy wszystkie wymagane itemy są w slotach (nic nie zmienia). */
    public static boolean hasAll(ItemStack[] slots, ItemStack[] required) {
        ItemStack[] sim = copy(slots);
        for (ItemStack req : required) {
            if (remove(sim, req, req.getAmount()) < req.getAmount()) return false;
        }
        return true;
    }

    /** Zabiera wszystkie wymagane itemy albo nic. @return true, jeśli zabrano */
    public static boolean consume(ItemStack[] slots, ItemStack[] required) {
        ItemStack[] sim = copy(slots);
        for (ItemStack req : required) {
            if (remove(sim, req, req.getAmount()) < req.getAmount()) return false;
        }
        System.arraycopy(sim, 0, slots, 0, slots.length);
        return true;
    }

    // ---------- POMOCNICZE ----------

    private static ItemStack[] copy(ItemStack[] slots) {
        ItemStack[] copy = new ItemStack[slots.length];
        for (int i = 0; i < slots.length; i++) {
            copy[i] = slots[i] == null ? null : slots[i].clone();
        }
        return copy;
    }
}