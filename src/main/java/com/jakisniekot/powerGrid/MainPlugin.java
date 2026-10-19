package com.jakisniekot.powerGrid;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.jakisniekot.powerGrid.power.displays.wire.WireDisplays;
import com.jakisniekot.powerGrid.power.displays.wire.WireItemActions;
import com.jakisniekot.powerGrid.command.mainCommandExecutor;
import com.jakisniekot.powerGrid.command.mainCommandTabber;
import com.jakisniekot.powerGrid.power.connector.ConnectorDAO;
import com.jakisniekot.powerGrid.power.connector.ConnectorHandler;
import com.jakisniekot.powerGrid.parse.exceptions.ErrorHandler;
import com.jakisniekot.powerGrid.parse.item.ItemHandler;
import com.jakisniekot.powerGrid.listener.*;
import com.jakisniekot.powerGrid.power.machine.MachineDAO;
import com.jakisniekot.powerGrid.power.machine.MachineTileHandler;
import com.jakisniekot.powerGrid.power.machine.MachineTypeHandler;
import com.jakisniekot.powerGrid.power.machine.gui.GuiConfig;
import com.jakisniekot.powerGrid.power.network.NetworkManager;
import com.jakisniekot.powerGrid.power.network.NetworkTickTask;
import com.jakisniekot.powerGrid.parse.particles.WireConnectionRunnable;
import com.jakisniekot.powerGrid.parse.placeholders.NewPlaceholders;
import com.jakisniekot.powerGrid.parse.recipe.RecipeHandler;
import com.jakisniekot.powerGrid.parse.sounds.SoundHandler;
import com.jakisniekot.powerGrid.power.wire.Wire;
import com.jakisniekot.powerGrid.power.wire.WireDAO;
import com.jakisniekot.powerGrid.util.*;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MainPlugin extends JavaPlugin {

    private MainPlugin plugin;

    private LangFileHandler langFileHandler;

    private WireItemActions wireItemActions;

    private DatabaseHandler database;

    private ErrorHandler errorHandler;
    private ItemHandler itemHandler;
    private RecipeHandler recipeHandler;
    private SoundHandler soundHandler;
    private MachineTypeHandler machineTypeHandler;

    private MachineTileHandler machineTileHandler;
    private ConnectorHandler connectorHandler;

    private ConnectorDAO connectorDAO;
    private MachineDAO machineDAO;
    private WireDAO wireDAO;

    private NetworkManager networkManager;
    private BukkitTask networkTickTask;

    private WireDisplays wireDisplays;
    private GuiConfig guiConfig;

    private PlayerUtility playerUtility;

    public Map<String, ItemStack> items;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        //CONFIG
        saveDefaultConfig();

        //INSTANCES
        this.langFileHandler = new LangFileHandler(plugin);

        this.database = new DatabaseHandler(plugin);

        this.itemHandler = new ItemHandler(plugin);
        this.recipeHandler = new RecipeHandler(plugin);
        this.errorHandler = new ErrorHandler(plugin);
        this.soundHandler = new SoundHandler(plugin);

        this.machineTypeHandler = new MachineTypeHandler(plugin);
        this.machineTileHandler = new MachineTileHandler(plugin);
        this.connectorHandler = new ConnectorHandler(plugin);

        this.networkManager = new NetworkManager(plugin);

        this.wireDisplays = new WireDisplays(plugin);


        //LANG
        langFileHandler.setupLangFiles();

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new NewPlaceholders(this).register();
            this.guiConfig = GuiConfig.parse(
                    getConfig().getConfigurationSection(""),
                    plugin.getLogger(),
                    s -> PlaceholderAPI.setPlaceholders((OfflinePlayer) null, s)
            );
        } else {
            printLogs("errors.noPAPI");
            this.guiConfig = GuiConfig.parse(
                    getConfig().getConfigurationSection("defaultMachine"),
                    getLogger(),
                    s -> s
            );
        }

        //Util
        warmUpProfiles();

        //MOTD
        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{version}", plugin.getPluginMeta().getVersion());
        placeholder.put("{fileHandlerStatus}", database.getStatus());
        printLogs("info.startup", placeholder);

        //Database
        database.connect();
        database.createTables();

        //Other Files
        itemHandler.setupItemFiles();
        recipeHandler.setupRecipeFiles();
        soundHandler.setupSoundFile();
        machineTypeHandler.setupMachineFiles();

        //DAOs
        String DB_TYPE = getConfig().getString("databaseType");
        this.connectorDAO = new ConnectorDAO(database.getConnection(), DB_TYPE, plugin);
        this.wireDAO = new WireDAO(database.getConnection(), DB_TYPE, plugin);
        this.machineDAO = new MachineDAO(database.getConnection(), DB_TYPE, plugin);

        //Power Related Instances
        connectorHandler.addFromDB();
        networkManager.loadFromDB();

        new NetworkTickTask(this, networkManager, 1).start();

        //PlayerUtility
        this.playerUtility = new PlayerUtility(plugin);

        //PluginManager
        PluginManager pm = plugin.getServer().getPluginManager();

        //Parsing
        printLogs("parsing.start");
        printLogs("parsing.item.separator");
        itemHandler.parseItems();
        printLogs("parsing.recipe.separator");
        recipeHandler.parseRecipes();
        printLogs("parsing.sound.separator");
        soundHandler.parseSounds();
        printLogs("parsing.machine.separator");
        machineTypeHandler.parseMachines();

        printLogs("parsing.finish");

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
        pm.registerEvents(new PlayerPickBlockListener(plugin), this);

        //Handlers
        this.wireItemActions = new WireItemActions(plugin);

        //Tasks
        new WireConnectionRunnable(plugin).runTaskTimer(this, 0L, 1L);

        //Wires
        reloadWires();


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        plugin.machineDAO.saveAllMachinery();
        plugin.connectorDAO.saveAllConnectors();

    }

    public void reload() {

    }

    /**
     * Tylko poto zeby sie pozbyc tego \/
     * Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, profilesHost=https://api.mojang.com, name=PROD]
     */
    private void warmUpProfiles() {
        ItemStack dummy = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) dummy.getItemMeta();

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "warmup");
        profile.setProperty(new ProfileProperty("textures", "e30=")); // pusty JSON w base64
        meta.setPlayerProfile(profile);

        dummy.setItemMeta(meta);
    }

    private void reloadWires() {
        List<Wire> wires = plugin.getWireDAO().getAll();
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

        wireDisplays.reinstateWires();

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

    public ErrorHandler errorHandler() {
        return errorHandler;
    }

    public RecipeHandler recipes() {
        return recipeHandler;
    }

    public SoundHandler getSoundFileHandler() {
        return soundHandler;
    }

    public MachineTypeHandler getMachineTypeHandler() {
        return machineTypeHandler;
    }

    public MachineTileHandler getMachineTileHandler() {
        return machineTileHandler;
    }

    public ConnectorHandler getConnectorHandler() {
        return connectorHandler;
    }

    public PlayerUtility getPlayerUtility() {
        return playerUtility;
    }

    public ConnectorDAO getConnectorDAO() {
        return connectorDAO;
    }

    public MachineDAO getMachineDAO() {
        return machineDAO;
    }

    public WireDAO getWireDAO() {
        return wireDAO;
    }

    public WireDisplays getWireDisplays() {
        return wireDisplays;
    }

    public NetworkManager networkManager() {
        return networkManager;
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

    public List<String> getItemsList() {
        return new ArrayList<>(items.keySet());
    }

    public ItemStack getItemFromID(String id) {
        if (items.get(id) == null) return new ItemStack(Material.AIR);
        return items.get(id).clone();
    }

    public GuiConfig getGuiConfig() {
        return guiConfig;
    }

    public boolean isExistingItem(String id) {
        return items.containsKey(id);
    }

    public FileConfiguration lang() {
        return langFileHandler.getLang();
    }

    public Component getMessage(String key, Map<String, String> placeholders) {
        String msg = lang().getString(key);
        if (msg == null) return TextUtility.color(key, placeholders);
        return TextUtility.color(msg, placeholders);
    }

    public Component getMessage(String key) {
        String msg = lang().getString(key);
        if (msg == null) return TextUtility.color(key);
        return TextUtility.color(msg);
    }

    public List<Component> getMessageList(String key) {
        List<String> list = lang().getStringList(key);
        if (list.isEmpty()) return new ArrayList<>();
        return TextUtility.colorList(list);
    }

    public List<Component> getMessageList(String key, Map<String , String> placeholder) {
        List<String> list = lang().getStringList(key);
        if (list.isEmpty()) return new ArrayList<>();
        return TextUtility.colorList(list, placeholder);
    }

    public void printMessage(Player player, List<Component> list) {
        for (Component component : list) {
            player.sendMessage(component);
        }
    }

    public void printLogs(String messageID) {
        List<String> lines = lang().getStringList(messageID);

        if (lines.isEmpty()) {
            String single = lang().getString(messageID);
            if (single == null) {
                plugin.getLogger().log(Level.INFO, ("Missing message: "+messageID));
                return;
            }
            lines = List.of(single);
        }

        lines.forEach(this::printTextLogs);
    }

    public void printTextLogs(String message) {
        plugin.getLogger().log(Level.INFO, ConsoleColors.colorize(message));
    }

    private List<String> getLines(String messageID) {
        List<String> lines = lang().getStringList(messageID);
        if (!lines.isEmpty()) return lines;

        String single = lang().getString(messageID);
        return single == null ? List.of() : List.of(single);
    }

    private String applyPlaceholders(String line, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            line = line.replace(entry.getKey(), entry.getValue());
        }
        return line;
    }

    public void printLogs(String messageID, Map<String, String> placeholders) {
        List<String> lines = getLines(messageID);

        if (lines.isEmpty()) {
            String single = lang().getString(messageID);
            if (single == null) {
                plugin.getLogger().log(Level.INFO, ("Missing message: "+messageID));
                return;
            }
            lines = List.of(single);
        }

        for (String line : lines) {
            plugin.getLogger().log(Level.INFO, ConsoleColors.colorize(applyPlaceholders(line, placeholders)));
        }
    }
}