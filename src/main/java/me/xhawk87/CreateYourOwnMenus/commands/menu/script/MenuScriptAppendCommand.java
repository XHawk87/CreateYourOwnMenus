/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import java.util.ArrayList;
import java.util.List;
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
public class MenuScriptAppendCommand extends IMenuScriptCommand {

    @Override
    public String getUsage() {
        return "/menu script ([player]) append [text] - Adds this text as the last line in the held item's lore";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.append";
    }

    @Override
    public boolean onCommand(CommandSender sender, Player target, Command command, String label, String[] args) {
        // Check the player is holding the item
        ItemStack held = target.getItemInHand();
        if (held == null || held.getTypeId() == 0) {
            sender.sendMessage("You must be holding a menu item");
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

        // Expecting one or more parameters that make up the command or comment to add
        StringBuilder sb = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
            sb.append(" ").append(args[i]);
        }
        String commandString = sb.toString();

        // Append to the lore
        if (!commandString.startsWith("/")) {
            // Support for colour codes in non-commands
            commandString = commandString.replace('&', ChatColor.COLOR_CHAR);
        }
        // Otherwise append this to the lore

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

        sender.sendMessage(commandString + " was added to the command list of this menu item");

        // Update the item
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
