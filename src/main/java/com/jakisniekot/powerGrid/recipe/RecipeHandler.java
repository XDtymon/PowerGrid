package com.jakisniekot.powerGrid.recipe;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * SEMI AI MADE CLASS
 */
public class RecipeHandler {

    private static final String RECIPES_DIR = "recipes";

    private final MainPlugin plugin;
    private final File recipesFolder;
    private final Map<String, Recipe> recipes = new LinkedHashMap<>();

    public RecipeHandler(MainPlugin plugin) {
        this.plugin = plugin;
        this.recipesFolder = new File(plugin.getDataFolder(), RECIPES_DIR);
    }

    /** Wywołaj w onEnable. */
    public void setupRecipeFiles() {
        recipesFolder.mkdirs();
        saveDefaultRecipes();
        loadRecipes();
    }

    /** Ponowne wczytanie wszystkiego z dysku (np. /powergrid reload). */
    public void reload() {
        loadRecipes();
    }

    // ------------------------------------------------------------------
    // Kopiowanie domyślnych plików z jara
    // ------------------------------------------------------------------

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
                            // nie nadpisuj - właściciel mógł edytować plik
                            if (!new File(plugin.getDataFolder(), name).exists()) {
                                plugin.saveResource(name, false);
                            }
                        });
            }
        } catch (IOException | URISyntaxException e) {
            plugin.printLogs("info.recipeParsing.defaultsNotLoaded");
        }
    }

    // ------------------------------------------------------------------
    // Wczytywanie wszystkich plików z folderu (także podfolderów)
    // ------------------------------------------------------------------

    private void loadRecipes() {
        recipes.clear();
        plugin.printLogs("info.recipeParsing.start");


        try (Stream<Path> paths = Files.walk(recipesFolder.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> {
                        String n = p.getFileName().toString().toLowerCase();
                        return n.endsWith(".yml") || n.endsWith(".yaml");
                    })
                    .sorted()
                    .forEach(this::loadFile);
        } catch (IOException e) {
            plugin.printLogs("info.recipeParsing.folderError");

        }

        plugin.printLogs("info.recipeParsing.end");
    }

    private void loadFile(Path path) {
        String fileName = path.getFileName().toString();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

        // format 1: cały plik = jeden przepis, ID = nazwa pliku
        if (config.isConfigurationSection("inputs") || config.isConfigurationSection("outputs")) {
            String id = fileName.replaceFirst("(?i)\\.ya?ml$", "");
            registerRecipe(fileName, id, config);
            return;
        }

        // format 2: każdy klucz najwyższego poziomu = jeden przepis
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                plugin.getLogger().warning(fileName + ": '" + id + "' is not a section");
                continue;
            }
            registerRecipe(fileName, id, section);
        }
    }

    private void registerRecipe(String fileName, String id, ConfigurationSection section) {
        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("{ID}", id);

        if (recipes.containsKey(id)) {
            plugin.getLogger().warning(fileName + ": duplicated '" + id + "', skipping");
            return;
        }

        Recipe recipe = new Recipe();
        RecipeErrors error = recipe.parseRecipe(section, plugin);

        if (error.getLevel() == Level.SEVERE) {
            plugin.printLogs("info.recipeParsing.error", placeholder);
            return;
        } else if (error.getLevel() == Level.WARNING) {
            plugin.printLogs("info.recipeParsing.warning", placeholder);
        } else {
            plugin.printLogs("info.recipeParsing.successful", placeholder);
        }

        recipes.put(id, recipe);
    }

    // ------------------------------------------------------------------
    // Dostęp
    // ------------------------------------------------------------------

    public Recipe getRecipe(String id) {
        return recipes.get(id);
    }

    public Map<String, Recipe> getRecipes() {
        return Collections.unmodifiableMap(recipes);
    }
}