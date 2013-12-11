/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import java.util.ArrayList;
import java.util.List;
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptClearCommand extends IMenuScriptCommand {

    @Override
    public String getUsage() {
        return "/menu script ([player]) clear - Removes all lore from the item in your hand";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.clear";
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

        // Clear all lore
        loreStrings.clear();
        sender.sendMessage("The command list for this menu item has been cleared");

        // Update the item
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
