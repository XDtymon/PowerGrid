package com.jakisniekot.powerGrid.power.displays.wire;

import com.jakisniekot.powerGrid.power.network.NetworkManager;
import com.jakisniekot.powerGrid.util.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.power.wire.WireDAO;
import com.jakisniekot.powerGrid.parse.particles.WireConnectionRunnable;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WireItemActions {

    private MainPlugin plugin;
    private WireDAO wireDAO;

    public WireItemActions(MainPlugin plugin) {
        this.plugin = plugin;
        this.wireDAO = plugin.getWireDAO();
    }

    public void addLocation(Player player, Location location, ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        if (pdc.has(KeyUtil.WireConnectionKey())) {
            finalizeConnection(player, location);
        } else {
            addFirstLocation(player, location, itemStack);
        }

    }

    public void finalizeConnection(Player player, Location loc2) {

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        String loc1String = pdc.get(KeyUtil.WireConnectionKey(), PersistentDataType.STRING);
        String loc2String = LocationIDString.getString(loc2);


        //Are connectors existent
        if (loc1String == null) return;
        if (loc2String == null) return;

        Location loc1 = LocationIDString.getLocation(loc1String);

        if (!plugin.getConnectorHandler().isConnector(loc1)) return;
        if (!plugin.getConnectorHandler().isConnector(loc2)) return;

        //IS ALREADY CONNECTED
        if (wireDAO.isAlreadyConnected(loc1String, loc2String)) {
            plugin.getPlayerUtility().actionbar(player, plugin.lang().getString("wire.actionbar.error.alreadyConnected"), new HashMap<>());
            return;
        }

        //IS TOO FAR
        double maxDistance = (double) pdc.get(KeyUtil.MaxConnectionDistanceKey(), PersistentDataType.INTEGER);
        if (loc1.distance(loc2) > maxDistance) {
            plugin.getPlayerUtility().actionbar(player, plugin.lang().getString("wire.actionbar.error.tooFar"), new HashMap<>());
            return;
        }

        //TOO MANY CONNECTIONS
        int con2maxConnections = plugin.getConnectorHandler().getConnector(loc2).maxConnections();

        if (wireDAO.getConnected(loc2String).size() >= con2maxConnections) {
            plugin.getPlayerUtility().actionbar(player, plugin.lang().getString("wire.actionbar.error.connectorHasTooManyConnections"), new HashMap<>());
            return;
        }

        //IS SAME PLACE
        if (loc1String.equals(loc2String)) {
            plugin.getPlayerUtility().actionbar(player, plugin.lang().getString("wire.actionbar.error.cannotConnectToItself"), new HashMap<>());
            return;
        } else {
            //WATT TRANSFER
            int wattTransfer = 0;

            try {
                wattTransfer = pdc.get(KeyUtil.WattPerSecondKey(), PersistentDataType.INTEGER);
            } catch (IllegalArgumentException _) {
            }

            //BLOCKS
            List<String> itemStackStrings = pdc.get(KeyUtil.WireBlocksKey(), PersistentDataType.LIST.strings());
            if (itemStackStrings == null) {
                itemStackStrings = new ArrayList<>();
                itemStackStrings.add("STONE");
            }

            pdc.remove(KeyUtil.WireConnectionKey());
            itemMeta.setEnchantmentGlintOverride(null);
            itemStack.setItemMeta(itemMeta);
            itemStack.setAmount(itemStack.getAmount() - 1);

            player.getInventory().setItemInMainHand(itemStack);
            WireConnectionRunnable.removeActive(player.getUniqueId());

            String itemID = pdc.get(KeyUtil.ItemIDKey(), PersistentDataType.STRING);

            plugin.getPlayerUtility().actionbar(player, plugin.lang().getString("wire.actionbar.success"), new HashMap<>());
            Map<String, String> placeholder = new HashMap<>();
            placeholder.put("{player}", player.getName());

            plugin.printLogs("debugLog.wire.success", placeholder);

            if (plugin.networkManager().connect(loc1, loc2, wattTransfer, itemID) == NetworkManager.Result.OK) {
                wireDAO.insertOrUpdate(loc1String, loc2String, wattTransfer, String.join("|", itemStackStrings), itemID);
            }

            List<Material> list = new ArrayList<>();
            for (String item : itemStackStrings) {
                try {
                    list.add(Material.valueOf(item));
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }

            plugin.getWireDisplays().placeLine(
                    loc1,
                    loc2,
                    list
            );

        }
    }

    public void addFirstLocation(Player player, Location location, ItemStack itemStack) {
        String loc1String = LocationIDString.getString(location);

        System.out.println(location);
        System.out.println(plugin.getConnectorHandler().getConnectors());

        if (!plugin.getConnectorHandler().isConnector(location)) return;
        int con1maxConnections = plugin.getConnectorHandler().getConnector(location).maxConnections();
        if (wireDAO.getConnected(loc1String).size() >= con1maxConnections) {
            plugin.getPlayerUtility().actionbar(player, plugin.lang().getString("wire.actionbar.error.connectorHasTooManyConnections"), new HashMap<>());
            return;
        }


        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(KeyUtil.WireConnectionKey(), PersistentDataType.STRING, loc1String);
        itemMeta.setEnchantmentGlintOverride(true);
        itemStack.setItemMeta(itemMeta);

        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{player}", player.getName());
        placeholder.put("{locString}", loc1String);

        plugin.printLogs("debugLog.wire.first", placeholder);

        player.getInventory().setItemInMainHand(itemStack);
        WireConnectionRunnable.addActive(player.getUniqueId());
    }

    public void cancelConnection(Player player, ItemStack itemStack) {

        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{player}", player.getName());

        plugin.printLogs("debugLog.wire.cancel", placeholder);

        WireConnectionRunnable.removeActive(player.getUniqueId());

        if (itemStack == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.remove(KeyUtil.WireConnectionKey());
        itemMeta.setEnchantmentGlintOverride(null);
        itemStack.setItemMeta(itemMeta);

        player.getInventory().setItemInMainHand(itemStack);

    }
}
