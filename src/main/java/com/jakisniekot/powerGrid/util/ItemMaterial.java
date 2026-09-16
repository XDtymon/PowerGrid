package com.jakisniekot.powerGrid.util;

import com.jakisniekot.powerGrid.MainPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ItemMaterial {

    public static ItemStack simple(String material) {

        ItemStack itemStack = new ItemStack(Material.AIR);

        try {
            if (Material.valueOf(material) != null) {
                itemStack = new ItemStack(Material.valueOf(material));
            }
        } catch (IllegalArgumentException argumentException) {
            if (material.endsWith("=")) {
                itemStack = head64(material);
            } else {
                itemStack = headPlayer(material);
            }
        }



        return itemStack;
    }

    public static ItemStack headPlayer(String material) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();

        OfflinePlayer player = Bukkit.getOfflinePlayer(material);

        itemMeta = (SkullMeta) itemStack.getItemMeta();
        if (player != null) {
            itemMeta.setOwningPlayer(player);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static ItemStack head64(String material) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = null;

            itemMeta = (SkullMeta) itemStack.getItemMeta();
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

            PlayerTextures textures = profile.getTextures();

            try {
                String decoded = new String(Base64.getDecoder().decode(material));
                String url = decoded.split("\"url\":\"")[1].split("\"")[0];

                textures.setSkin(new URL(url));
            } catch (Exception e) {
                e.printStackTrace();
            }

            profile.setTextures(textures);
            itemMeta.setOwnerProfile(profile);

            itemStack.setItemMeta(itemMeta);




        return itemStack;
    }
}
