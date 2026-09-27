package com.jakisniekot.powerGrid.command;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class mainCommandTabber implements TabCompleter {

    private MainPlugin plugin;

    public mainCommandTabber(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        List<String> completions = new ArrayList<>();

        List<String> items = plugin.getItemsList();

        Set<String> machines = plugin.getMachineFileHandler().getMachineTypesConfig().getConfigurationSection("machines").getKeys(false);

        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getDisplayName());
        }

        if (command.getName().equalsIgnoreCase("pg") || command.getName().equalsIgnoreCase("powergrid")) {

            if (sender instanceof Player player) {
                if (args.length == 1) {
                    completions.add("give");
                    completions.add("reload");
                    completions.add("save");
                    completions.add("machine");
                } else {
                    switch (args[0].toLowerCase()) {
                        case "give":
                            if (args.length == 2) {
                                completions.addAll(items);
                            } else if (args.length == 3) {
                                completions.addAll(players);
                            } else if (args.length == 4) {
                                completions.add("1");
                                completions.add("2");
                                completions.add("4");
                                completions.add("8");
                                completions.add("16");
                                completions.add("32");
                                completions.add("64");
                            }

                            break;
                        case "machine":
                            if (args.length == 2) {
                                completions.add("structure");
                            } else if (args.length == 3) {
                                completions.addAll(machines);
                            }
                            break;
                    }
                }
            }
        }

        String lastWord = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(lastWord));

        return completions;
    }
}
