/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author XHawk87
 */
public class MenuScriptUtils {

    public static final String commandStart = "/";
    public static final String playerCommand = "@p/";
    public static final String hiddenCommand = ChatColor.COLOR_CHAR + "/";
    public static final String hiddenPlayerCommand = ChatColor.COLOR_CHAR + "@"
            + ChatColor.COLOR_CHAR + "p" + ChatColor.COLOR_CHAR + "/";

    /**
     * Strip all colour chars used to hide the lines
     *
     * @param firstLine The first line of the script
     * @return The unpacked hidden lines
     */
    public static List<String> unpackHiddenLines(String firstLine) {
        String[] lines = firstLine.split(ChatColor.COLOR_CHAR + "\r", -1);
        List<String> unpacked = new ArrayList<>();
        for (int i = 0; i < lines.length - 1; i++) {
            unpacked.add(unpackHiddenText(lines[i]));
        }
        unpacked.add(lines[lines.length - 1]);
        return unpacked;
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
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                // Check that the lore contains at least one scripted command
                List<String> loreStrings = meta.getLore();
                if (loreStrings.isEmpty()) {
                    return false;
                }
                loreStrings.addAll(unpackHiddenLines(loreStrings.get(0)));

                for (String loreString : loreStrings) {
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

    /**
     * @param commandString The line to add to the lore
     * @param loreStrings   The lore lines
     */
    public static void appendToLore(String commandString, List<String> loreStrings) {
        if (loreStrings.size() == 1) {
            // Handle first-line special case
            String firstLine = loreStrings.get(0);
            int lastPartIndex = firstLine.lastIndexOf("\r") + 1;
            String lastPart = firstLine.substring(lastPartIndex);
            if (lastPart.isEmpty()) {
                loreStrings.set(0, firstLine.substring(0, lastPartIndex) + commandString);
            } else {
                loreStrings.add(commandString);
            }
        } else {
            loreStrings.add(commandString);
        }
    }

    /**
     * @param itemStackRef The item in the sender's hand
     * @param sender       The command sender
     * @param plugin       This plugin
     * @return True if the sender's hand is empty, otherwise false
     */
    public static boolean isEmptyHand(ItemStackRef itemStackRef, CommandSender sender, CreateYourOwnMenus plugin) {
        ItemStack held = itemStackRef.get();
        if (held == null || held.getType() == Material.AIR) {
            sender.sendMessage(plugin.translate(sender, "error-no-item-in-hand", "You must be holding a menu item"));
            return true;
        }
        return false;
    }

    /**
     * Support for colour codes in non-commands
     *
     * @param commandString The command string
     * @return If a text line, replace '&' with the chat colour character, otherwise the same string
     */
    public static String replaceColorSymbol(String commandString) {
        return commandString.startsWith("/") ? commandString : commandString.replace('&', ChatColor.COLOR_CHAR);
    }

    /**
     * Expand all hidden commands from the first line
     *
     * @param loreStrings Lines of text from the item lore
     */
    public static void unpackHiddenLinesFromLore(List<String> loreStrings) {
        if (!loreStrings.isEmpty()) {
            String firstLine = loreStrings.get(0);
            List<String> lines = unpackHiddenLines(firstLine);
            loreStrings.set(0, lines.get(lines.size() - 1));
            loreStrings.addAll(lines.subList(0, lines.size() - 1));
        }
    }
}
