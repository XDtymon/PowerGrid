package com.jakisniekot.powerGrid.command;

import com.jakisniekot.powerGrid.MainPlugin;
import com.sun.jdi.IntegerType;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
                    break;

                    /*
                case "machinetype":
                    if (args.length == 4) {

                    }

                     */
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
