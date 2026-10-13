package com.jakisniekot.powerGrid.parse.item;

import com.jakisniekot.powerGrid.util.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.parse.exceptions.ItemParseError;
import com.jakisniekot.powerGrid.util.ItemMaterial;
import com.jakisniekot.powerGrid.util.TextUtility;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class ItemHandler {

    private MainPlugin plugin;

    private FileConfiguration itemConfig;
    private File itemFile;

    public ItemHandler(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupItemFiles() {
        String path = "items.yml";
        itemFile = new File(plugin.getDataFolder(), path);

        if (!itemFile.exists()) {
            itemFile.getParentFile().mkdirs();
            plugin.saveResource(path, false);
        }

        itemConfig = YamlConfiguration.loadConfiguration(itemFile);

        InputStream defStream = plugin.getResource(path);
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            itemConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getItemConfig() {
        return itemConfig;
    }


    public Map<String, ItemStack> parseItems() {

        this.itemConfig = getItemConfig();

        Map<String, ItemStack> itemStack = new HashMap<>();
        for (String ID : itemConfig.getKeys(false)) {
            itemStack.put(ID, parseItem(ID));
        }

        plugin.items = itemStack;

        return itemStack;
    }

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemStack parseItem(String itemID) {

        PowerItemType powerItemType = null;
        itemStack = new ItemStack(Material.STONE);
        itemMeta = itemStack.getItemMeta();


        if (itemConfig.getConfigurationSection(itemID) == null) return errorItem();
        ConfigurationSection itemSection = itemConfig.getConfigurationSection(itemID);
        String powerItemTypeString = itemSection.getString("powerItemType", "NONE");

        itemStack = simpleItem(itemID);

        try {
            powerItemType = PowerItemType.valueOf(itemSection.getString("powerItemType"));
        } catch (IllegalArgumentException e) {
            plugin.errorHandler().print(ItemParseError.ITEM_TYPE_ERROR, itemID);
            powerItemType = PowerItemType.NONE;
        }

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(KeyUtil.TypeKey(), PersistentDataType.STRING, powerItemType.toString());
        pdc.set(KeyUtil.ItemIDKey(), PersistentDataType.STRING, itemID);

        switch (powerItemTypeString) {
            case "NONE":
                itemMeta = setLore(itemSection, new HashMap<>());
                break;
            case "MACHINE": {
                String machineID = itemSection.getString("machineID");
                if (machineID == null) return null;
                pdc.set(KeyUtil.ItemIDKey(), PersistentDataType.STRING, itemID);
                pdc.set(KeyUtil.MachineIDKey(), PersistentDataType.STRING, machineID);
                itemMeta = setLore(itemSection, new HashMap<>());
                break;
            }

            case "WIRE": {
                //CONNECTION DISTANCE
                int maxConnectionDistance = 0;
                maxConnectionDistance = itemSection.getInt("maxConnectionDistance");

                if (maxConnectionDistance == 0) {
                    return null;
                } else {
                    pdc.set(KeyUtil.MaxConnectionDistanceKey(), PersistentDataType.INTEGER, maxConnectionDistance);
                }

                //WIRE BLOCKS
                List<String> wireBlocks = itemSection.getStringList("wireBlocks");
                //if (!wireBlocks.isEmpty()) wireBlocks.add("STONE");
                pdc.set(KeyUtil.WireBlocksKey(), PersistentDataType.LIST.strings(), wireBlocks);

                //WATT PER SECOND
                int wattPerSecond = 0;
                wattPerSecond = itemSection.getInt("wattPerSecond");

                if (wattPerSecond == 0) {
                    if (!(itemSection.getBoolean("forceWPS"))) return null;
                } else {
                    pdc.set(KeyUtil.WattPerSecondKey(), PersistentDataType.INTEGER, wattPerSecond);
                }

                //CABLE PARTICLES
                String wireParticles;
                if (itemSection.getString("wireParticles") != null) {
                    wireParticles = itemSection.getString("wireParticles");
                } else {
                    wireParticles = "REDSTONE|#0000FF|1.2";
                }

                pdc.set(KeyUtil.WireParticleKey(), PersistentDataType.STRING, wireParticles);

                Map<String, String> lorePlaceholder = new HashMap<>();
                lorePlaceholder.put("%maxConnectionDistance%", String.valueOf(maxConnectionDistance));
                lorePlaceholder.put("%wattPerSecond%", String.valueOf(wattPerSecond));
                itemMeta = setLore(itemSection, lorePlaceholder);
                break;
            }

            case "CONNECTOR": {
                //MAX CONNECTION AMOUNT
                int maxConnectionAmount = itemSection.getInt("maxConnectionAmount", 1);
                pdc.set(KeyUtil.MaxConnectionAmountKey(), PersistentDataType.INTEGER, maxConnectionAmount);

                //ALLOWED WIRE TYPES & LIST TYPE
                List<String> allowedWireTypes = itemSection.getStringList("allowedWireTypes");
                boolean blacklist = itemSection.getBoolean("blacklist");

                if (allowedWireTypes.isEmpty()) {
                    pdc.set(KeyUtil.AllowedTypesListTypeKey(), PersistentDataType.BOOLEAN, true);
                } else {
                    pdc.set(KeyUtil.AllowedTypesKey(), PersistentDataType.LIST.strings(), allowedWireTypes);
                    pdc.set(KeyUtil.AllowedTypesListTypeKey(), PersistentDataType.BOOLEAN, blacklist);
                }

                Map<String, String> lorePlaceholder = new HashMap<>();
                lorePlaceholder.put("%maxConnectionAmount%", String.valueOf(maxConnectionAmount));
                lorePlaceholder.put("%allowedWireTypes%", String.valueOf(allowedWireTypes));
                itemMeta = setLore(itemSection, lorePlaceholder);
                break;
            }

            case null, default:
                break;
        }

        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(1);

        plugin.errorHandler().print(ItemParseError.NONE, itemID);

        return itemStack;
    }

    public ItemStack simpleItem(String itemID) {
        ConfigurationSection itemSection = itemConfig.getConfigurationSection(itemID);

        Component itemName = null;
        List<Component> lore = new ArrayList<>();

        if (itemSection == null) {
            plugin.errorHandler().print(ItemParseError.FATAL_ERROR, itemID);
            return errorItem();
        }

        String materialString = itemSection.getString("material");

        if (materialString == null) {
            plugin.errorHandler().print(ItemParseError.MATERIAL_ERROR, itemID);
        } else {
            try {
                itemStack = ItemMaterial.simple(materialString);
                itemMeta = itemStack.getItemMeta();
            } catch (IllegalArgumentException e) {
                plugin.errorHandler().print(ItemParseError.MATERIAL_ERROR, itemID);
            }
        }

        if (itemSection.getString("itemName") != null) {
            itemName = TextUtility.color(itemSection.getString("itemName"));
            itemMeta.displayName(itemName);
        } else {
            plugin.errorHandler().print(ItemParseError.ITEM_NAME_ERROR, itemID);
        }

        if (itemSection.getKeys(false).contains("customModelData")) {
            itemMeta.setCustomModelData(itemSection.getInt("customModelData"));
        }

        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(1);

        return itemStack;
    }

    private ItemMeta setLore(ConfigurationSection itemSection, Map<String, String> placeholder) {

        if (!itemSection.getStringList("lore").isEmpty()) {
            itemMeta.lore(TextUtility.colorList(itemSection.getStringList("lore"), placeholder));
        }

        return itemMeta;
    }

    private ItemStack errorItem() {
        ItemStack errorsItemStack = new ItemStack(Material.BARRIER);
        ItemMeta errorsItemMeta = errorsItemStack.getItemMeta();
        errorsItemMeta.itemName(TextUtility.color("&cERROR &8- &7Check console"));
        errorsItemStack.setItemMeta(itemMeta);
        return errorsItemStack;
    }
}