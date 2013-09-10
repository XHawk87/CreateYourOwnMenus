/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptUtils {

    public static final String commandStart = "/";
    public static final String playerCommand = "@p/";
    public static final String hiddenCommand = ChatColor.COLOR_CHAR + "/";
    public static final String hiddenPlayerCommand = ChatColor.COLOR_CHAR + "@"
            + ChatColor.COLOR_CHAR + "p" + ChatColor.COLOR_CHAR + "/";

    public static void showCommands(List<String> loreStrings) {
        StringBuilder comments = new StringBuilder();
        List<String> commands = new ArrayList<>();

        // Expand all hidden commands from the first line
        if (!loreStrings.isEmpty()) {
            String firstLine = loreStrings.get(0);
            List<String> lines = unpackHiddenLines(firstLine);
            loreStrings.set(0, lines.get(lines.size() - 1));
            commands.addAll(lines.subList(0, lines.size() - 1));
        }

        for (int i = 0; i < loreStrings.size(); i++) {
            String loreString = loreStrings.get(i);
            if (loreString.startsWith(commandStart)
                    || loreString.startsWith(playerCommand)) {
                commands.add(loreString);
            } else if (loreString.startsWith(hiddenCommand)
                    || loreString.startsWith(hiddenPlayerCommand)) {
                // Legacy command hiding
                commands.add(unpackHiddenText(loreString));
            } else {
                comments.append(packHiddenText(loreString)).append(ChatColor.COLOR_CHAR).append('\r');
            }
        }

        // and condense them into the first line
        loreStrings = commands;
        if (loreStrings.isEmpty()) {
            loreStrings.add(commands.toString());
        } else {
            loreStrings.set(0, commands.toString() + loreStrings.get(0));
        }
    }

    public static void hideCommands(List<String> loreStrings) {
        StringBuilder commands = new StringBuilder();
        List<String> comments = new ArrayList<>();

        // Expand all hidden comments from the first line
        if (!loreStrings.isEmpty()) {
            String firstLine = loreStrings.get(0);
            List<String> lines = unpackHiddenLines(firstLine);
            loreStrings.set(0, lines.get(lines.size() - 1));
            comments.addAll(lines.subList(0, lines.size() - 1));
        }

        for (int i = 0; i < loreStrings.size(); i++) {
            String loreString = loreStrings.get(i);
            if (loreString.startsWith(commandStart)
                    || loreString.startsWith(playerCommand)) {
                commands.append(packHiddenText(loreString)).append(ChatColor.COLOR_CHAR).append('\r');
            } else if (loreString.startsWith(hiddenCommand)
                    || loreString.startsWith(hiddenPlayerCommand)) {
                // Legacy command hiding
                commands.append(loreString).append(ChatColor.COLOR_CHAR).append('\r');
            } else {
                comments.add(loreString);
            }
        }

        // and condense them into the first line
        loreStrings = comments;
        if (loreStrings.isEmpty()) {
            loreStrings.add(commands.toString());
        } else {
            loreStrings.set(0, commands.toString() + loreStrings.get(0));
        }
    }

    /**
     * Strip all colour chars used to hide the lines
     *
     * @param firstLine The first line of the script
     * @return The unpacked hidden lines
     */
    public static List<String> unpackHiddenLines(String firstLine) {
        return Arrays.asList(unpackHiddenText(firstLine).split("\r"));
    }

    /**
     * Remove colour chars from a line of text that are used to hide it, and
     * stop when reaching clear-text
     *
     * @param line The line to unpack
     * @return The unpacked line
     */
    public static String unpackHiddenText(String line) {
        StringBuilder sb = new StringBuilder();
        boolean expectColourChar = true;
        boolean clearText = false;
        for (char c : line.toCharArray()) {
            if (clearText) {
                sb.append(c);
            } else {
                if (expectColourChar) {
                    if (c != ChatColor.COLOR_CHAR) {
                        // Since there is no colour char, we must have reached 
                        // the end of the hidden section
                        sb.append(c);
                        clearText = true;
                    }
                } else {
                    sb.append(c);
                }
                expectColourChar = !expectColourChar;
            }
        }
        return sb.toString();
    }

    /**
     * Add a colour char to the front of each character in order to hide it
     *
     * @param line The line to pack
     * @return The packed line
     */
    public static String packHiddenText(String line) {
        StringBuilder sb = new StringBuilder();
        // Place a color char in front of each char in order to hide the comments
        for (char c : line.toCharArray()) {
            sb.append(ChatColor.COLOR_CHAR).append(c);
        }
        return sb.toString();
    }

    /**
     * Checks if the given ItemStack is a valid menu script item
     *
     * @param item The ItemStack to check
     * @return True if it is a valid menu item, otherwise false
     */
    public static boolean isValidMenuItem(ItemStack item) {
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                // Check that the lore contains at least one scripted command
                for (String loreString : meta.getLore()) {
                    if (loreString.startsWith(commandStart)
                            || loreString.startsWith(playerCommand)
                            || loreString.startsWith(hiddenCommand)
                            || loreString.startsWith(hiddenPlayerCommand)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
