/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import java.util.ArrayList;
import java.util.List;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptCommand;
import me.xhawk87.CreateYourOwnMenus.utils.ItemStackRef;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptClearCommand extends IMenuScriptCommand {

    public MenuScriptClearCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) clear - Removes all lore from the item in your hand";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.clear";
    }

    @Override
    public boolean onCommand(CommandSender sender, ItemStackRef itemStackRef, Command command, String label, String[] args) {
        // Check the player is holding the item
        ItemStack held = itemStackRef.get();
        if (held == null || held.getTypeId() == 0) {
            sender.sendMessage(plugin.translate(sender, "error-no-item-in-hand", "You must be holding a menu item"));
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
        sender.sendMessage(plugin.translate(sender, "script-cleared", "The command list for this menu item has been cleared"));

        // Update the item
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
