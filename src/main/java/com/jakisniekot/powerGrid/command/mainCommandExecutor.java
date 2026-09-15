package com.jakisniekot.powerGrid.command;

import com.jakisniekot.powerGrid.MainPlugin;
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

            if (args.length == 0) {

            } else if (args.length == 1) {

            } else if (args.length == 2) {

            } else if (args.length == 3) {
                switch (args[0].toLowerCase()) {
                    case "give":

                        int amount = Integer.parseInt(args[2]);
                        if (amount == 0) {
                            amount = 1;
                        }
                        if (plugin.isExistingItem(args[1])) {
                            giveItem(player, amount, args[1]);
                        } else {

                        }

                        break;
                }
            } else if (args.length == 4) {

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
