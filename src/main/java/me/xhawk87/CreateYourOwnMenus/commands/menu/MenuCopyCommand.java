/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Sub-command to create a new menu
 *
 * @author XHawk87
 */
public class MenuCopyCommand implements IMenuCommand {

    private CreateYourOwnMenus plugin;

    public MenuCopyCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Entering a sub-command without parameters is assumed to be a request 
        // for information. So display some detailed help.
        if (args.length == 0) {
            sender.sendMessage(plugin.translate(sender, "menu-copy-usage-extended", "/menu copy [old menu id] [new menu id] [new title] - Create a new menu with the same size and contents as the given menu but with a new id and title"));
            return true;
        }
        // The title may contain spaces, so more than 3 args may be present
        if (args.length < 3) {
            return false;
        }

        String oldMenuId = args[0];
        String newMenuId = args[1];

        Menu oldMenu = plugin.getMenu(oldMenuId);
        if (oldMenu == null) {
            sender.sendMessage(plugin.translate(sender, "unknown-menu-id", "There is no menu with id {0}", oldMenuId));
            return true;
        }
        int rows = oldMenu.getInventory().getSize() / 9;

        // Build the title
        StringBuilder sb = new StringBuilder(args[2].replace('&', ChatColor.COLOR_CHAR));
        for (int i = 3; i < args.length; i++) {
            sb.append(" ").append(args[i].replace('&', ChatColor.COLOR_CHAR));
        }
        String title = sb.toString();
        if (title.length() > 32) {
            sender.sendMessage(plugin.translate(sender, "title-char-limit", "Titles are limited to 32 characters (including colours)"));
            return true;
        }

        // Check that the id is unique
        if (plugin.getMenu(newMenuId) != null) {
            sender.sendMessage(plugin.translate(sender, "menu-already-exists", "A menu with id {0} already exists", newMenuId));
            return true;
        }

        // Create the menu
        Menu newMenu = plugin.createMenu(newMenuId, title, rows);
        Inventory inv = newMenu.getInventory();
        ItemStack[] contents = oldMenu.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                inv.setItem(i, item.clone());
            }
        }
        newMenu.save();
        sender.sendMessage(plugin.translate(sender, "menu-created", "{0} menu has been created", title));
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu copy [old menu id] [new menu id] [new title] - Create a new menu with the same size and contents as the given menu but with a new id and title";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.copy";
    }
}
