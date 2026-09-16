package com.jakisniekot.powerGrid.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

public class ConsoleColors {

    private static final Map<Character, String> LEGACY_MAP = new HashMap<>();
    private static final Pattern LEGACY_PATTERN = Pattern.compile("[&§]([0-9a-fk-or])", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEX_AMP_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    static {
        LEGACY_MAP.put('0', "\u001B[30m"); // Black
        LEGACY_MAP.put('1', "\u001B[34m"); // Dark Blue
        LEGACY_MAP.put('2', "\u001B[32m"); // Dark Green
        LEGACY_MAP.put('3', "\u001B[36m"); // Dark Aqua
        LEGACY_MAP.put('4', "\u001B[31m"); // Dark Red
        LEGACY_MAP.put('5', "\u001B[35m"); // Dark Purple
        LEGACY_MAP.put('6', "\u001B[33m"); // Gold
        LEGACY_MAP.put('7', "\u001B[37m"); // Gray
        LEGACY_MAP.put('8', "\u001B[90m"); // Dark Gray
        LEGACY_MAP.put('9', "\u001B[94m"); // Blue
        LEGACY_MAP.put('a', "\u001B[92m"); // Green
        LEGACY_MAP.put('b', "\u001B[96m"); // Aqua
        LEGACY_MAP.put('c', "\u001B[91m"); // Red
        LEGACY_MAP.put('d', "\u001B[95m"); // Light Purple
        LEGACY_MAP.put('e', "\u001B[93m"); // Yellow
        LEGACY_MAP.put('f', "\u001B[97m"); // White
        LEGACY_MAP.put('k', "");           // Obfuscated - brak odpowiednika, pomijamy
        LEGACY_MAP.put('l', "\u001B[1m");  // Bold
        LEGACY_MAP.put('m', "\u001B[9m");  // Strikethrough
        LEGACY_MAP.put('n', "\u001B[4m");  // Underline
        LEGACY_MAP.put('o', "\u001B[3m");  // Italic
        LEGACY_MAP.put('r', "\u001B[0m");
    }

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return text;

        // 1. Hex w formacie &#RRGGBB
        text = replaceHex(text, HEX_AMP_PATTERN);
        // 2. Hex w formacie <#RRGGBB>
        //  text = replaceHex(text, HEX_TAG_PATTERN);
        // 3. Legacy kody & i §
        text = replaceLegacy(text);
        // 4. MiniMessage tagi <green>, </green> itd.
        //  text = replaceMiniMessage(text);

        return text + "\u001B[0m"; // domyślny reset na końcu, żeby kolor nie "wyciekł"
    }

    private static String replaceLegacy(String text) {
        Matcher matcher = LEGACY_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            char code = Character.toLowerCase(matcher.group(1).charAt(0));
            String ansi = LEGACY_MAP.getOrDefault(code, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(ansi));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String replaceHex(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            String ansi = String.format("\u001B[38;2;%d;%d;%dm", r, g, b);
            matcher.appendReplacement(result, Matcher.quoteReplacement(ansi));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
