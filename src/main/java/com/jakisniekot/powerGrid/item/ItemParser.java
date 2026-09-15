package com.jakisniekot.powerGrid.item;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.List;

public class ItemParser {

    private MainPlugin plugin;

    private ItemFileHandler itemFileHandler;

    private ItemStack itemStack = new ItemStack(Material.STONE);
    private ItemMeta itemMeta = itemStack.getItemMeta();

    public ItemParser(MainPlugin plugin) {
        this.plugin = plugin;
        this.itemFileHandler = plugin.getItemFileHandler();
    }

    public Map<String, ItemStack> parseItemsFromConfig() {
        Map<String, ItemStack> itemStack = new HashMap<>();
        for (String ID : itemFileHandler.getItemConfig().getKeys(false)) {
            itemStack.put(ID, parseItem(ID));
        }
        return itemStack;
    }

    public ItemStack parseItem(String configItemID) {
        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{ID}", configItemID);

        boolean error = false;

        FileConfiguration itemConfig = itemFileHandler.getItemConfig();
        ConfigurationSection itemSection = itemConfig.getConfigurationSection(configItemID);

        Material material = null;
        Component itemName = null;
        List<Component> lore = new ArrayList<>();
        PowerItemType powerItemType = null;



        if (itemSection == null) {
            plugin.printLogs("error.items.noSuchItem", placeholder);
            error = true;
            return errorItem();
        }




        String materialString = itemConfig.getString("material");

        if (materialString == null) {
            plugin.printLogs("error.items.noMaterial", placeholder);
            error = true;
        } else {
            try {
                material = Material.valueOf(materialString);
                itemStack = new ItemStack(material);
                itemMeta = itemStack.getItemMeta();
            } catch (IllegalArgumentException e) {
                plugin.printLogs("error.items.nonExistentMaterial", placeholder);
                error = true;
            }
        }


        if (itemConfig.getString("itemName") != null) {
            itemName = plugin.colorizerLegacy(itemConfig.getString("itemName"));
            itemMeta.displayName(itemName);
        }

        if (itemConfig.getStringList("lore").isEmpty()) {
            for (String line : itemConfig.getStringList("lore")) {
                lore.add(plugin.colorizerLegacy(line));
            }

            itemMeta.lore(lore);
        }

        if (itemConfig.getBoolean("cmdForce")) {
            itemMeta.setCustomModelData(itemConfig.getInt("customModelData"));
        } else {
            if (itemConfig.getInt("customModelData") != 0) {
                itemMeta.setCustomModelData(itemConfig.getInt("customModelData"));
            } else {
                plugin.printLogs("error.items.noCustomModelDataWarning", placeholder);
            }
        }

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(KeyUtil.TypeKey(), PersistentDataType.STRING, powerItemType.toString());


        if (itemConfig.getString("powerItemType") != null) {
            if (itemConfig.getString("powerItemType") == "NONE") {

            } else {
                try {
                    powerItemType = PowerItemType.valueOf(itemConfig.getString("powerItemType"));

                    PowerItemTypeStats powerItemTypeStats = new PowerItemTypeStats(plugin, powerItemType, itemSection);

                    if (powerItemTypeStats.addStats(itemMeta) != null) {
                        itemMeta = powerItemTypeStats.addStats(itemMeta);
                    } else {
                        return errorItem();
                    }

                } catch (IllegalArgumentException e) {
                    plugin.printLogs("error.items.noCustomModelDataWarning", placeholder);

                }
            }
        }





        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private ItemStack errorItem() {
        ItemStack errorItemStack = new ItemStack(Material.BARRIER);
        ItemMeta errorItemMeta = errorItemStack.getItemMeta();
        errorItemMeta.itemName(plugin.colorizerLegacy("&cERROR &8- &7Check console"));
        errorItemStack.setItemMeta(itemMeta);
        return errorItemStack;
    }


}
