package com.jakisniekot.powerGrid.item;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemParser {

    private MainPlugin plugin;

    private ItemFileHandler itemFileHandler;

    private ItemStack itemStack = new ItemStack(Material.STONE);
    private ItemMeta itemMeta = itemStack.getItemMeta();

    public ItemParser(MainPlugin plugin) {
        this.plugin = plugin;
        this.itemFileHandler = plugin.getItemFileHandler();
    }

    public void parseItem(String configItemID) {
        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{ID}", configItemID);

        boolean error = false;

        FileConfiguration itemConfig = itemFileHandler.getItemConfig();
        ConfigurationSection itemSection = itemConfig.getConfigurationSection(configItemID);

        Material material = null;
        String itemName = null;
        List<String> list = new ArrayList<>();
        int customModelData = 0;
        //PowerItemType powerItemType = null;






        if (itemSection == null) {
            plugin.printLogs("error.items.noSuchItem", placeholder);
            error = true;
            return;
        }

        String materialString = itemConfig.getString("material");

        if (materialString == null) {
            plugin.printLogs("error.items.noMaterial", placeholder);
            error = true;
        } else {
            try {
                material = Material.valueOf(materialString);
            } catch (IllegalArgumentException e) {
                plugin.printLogs("error.items.nonExistentMaterial", placeholder);
                error = true;
            }
        }
    }


}
