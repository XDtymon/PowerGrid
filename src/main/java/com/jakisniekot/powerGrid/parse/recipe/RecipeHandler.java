package com.jakisniekot.powerGrid.parse.recipe;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.parse.exceptions.RecipeParseError;
import com.jakisniekot.powerGrid.util.TextUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * SEMI AI MADE CLASS
 */
public class RecipeHandler {

    private static final String RECIPES_DIR = "recipes";

    private final MainPlugin plugin;
    private final File recipesFolder;
    private final Map<String, Recipe> recipes = new LinkedHashMap<>();
    private final Map<String, List<Recipe>> recipesByMachine = new HashMap<>();

    public RecipeHandler(MainPlugin plugin) {
        this.plugin = plugin;
        this.recipesFolder = new File(plugin.getDataFolder(), RECIPES_DIR);
    }

    /** Wywołaj w onEnable. */
    public void setupRecipeFiles() {
        recipesFolder.mkdirs();
        saveDefaultRecipes();
    }

    /** Ponowne wczytanie wszystkiego z dysku (np. /powergrid reload). */
    public void reload() {
        parseRecipes();
    }

    private void saveDefaultRecipes() {
        try {
            CodeSource source = plugin.getClass().getProtectionDomain().getCodeSource();
            if (source == null) return;

            File jarFile = new File(source.getLocation().toURI());

            try (JarFile jar = new JarFile(jarFile)) {
                jar.stream()
                        .filter(e -> !e.isDirectory())
                        .map(JarEntry::getName)
                        .filter(name -> name.startsWith(RECIPES_DIR + "/"))
                        .filter(name -> name.endsWith(".yml") || name.endsWith(".yaml"))
                        .forEach(name -> {
                            if (!new File(plugin.getDataFolder(), name).exists()) {
                                plugin.saveResource(name, false);
                            }
                        });
            }
        } catch (IOException | URISyntaxException e) {
            plugin.errorHandler().print(RecipeParseError.DEFAULTS_NOT_LOADED, "");
            e.printStackTrace();
        }
    }

    public void parseRecipes() {
        recipes.clear();

        try (Stream<Path> paths = Files.walk(recipesFolder.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> {
                        String n = p.getFileName().toString().toLowerCase();
                        return n.endsWith(".yml") || n.endsWith(".yaml");
                    })
                    .sorted()
                    .forEach(this::loadFile);
        } catch (IOException e) {
            plugin.errorHandler().print(RecipeParseError.DEFAULTS_NOT_LOADED, "");
            return;
        }

        for (Recipe recipe : getRecipes().values()) {
            for (String id : recipe.getAvailableMachinesID()) {
                recipesByMachine.computeIfAbsent(id, k -> new ArrayList<>()).add(recipe);
            }
        }
    }

    private void loadFile(Path path) {
        String fileName = path.getFileName().toString();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

        if (config.isConfigurationSection("inputs") || config.isConfigurationSection("outputs")) {
            String id = fileName.replaceFirst("(?i)\\.ya?ml$", "");
            registerRecipe(fileName, id, config);
            return;
        }

        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            registerRecipe(fileName, id, section);
        }
    }

    private void registerRecipe(String fileName, String id, ConfigurationSection section) {

        if (recipes.containsKey(id)) {

            return;
        }

        Recipe recipe = new Recipe(section, plugin, fileName);
        if (recipe.getId() == null) return;

        recipes.put(id, recipe);
    }

    public Recipe getRecipe(String id) {
        return recipes.get(id);
    }

    public Map<String, Recipe> getRecipes() {
        return Collections.unmodifiableMap(recipes);
    }

    public Map<String, List<Recipe>> getRecipesByMachineList() {
        return recipesByMachine;
    }

    public List<Recipe> getRecipesForMachine(
            String machineID
    ) {
        return recipesByMachine.get(machineID);
    }

    public boolean isAnRecipe(String string) {
        return recipes.containsKey(string);
    }

    public void printRecipe(Recipe recipe, Player player) {
        Map<String, String> placeholder = new HashMap<>();

        List<String> message = plugin.lang().getStringList("command.recipe.print.main");
        String itemMessage = plugin.lang().getString(
                "command.recipe.print.item",
                " &8- &f{amount}&7x {name}"
        );
        String machineMessage = plugin.lang().getString(
                "command.recipe.print.machine",
                "&8- &f{machine}"
        );
        List<Component> list = new ArrayList<>();
        if (message.isEmpty()) return;

        long processTime = recipe.getNeededProgressTime();
        long energyUsage = recipe.getEnergyPerTick();
        ItemStack[] inputs = recipe.getInputs();
        ItemStack[] outputs = recipe.getOutputs();
        Set<String> machineIDs = recipe.getAvailableMachinesID();
        placeholder.put("{recipeID}", recipe.getId());
        placeholder.put("{processTime}", String.valueOf(processTime));
        placeholder.put("{energyUsage}", String.valueOf(energyUsage));

        for (String s : message) {
            if (s.contains("{INPUTS}")) {
                for (ItemStack itemStack : inputs) {
                    Map<String, String> itemPlaceholder = new HashMap<>();
                    itemPlaceholder.put("{amount}", String.valueOf(itemStack.getAmount()));
                    itemPlaceholder.put("{name}", getItemName(itemStack));
                    list.add(TextUtility.color(itemMessage, itemPlaceholder));
                }
            } else if (s.contains("{OUTPUTS}")) {
                for (ItemStack itemStack : outputs) {
                    Map<String, String> itemPlaceholder = new HashMap<>();
                    itemPlaceholder.put("{amount}", String.valueOf(itemStack.getAmount()));
                    itemPlaceholder.put("{name}", getItemName(itemStack));
                    list.add(TextUtility.color(itemMessage, itemPlaceholder));
                }
            } else if (s.contains("{MACHINES}")) {
                if (machineIDs.isEmpty()) {
                    list.add(TextUtility.color("&aALL"));
                } else {
                    for (String machine : machineIDs) {
                        Map<String, String> machinePlaceholder = new HashMap<>();
                        machinePlaceholder.put("{machine}", machine);
                        list.add(TextUtility.color(machineMessage,machinePlaceholder));
                    }
                }
            } else {
                list.add(TextUtility.color(s, placeholder));
            }
        }

        plugin.printMessage(player, list);

    }

    private String getItemName(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            Component display = meta.displayName();
            if (display != null) {
                return LegacyComponentSerializer.legacyAmpersand().serialize(display);
            }
        }
        return itemStack.getType().toString();
    }

}