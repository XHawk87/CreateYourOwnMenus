/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import java.util.ArrayList;
import java.util.List;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptDeleteCommand implements IMenuCommand {

    @Override
    public String getUsage() {
        return "/menu script delete [index] - Deletes the line with the given index (0 for first) in the held item's lore";
    }

    @Override
    public String getPermission() {
        return null;
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

            if (args.length != 1) {
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

            // Remove the deleted line
            String removedText;
            if (index == 0) {
                // Handle first-line special case
                String replacedWith;
                if (loreStrings.size() >= 2) {
                    replacedWith = loreStrings.get(1);
                    loreStrings.remove(1);
                } else {
                    replacedWith = "";
                }
                String firstLine = loreStrings.get(0);
                System.out.println("Attempting to delete first line: " + firstLine);
                int lastPartIndex = firstLine.lastIndexOf('\r') + 1;
                removedText = firstLine.substring(lastPartIndex);
                loreStrings.set(0, firstLine.substring(0, lastPartIndex) + replacedWith);
            } else {
                removedText = loreStrings.remove(index);
            }
            sender.sendMessage("Removed " + removedText + " from line " + index + " in the command list of this menu item");

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
