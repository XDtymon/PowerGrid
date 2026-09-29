package com.jakisniekot.powerGrid.item;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.util.ItemMaterial;
import com.jakisniekot.powerGrid.util.TextUtility;
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
        plugin.printLogs("info.itemParsingg.start");

        Map<String, ItemStack> itemStack = new HashMap<>();
        for (String ID : itemFileHandler.getItemConfig().getKeys(false)) {
            itemStack.put(ID, parseItem(ID));
        }

        plugin.printLogs("info.itemParsingg.end");

        return itemStack;
    }

    public ItemStack parseItem(String configItemID) {
        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{ID}", configItemID);

        boolean errors = false;

        FileConfiguration itemConfig = itemFileHandler.getItemConfig();
        ConfigurationSection itemSection = itemConfig.getConfigurationSection(configItemID);

        Component itemName = null;
        List<Component> lore = new ArrayList<>();
        PowerItemType powerItemType = null;



        if (itemSection == null) {
            plugin.printLogs("errors.items.noSuchItem", placeholder);
            errors = true;
            return errorItem();
        }




        String materialString = itemSection.getString("material");

        if (materialString == null) {
            plugin.printLogs("errors.items.noMaterial", placeholder);
            errors = true;
        } else {
            try {
                itemStack = ItemMaterial.simple(materialString);
                itemMeta = itemStack.getItemMeta();
            } catch (IllegalArgumentException e) {
                errors = true;
            }
        }


        if (itemSection.getString("itemName") != null) {
            itemName = TextUtility.color(itemSection.getString("itemName"));
            itemMeta.displayName(itemName);
        } else {
            errors = true;
        }

        if (itemSection.getKeys(false).contains("customModelData")) {
            itemMeta.setCustomModelData(itemSection.getInt("customModelData"));
        }


        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();


        if (itemSection.getString("powerItemType") != null) {
            if (itemSection.getString("powerItemType") == "NONE") {

            } else if (itemSection.getString("powerItemType") == "MACHINE") {
                
            } else {
                try {
                    powerItemType = PowerItemType.valueOf(itemSection.getString("powerItemType"));
                    pdc.set(KeyUtil.TypeKey(), PersistentDataType.STRING, powerItemType.toString());

                    PowerItemTypeStats powerItemTypeStats = new PowerItemTypeStats(plugin, powerItemType, itemSection, configItemID);

                    if (powerItemTypeStats.addStats(itemMeta) != null) {
                        itemMeta = powerItemTypeStats.addStats(itemMeta);
                    } else {
                        return errorItem();
                    }

                } catch (IllegalArgumentException e) {
                    errors = true;

                }
            }
        }


        if (!errors) {
            plugin.printLogs("info.itemParsing.successful", placeholder);
        } else {
            plugin.printLogs("info.itemParsing.warning", placeholder);
        };

        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(1);
        return itemStack;
    }

    private ItemStack errorItem() {
        ItemStack errorsItemStack = new ItemStack(Material.BARRIER);
        ItemMeta errorsItemMeta = errorsItemStack.getItemMeta();
        errorsItemMeta.itemName(TextUtility.color("&cERROR &8- &7Check console"));
        errorsItemStack.setItemMeta(itemMeta);
        return errorsItemStack;
    }


}