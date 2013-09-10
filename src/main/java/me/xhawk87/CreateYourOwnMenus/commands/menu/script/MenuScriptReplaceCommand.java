/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import java.util.ArrayList;
import java.util.List;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptReplaceCommand implements IMenuCommand {

    @Override
    public String getUsage() {
        return "/menu script replace [index] [text] - Replaces the line with the given index (0 for first) with the given text in the held item's lore";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.replace";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Only players can hold items
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Check the player is holding the item
            ItemStack held = player.getItemInHand();
            if (held == null || held.getTypeId() == 0) {
                player.sendMessage("You must be holding a menu item");
                return true;
            }

            // Get or create the lore
            ItemMeta meta = held.getItemMeta();
            List<String> loreStrings;
            if (meta.hasLore()) {
                loreStrings = meta.getLore();
            } else {
                loreStrings = new ArrayList<>();
            }

            if (args.length < 2) {
                return false;
            }

            String indexString = args[0];
            int index;
            try {
                index = Integer.parseInt(indexString);
                if (index < 0) {
                    sender.sendMessage("The index must be at least 0: " + indexString);
                    return true;
                }
                if (index >= loreStrings.size()) {
                    sender.sendMessage("The index must be less than the number of lines in the lore (" + loreStrings.size() + "): " + indexString);
                    return true;
                }
            } catch (NumberFormatException ex) {
                sender.sendMessage("The index must be a whole number: " + indexString);
                return true;
            }

            // Expecting one or more parameters that make up the command or comment to add
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++) {
                sb.append(" ").append(args[i]);
            }
            String commandString = sb.toString();

            // Replace the given line in the lore
            if (!commandString.startsWith("/")) {
                // Support for colour codes in non-commands
                commandString = commandString.replace('&', ChatColor.COLOR_CHAR);
            }
            loreStrings.set(index, commandString);
            sender.sendMessage(commandString + " replaced line " + index + " in the command list of this menu item");

            // Update the item
            meta.setLore(loreStrings);
            held.setItemMeta(meta);
            return true;
        } else {
            sender.sendMessage("You must be logged in to replace a line in a menu item script");
            return true;
        }
    }
}
