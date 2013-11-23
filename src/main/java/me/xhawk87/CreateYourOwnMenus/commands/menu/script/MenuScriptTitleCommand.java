/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptCommand;
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
public class MenuScriptTitleCommand extends IMenuScriptCommand {

    @Override
    public String getUsage() {
        return "/menu script ([player]) title [text] - Sets the held item's display name to the given text. May include formatting codes (&)";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Player target, Command command, String label, String[] args) {
        // Check the player is holding the item
        ItemStack held = target.getItemInHand();
        if (held == null || held.getTypeId() == 0) {
            sender.sendMessage("You must be holding a menu item");
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
    }
}
