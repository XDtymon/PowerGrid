package com.jakisniekot.powerGrid;

import com.jakisniekot.powerGrid.actions.wire.WireItemActions;
import com.jakisniekot.powerGrid.command.mainCommandExecutor;
import com.jakisniekot.powerGrid.command.mainCommandTabber;
import com.jakisniekot.powerGrid.database.ConnectorDAO;
import com.jakisniekot.powerGrid.database.DatabaseConnection;
//import com.jakisniekot.powerGrid.database.RelayDAO;
import com.jakisniekot.powerGrid.database.Wire;
import com.jakisniekot.powerGrid.database.WireDAO;
import com.jakisniekot.powerGrid.item.ItemFileHandler;
import com.jakisniekot.powerGrid.item.ItemParser;
import com.jakisniekot.powerGrid.lang.LangFileHandler;
import com.jakisniekot.powerGrid.listener.*;
import com.jakisniekot.powerGrid.machine.MachineFileHandler;
import com.jakisniekot.powerGrid.particles.WireConnectionRunnable;
import com.jakisniekot.powerGrid.util.ConsoleColors;
import com.jakisniekot.powerGrid.util.LocationIDString;
import com.jakisniekot.powerGrid.util.PlayerUtility;
import com.jakisniekot.powerGrid.util.TextUtility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MainPlugin extends JavaPlugin {

    private MainPlugin plugin;

    private LangFileHandler langFileHandler;
    private ItemFileHandler itemFileHandler;
    private MachineFileHandler machineFileHandler;

    private WireItemActions wireItemActions;

    private DatabaseConnection database;


    private ConnectorDAO connectorDAO;
    //private RelayDAO relayDAO;
    private WireDAO wireDAO;

    private PlayerUtility playerUtility;

    private Map<String, ItemStack> items;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        //Instances
        this.langFileHandler = new LangFileHandler(plugin);
        this.itemFileHandler = new ItemFileHandler(plugin);
        this.machineFileHandler = new MachineFileHandler(plugin);
        this.database = new DatabaseConnection(plugin);

        //Files
        saveDefaultConfig();
        langFileHandler.setupLangFiles();
        itemFileHandler.setupItemFiles();
        machineFileHandler.setupMachineFiles();

        //Database
        database.connect();
        database.createTables();

        String DB_TYPE = getConfig().getString("databaseType");

        this.connectorDAO = new ConnectorDAO(database.getConnection(), DB_TYPE, plugin);
        this.wireDAO = new WireDAO(database.getConnection(), DB_TYPE, plugin);
        //this.relayDAO = new RelayDAO(database.getConnection(), DB_TYPE);

        //PlayerUtility
        this.playerUtility = new PlayerUtility(plugin);

        //MOTD
        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{version}", plugin.getPluginMeta().getVersion());
        placeholder.put("{fileHandlerStatus}", database.getStatus());
        printLogs("info.startup", placeholder);

        //PluginManager
        PluginManager pm = plugin.getServer().getPluginManager();

        //Items
        items = new ItemParser(plugin).parseItemsFromConfig();

        //Commands
        getCommand("powergrid").setExecutor(new mainCommandExecutor(plugin));
        getCommand("powergrid").setTabCompleter(new mainCommandTabber(plugin));

        //Listeners
        pm.registerEvents(new PlayerInteractListener(plugin), this);
        pm.registerEvents(new BlockPlaceListener(plugin), this);
        pm.registerEvents(new BlockBreakListener(plugin), this);
        pm.registerEvents(new SlotSwitchListener(plugin), this);
        pm.registerEvents(new InventoryClickListener(plugin), this);
        pm.registerEvents(new PlayerDeathListener(plugin), this);
        pm.registerEvents(new PlayerQuitListener(plugin), this);
        pm.registerEvents(new PlayerTeleportationListener(plugin), this);

        //Handlers
        this.wireItemActions = new WireItemActions(plugin);


        //Tests
        new WireConnectionRunnable(plugin).runTaskTimer(this, 0L, 1L);

        //Wires
        reloadWires();
        /*
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(ArmorStand.class)) {
                PersistentDataContainer pdc = entity.getPersistentDataContainer();
                if (pdc.has(KeyUtil.FirstWireStandKey())) {
                    entity.remove();
                }
            }
        }
         */


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        //Files
        saveDefaultConfig();
        langFileHandler.setupLangFiles();
        itemFileHandler.setupItemFiles();
        machineFileHandler.setupMachineFiles();

        //Database
        database.connect();
        database.createTables();

        String DB_TYPE = getConfig().getString("databaseType");

        this.connectorDAO = new ConnectorDAO(database.getConnection(), DB_TYPE, plugin);
        this.wireDAO = new WireDAO(database.getConnection(), DB_TYPE, plugin);
        //this.relayDAO = new RelayDAO(database.getConnection(), DB_TYPE);

        //PlayerUtility
        this.playerUtility = new PlayerUtility(plugin);

        //MOTD
        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{version}", plugin.getPluginMeta().getVersion());
        placeholder.put("{fileHandlerStatus}", database.getStatus());
        printLogs("info.startup", placeholder);

        //PluginManager
        PluginManager pm = plugin.getServer().getPluginManager();

        //Items
        items = new ItemParser(plugin).parseItemsFromConfig();

        //Commands
        getCommand("powergrid").setExecutor(new mainCommandExecutor(plugin));
        getCommand("powergrid").setTabCompleter(new mainCommandTabber(plugin));

        //Listeners
        pm.registerEvents(new PlayerInteractListener(plugin), this);
        pm.registerEvents(new BlockPlaceListener(plugin), this);
        pm.registerEvents(new BlockBreakListener(plugin), this);
        pm.registerEvents(new SlotSwitchListener(plugin), this);
        pm.registerEvents(new InventoryClickListener(plugin), this);
        pm.registerEvents(new PlayerDeathListener(plugin), this);
        pm.registerEvents(new PlayerQuitListener(plugin), this);
        pm.registerEvents(new PlayerTeleportationListener(plugin), this);

        //Handlers
        this.wireItemActions = new WireItemActions(plugin);


        //Tests
        new WireConnectionRunnable(plugin).runTaskTimer(this, 0L, 1L);

        //Wires
        reloadWires();

    }

    public void printLogs(String messageID) {
        Object object;
        if (getLang().getStringList(messageID).isEmpty()) {
            object = getLang().getString(messageID);
        } else {
            object = getLang().getStringList(messageID);
        }

        if (object instanceof String s) {
            plugin.getLogger().log(Level.INFO, ConsoleColors.colorize(s));
        } else {
            assert object != null;
            for (String line : (List<String>) object) {
                plugin.getLogger().log(Level.INFO, ConsoleColors.colorize(line));

            }
        }
    }

    public void printLogs(String messageID, Map<String, String> placeholders) {

        Object object;
        if (getLang().getStringList(messageID).isEmpty()) {
            object = getLang().getString(messageID);
        } else {
            object = getLang().getStringList(messageID);
        }

        if (object == null)  {
            plugin.getLogger().log(Level.INFO, ("Missing message: "+messageID));
            return;
        }

        if (object instanceof String s) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                s = s.replace(entry.getKey(), entry.getValue());
            }
            plugin.getLogger().log(Level.INFO, ConsoleColors.colorize(s));
        } else {
            for (String line : (List<String>) object) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }
                plugin.getLogger().log(Level.INFO, ConsoleColors.colorize(line));

            }
        }
    }

    private void reloadWires() {
        List<Wire> wires = wireDAO.getAll();
        int wiresReloaded = wires.size();
        int armorStandsRemoved = 0;

        for (Wire wire : wires) {
            Location loc1 = LocationIDString.getLocation(wire.loc1());
            Location loc2 = LocationIDString.getLocation(wire.loc2());

            if (loc1 == null || loc2 == null || loc1.getWorld() == null) continue;

            World world = loc1.getWorld();

            world.getChunkAt(loc1).load();
            world.getChunkAt(loc2).load();

            double midX = (loc1.getX() + loc2.getX()) / 2.0;
            double midY = (loc1.getY() + loc2.getY()) / 2.0;
            double midZ = (loc1.getZ() + loc2.getZ()) / 2.0;
            Location midpoint = new Location(world, midX, midY, midZ);

            double radius = (loc1.distance(loc2) / 2.0) + 5; // +5 buffer for sag/curve in the wire model

            for (Entity entity : world.getNearbyEntities(midpoint, radius, radius, radius)) {
                if (entity instanceof ArmorStand stand) {
                    PersistentDataContainer pdc = stand.getPersistentDataContainer();
                    if (pdc.has(KeyUtil.FirstWireStandKey())) {
                        stand.remove();
                        armorStandsRemoved++;
                    }
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, wireDAO::reinstateWires, 20L * 5);

        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{wiresReloaded}", String.valueOf(wiresReloaded));
        placeholder.put("{armorStandsRemoved}", String.valueOf(armorStandsRemoved));
        printLogs("info.wireReload", placeholder);
    }

    public void dropItems(ItemStack[] itemStacks, Location location) {
        for (ItemStack itemStack : itemStacks) {
            location.getWorld().dropItem(location, itemStack);
        }
    }




    public MachineFileHandler getMachineFileHandler() {
        return machineFileHandler;
    }

    public PlayerUtility getPlayerUtility() {
        return playerUtility;
    }

    public ConnectorDAO getConnectorDAO() {
        return connectorDAO;
    }
/*
    public RelayDAO getRelayDAO() {
        return relayDAO;
    }
 */
    public WireDAO getWireDAO() {
        return wireDAO;
    }

    public WireItemActions getWireActions() {
        return wireItemActions;
    }

    public Connection getConnection() {
        return database.getConnection();
    }

    public LangFileHandler getLangFileHandler() {
        return langFileHandler;
    }

    public ItemFileHandler getItemFileHandler() {
        return itemFileHandler;
    }

    public List<String> getItemsList() {
        return new ArrayList<>(items.keySet());
    }

    public ItemStack getItemFromID(String id) {
        return items.get(id).clone();
    }

    public boolean isExistingItem(String id) {
        return items.containsKey(id);
    }

    public FileConfiguration getLang() {
        return langFileHandler.getLang();
    }

    public Component getMessage(String key, Map<String, String> placeholders) {
        String msg = getLang().getString(key, "Missing message: " + key);
        return TextUtility.stringReplacer(msg, placeholders);
    }

    public Component getMessage(String key) {
        String msg = getLang().getString(key, "Missing message: " + key);
        return TextUtility.color(msg);
    }


}
