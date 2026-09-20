package com.jakisniekot.powerGrid.util;

import com.jakisniekot.powerGrid.MainPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtility {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final Pattern HEX_PATTERN =
            Pattern.compile("(?:&)?#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_HEX_PATTERN =
            Pattern.compile("(?i)(?:&)?#([A-F0-9]{6})");

    private TextUtility() {

    }

    public static String stringReplacer(String text, Map<String, String> placeholders, boolean consoleTEXT) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        if (consoleTEXT) {
            return ConsoleColors.colorize(text);
        } else {
            return colorLegacy(text);
        }
    }

    public static Component stringReplacer(String text, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        return color(text);
    }

    public static List<String> listReplacer(List<String> list, Map<String, String> placeholders, boolean consoleTEXT) {
        List<String> newList = new ArrayList<>();

        for (String text : list) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue());
            }

            if (consoleTEXT) {
                newList.add(ConsoleColors.colorize(text));
            } else {
                newList.add(colorLegacy(text));
            }

        }


        return newList;
    }

    public static List<Component> listReplacer(List<String> list, Map<String, String> placeholders) {
        List<Component> newList = new ArrayList<>();

        for (String text : list) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue());
            }

            newList.add(color(text));
        }


        return newList;
    }

        public static Component color(String text) {
            if (text == null || text.isEmpty()) {
                return Component.empty();
            }

            // &7, &a, &l, &n itd.
            text = replaceHexColors(text);
            text = replaceLegacyColors(text);

            // #00ff00 -> <#00ff00>

            // Parsowanie MiniMessage
            return MINI_MESSAGE.deserialize(text).decoration(TextDecoration.ITALIC, false);
        }

        public static String colorLegacy(String text) {
            if (text == null || text.isEmpty()) {
                return "";
            }

            // Obsługa #FF0000 oraz &#FF0000
            Matcher matcher = LEGACY_HEX_PATTERN.matcher(text);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                String hex = matcher.group(1);

                String replacement = ChatColor.of("#" + hex).toString();

                matcher.appendReplacement(
                        buffer,
                        Matcher.quoteReplacement(replacement)
                );
            }

            matcher.appendTail(buffer);

            // Obsługa &a, &c, &l itd.
            return ChatColor.translateAlternateColorCodes('&', buffer.toString());
        }

        private static String replaceLegacyColors(String text) {
            return text
                    .replace("&0", "<black>")
                    .replace("&1", "<dark_blue>")
                    .replace("&2", "<dark_green>")
                    .replace("&3", "<dark_aqua>")
                    .replace("&4", "<dark_red>")
                    .replace("&5", "<dark_purple>")
                    .replace("&6", "<gold>")
                    .replace("&7", "<gray>")
                    .replace("&8", "<dark_gray>")
                    .replace("&9", "<blue>")
                    .replace("&a", "<green>")
                    .replace("&b", "<aqua>")
                    .replace("&c", "<red>")
                    .replace("&d", "<light_purple>")
                    .replace("&e", "<yellow>")
                    .replace("&f", "<white>")
                    .replace("&l", "<bold>")
                    .replace("&o", "<italic>")
                    .replace("&n", "<underlined>")
                    .replace("&m", "<strikethrough>")
                    .replace("&k", "<obfuscated>")
                    .replace("&r", "<reset>");
        }

        private static String replaceHexColors(String text) {
            Matcher matcher = HEX_PATTERN.matcher(text);

            return matcher.replaceAll("<#$1>");

        }
}
