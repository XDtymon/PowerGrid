package com.jakisniekot.powerGrid.item;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class PowerItemTypeStats {

    private MainPlugin plugin;

    private PowerItemType powerItemType;
    private ConfigurationSection itemSection;

    public PowerItemTypeStats(
            MainPlugin plugin,
            PowerItemType powerItemType,
            ConfigurationSection itemSection
    ) {
        this.plugin = plugin;
        this.itemSection = itemSection;
        this.powerItemType = powerItemType;
    }

    public ItemMeta addStats(ItemMeta itemMeta) {
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        if (powerItemType == PowerItemType.WIRE) {
            //CONNECTION DISTANCE
            int maxConnectionDistance = 0;
            maxConnectionDistance = itemSection.getInt("maxConnectionDistance");

            if (maxConnectionDistance == 0) {
                return null;
            } else {
                pdc.set(KeyUtil.MaxConnectionDistanceKey(), PersistentDataType.INTEGER, maxConnectionDistance);
            }

            //WATT PER SECOND
            int wattPerSecond = 0;
            wattPerSecond = itemSection.getInt("wattPerSecond");

            if (wattPerSecond == 0) {
                if (!(itemSection.getBoolean("forceWPS"))) return null;
            } else {
                pdc.set(KeyUtil.WattPerSecondKey(), PersistentDataType.INTEGER, wattPerSecond);
            }

            List<Component> lore = new ArrayList<>();

            Map<String, String> placeholder = new HashMap<>();
            placeholder.put("%maxConnectionDistance%", String.valueOf(maxConnectionDistance));
            placeholder.put("%wattPerSecond%", String.valueOf(wattPerSecond));

            if (!itemSection.getStringList("lore").isEmpty()) {
                for (String line : itemSection.getStringList("lore")) {
                    lore.add(plugin.colorizerLegacy(line, placeholder));
                }

                itemMeta.lore(lore);
            }

            itemMeta.lore(lore);

        } else if (powerItemType == PowerItemType.CONNECTOR) {
            //MAX CONNECTION AMOUNT
            int maxConnectonAmount = 0;
            maxConnectonAmount = itemSection.getInt("maxConnectionAmount");

            if (maxConnectonAmount == 0) {
                return null;
            }

            //ALLOWED WIRE TYPES & LIST TYPE
            List<String> allowedWireTypes = itemSection.getStringList("allowedWireTypes");
            boolean blacklist = itemSection.getBoolean("blacklist");
            if (allowedWireTypes.isEmpty()) {
                pdc.set(KeyUtil.AllowedTypesListTypeKey(), PersistentDataType.BOOLEAN, true);
            } else {
                pdc.set(KeyUtil.AllowedTypesKey(), PersistentDataType.LIST.strings(), allowedWireTypes);

                /*
                // Odczyt
public List<String> loadList(PersistentDataContainer pdc) {
    List<String> list = pdc.get(key, PersistentDataType.LIST.strings());
    return list != null ? list : new ArrayList<>();
}

// Sprawdzenie czy istnieje
public boolean hasList(PersistentDataContainer pdc) {
    return pdc.has(key, PersistentDataType.LIST.strings());
}
                 */




                pdc.set(KeyUtil.AllowedTypesListTypeKey(), PersistentDataType.BOOLEAN, blacklist);
            }

            List<Component> lore = new ArrayList<>();

            Map<String, String> placeholder = new HashMap<>();
            placeholder.put("%maxConnectonAmount%", String.valueOf(maxConnectonAmount));
            placeholder.put("%allowedWireTypes%", String.valueOf(allowedWireTypes));

            if (!itemSection.getStringList("lore").isEmpty()) {

                for (String line : itemSection.getStringList("lore")) {
                    lore.add(plugin.colorizerLegacy(line, placeholder));
                }

                itemMeta.lore(lore);
            }



        }

        return itemMeta;
    }
}
