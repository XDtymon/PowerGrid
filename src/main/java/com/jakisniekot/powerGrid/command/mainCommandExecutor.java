package com.jakisniekot.powerGrid.command;

import com.jakisniekot.powerGrid.MainPlugin;
import com.jakisniekot.powerGrid.parse.recipe.Recipe;
import com.jakisniekot.powerGrid.power.network.EnergyNetwork;
import com.jakisniekot.powerGrid.util.LocationUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class mainCommandExecutor implements CommandExecutor {

    private MainPlugin plugin;

    public mainCommandExecutor(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (sender instanceof Player player) {

            switch (args[0].toLowerCase()) {
                case "give":

                    if (!player.hasPermission("powergrid.command.give")) {
                        return true;
                    }
                    //pg give <item> (player) (amount)

                    String item = null;
                    Player target = null;
                    int amount = 0;


                    if (args.length >= 2) {
                        if (plugin.isExistingItem(args[1])) {
                            item = args[1];
                        } else {
                            return true;
                        }
                    }

                    if (args.length >= 3) {
                        if (Bukkit.getPlayer(args[2]) != null) {
                            target = Bukkit.getPlayer(args[2]);
                        } else {
                            return true;
                        }
                    }

                    if (args.length == 4) {
                        try {
                            amount = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            return true;
                        }
                    }

                    if (args.length == 2) giveItem(player, 1, item);
                    if (args.length == 3) giveItem(target, 1, item);
                    if (args.length == 4) giveItem(target, amount, item);

                    break;
                case "reload":
                    break;
                case "save":
                    plugin.getConnectorDAO().saveAllConnectors();
                    plugin.getMachineDAO().saveAllMachinery();
                    break;
                case "machine":

                    /*
                    if (args[1].toLowerCase().equals("structure")) {
                        String machineId = args[2];

                        Block targetBlock = player.getTargetBlockExact(10);
                        if (targetBlock == null) {
                            player.sendMessage("You must be looking directly at a block within range.");
                            return true;
                        }

                        int MAX_AIM_DISTANCE = 5;

                        Location eye = player.getEyeLocation();
                        Vector direction = eye.getDirection();

                        RayTraceResult result = player.getWorld().rayTraceBlocks(eye, direction, MAX_AIM_DISTANCE);

                        int[] controllerPos = {
                                result.getHitBlock().getLocation().getBlockX(),
                                result.getHitBlock().getLocation().getBlockY(),
                                result.getHitBlock().getLocation().getBlockZ()
                        };


                        if (result != null && result.getHitBlock() != null) {
                            controllerPos[0] = result.getHitBlock().getLocation().getBlockX();
                            controllerPos[1] = result.getHitBlock().getLocation().getBlockY();
                            controllerPos[2] = result.getHitBlock().getLocation().getBlockZ();
                        } else {
                            return true;

                        }


                        Multiblock multiblock = new Multiblock(
                                plugin
                                        .getMachineFileHandler()
                                        .getMachineTypesConfig()
                                        .getConfigurationSection("machines."+machineId)
                        );

                        if (multiblock == null) {
                            player.sendMessage("No machine found with id '" + machineId + "'.");
                            return true;
                        }

                        List<List<List<Material>>> structure = multiblock.getStructure();

                        if (controllerPos == null) {
                            player.sendMessage("This machine's structure has no controller ('@') defined.");
                            return true;
                        }

                        World world = targetBlock.getWorld();
                        Location controllerLoc = targetBlock.getLocation();

                        int placed = 0;

                        multiblock.build(world.getBlockAt(controllerLoc));

                        player.sendMessage("Pasted multiblock '" + machineId + "' (" + placed + " blocks placed).");
                        return true;
                    }

                     */

                case "recipes":
                    if (args.length == 1) {
                        for (String id : plugin.recipes().getRecipes().keySet()) {
                            Recipe recipe = plugin.recipes().getRecipe(id);
                            plugin.recipes().printRecipe(recipe, player);
                        }
                    } else if (args.length == 2) {
                        String recipeID = args[1];
                        if (!plugin.recipes().isAnRecipe(recipeID)) {

                            return true;
                        }

                        Recipe recipe = plugin.recipes().getRecipe(recipeID);
                        plugin.recipes().printRecipe(recipe, player);
                    }

                    break;
                case "item":
                    if (args.length == 2) {
                        switch (args[1].toLowerCase()) {
                            case "parse":
                                org.bukkit.inventory.ItemStack itemStack = player.getInventory().getItemInMainHand();

                                YamlConfiguration yaml = new YamlConfiguration();
                                yaml.set("item", itemStack);

                                String serialized = yaml.saveToString()
                                        .replaceFirst("^item:\\s*\\n", "");

                                Component message = plugin.getMessage("command.getSerializedItem.success")
                                        .clickEvent(ClickEvent.copyToClipboard(serialized))
                                        .hoverEvent(HoverEvent.showText(
                                                Component.text("Kliknij, aby skopiować item")
                                        ));

                                player.sendMessage(message);
                                break;
                        }

                    }
                    break;
                case "network":
                    if (args.length == 2) {
                        switch (args[1].toLowerCase()) {
                            case "info":
                                Location location = LocationUtil.getTargetLocation(player);
                                if (location == null) return true;
                                EnergyNetwork net = plugin.networkManager().getNetworkAt(location);
                                if (net != null) net.diagnose(plugin, 1).forEach(sender::sendMessage);
                                break;
                        }
                    }
                    break;
            }


        } else {

        }

        return true;
    }


    private void giveItem(Player player, int amount, String id) {
        ItemStack itemStack = plugin.getItemFromID(id);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }


}
