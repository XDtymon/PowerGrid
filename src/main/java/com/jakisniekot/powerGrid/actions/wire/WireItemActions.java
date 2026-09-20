package com.jakisniekot.powerGrid.actions.wire;

import com.jakisniekot.powerGrid.KeyUtil;
import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.database.ConnectorDAO;
import com.jakisniekot.powerGrid.database.Wire;
import com.jakisniekot.powerGrid.database.WireDAO;
import com.jakisniekot.powerGrid.particles.WireConnectionRunnable;
import com.jakisniekot.powerGrid.util.LocationIDString;
import org.bukkit.Location;
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

        ConnectorDAO connectorDAO = plugin.getConnectorDAO();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        String loc1String = pdc.get(KeyUtil.WireConnectionKey(), PersistentDataType.STRING);
        String loc2String = LocationIDString.getString(loc2);


        //Are connectors existent
        if (loc1String == null) return;
        if (loc2String == null) return;

        if (connectorDAO.get(loc1String) == null) return;
        if (connectorDAO.get(loc2String) == null) return;

        //IS ALREADY CONNECTED
        if (wireDAO.isAlreadyConnected(loc1String, loc2String)) {
            plugin.getPlayerUtility().actionbar(player, plugin.getLang().getString("wire.actionbar.error.alreadyConnected"), new HashMap<>());
            return;
        }

        //IS TOO FAR
        Location loc1 = LocationIDString.getLocation(loc1String);
        double maxDistance = (double) pdc.get(KeyUtil.MaxConnectionDistanceKey(), PersistentDataType.INTEGER);
        if (loc1.distance(loc2) > maxDistance) {
            plugin.getPlayerUtility().actionbar(player, plugin.getLang().getString("wire.actionbar.error.tooFar"), new HashMap<>());
            return;
        }

        //TOO MANY CONNECTIONS
        int con2maxConnections = connectorDAO.get(loc2String).maxConnections();

        if (wireDAO.getConnected(loc2String).size() >= con2maxConnections) {
            plugin.getPlayerUtility().actionbar(player, plugin.getLang().getString("wire.actionbar.error.connectorHasTooManyConnections"), new HashMap<>());
            return;
        }

        //IS SAME PLACE
        if (loc1String.equals(loc2String)) {
            plugin.getPlayerUtility().actionbar(player, plugin.getLang().getString("wire.actionbar.error.cannotConnectToItself"), new HashMap<>());
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

            plugin.getPlayerUtility().actionbar(player, plugin.getLang().getString("wire.actionbar.success"), new HashMap<>());
            Map<String, String> placeholder = new HashMap<>();
            placeholder.put("{player}", player.getName());

            plugin.printLogs("debugLog.wire.success", placeholder);

            wireDAO.insertOrUpdate(loc1String, loc2String, wattTransfer, String.join("|", itemStackStrings), itemID);

            new WireLine(
                    plugin,
                    LocationIDString.getLocation(loc1String),
                    LocationIDString.getLocation(loc2String),
                    String.join("|", itemStackStrings)
            ).place();
        }


    }

    public void addFirstLocation(Player player, Location location, ItemStack itemStack) {
        String loc1String = LocationIDString.getString(location);
        ConnectorDAO connectorDAO = plugin.getConnectorDAO();

        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{player}", player.getName());
        placeholder.put("{locString}", loc1String);

        plugin.printLogs("debugLog.wire.first", placeholder);

        if (connectorDAO.get(loc1String) == null) return;
        int con1maxConnections = connectorDAO.get(loc1String).maxConnections();
        if (wireDAO.getConnected(loc1String).size() >= con1maxConnections) {
            plugin.getPlayerUtility().actionbar(player, plugin.getLang().getString("wire.actionbar.error.connectorHasTooManyConnections"), new HashMap<>());
            return;
        }


        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(KeyUtil.WireConnectionKey(), PersistentDataType.STRING, loc1String);
        itemMeta.setEnchantmentGlintOverride(true);
        itemStack.setItemMeta(itemMeta);

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
