/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

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
public class MenuScriptTitleCommand implements IMenuCommand {

    @Override
    public String getUsage() {
        return "/menu script title [text] - Sets the held item's display name to the given text. May include formatting codes (&)";
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

            // Get the item meta
            ItemMeta meta = held.getItemMeta();

            // Expecting one or more parameters that make up new title
            StringBuilder sb = new StringBuilder(args[0]);
            for (int i = 1; i < args.length; i++) {
                sb.append(" ").append(args[i]);
            }
            String titleString = sb.toString();

            // Append to the lore
            if (!titleString.startsWith("/")) {
                // Support for colour codes in non-commands
                titleString = titleString.replace('&', ChatColor.COLOR_CHAR);
            }
            // Otherwise append this to the lore

            sender.sendMessage("The title of this menu item is now " + titleString);

            // Update the item
            meta.setDisplayName(titleString);
            held.setItemMeta(meta);
            return true;
        } else {
            sender.sendMessage("You must be logged in to set the title of a menu item");
            return true;
        }
    }
}
